package com.preptrove.app

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupCrypto {
 const val MAX_BYTES=5*1024*1024
 private val magic="PTB1".toByteArray(Charsets.US_ASCII)
 private fun key(password:CharArray,salt:ByteArray):SecretKeySpec {
  val spec=PBEKeySpec(password,salt,210000,256)
  return try {SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded,"AES")} finally {spec.clearPassword()}
 }
 fun encrypt(bytes:ByteArray,password:CharArray):ByteArray {
  require(password.size>=12) {"Use a password of at least 12 characters."}
  require(bytes.size<=MAX_BYTES-48) {"Backup is too large."}
  val random=SecureRandom();val salt=ByteArray(16).also {random.nextBytes(it)};val iv=ByteArray(12).also {random.nextBytes(it)}
  val cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,key(password,salt),GCMParameterSpec(128,iv));cipher.updateAAD(magic)
  return magic+salt+iv+cipher.doFinal(bytes)
 }
 fun decrypt(bytes:ByteArray,password:CharArray):ByteArray {
  require(bytes.size in 48..MAX_BYTES && bytes.copyOfRange(0,4).contentEquals(magic)) {"Unsupported backup file."}
  val cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,key(password,bytes.copyOfRange(4,20)),GCMParameterSpec(128,bytes.copyOfRange(20,32)));cipher.updateAAD(magic)
  return cipher.doFinal(bytes.copyOfRange(32,bytes.size))
 }
}
data class BackupSnapshot(val entries:List<Entry>,val reviews:List<ReviewEvent> = emptyList())
object BackupCodec {
 fun encode(entries:List<Entry>,reviews:List<ReviewEvent> = emptyList()):ByteArray {
  require(entries.size<=5000 && reviews.size<=50000) {"Too many backup records."}
  val array=JSONArray()
  entries.forEach {e->array.put(JSONObject().put("id",e.id).put("kind",e.kind).put("title",e.title).put("detail",e.detail).put("category",e.category).put("kitId",e.kitId).put("quantity",e.quantity).put("unit",e.unit).put("expiry",e.expiry).put("location",e.location).put("done",e.done).put("checked",e.checked).put("extraData",e.extraData).put("purchasedDate",e.purchasedDate).put("intervalDays",e.intervalDays).put("isTemplate",e.isTemplate))}
  val ids=entries.map {it.id}.toSet()
  val history=JSONArray()
  reviews.filter {it.entryId in ids}.forEach {review->history.put(JSONObject().put("id",review.id).put("entryId",review.entryId).put("title",review.title).put("completedOn",review.completedOn))}
  val root=JSONObject().put("schemaVersion",if(history.length()==0) 1 else 2).put("records",array)
  if(history.length()>0)root.put("reviews",history)
  return root.toString(2).toByteArray(Charsets.UTF_8).also {require(it.size<=BackupCrypto.MAX_BYTES-48) {"Backup is too large."}}
 }
 fun decodeCopies(bytes:ByteArray):List<Entry> = decodeSnapshotCopies(bytes).entries
 fun decodeSnapshotCopies(bytes:ByteArray):BackupSnapshot {
  require(bytes.size<=BackupCrypto.MAX_BYTES) {"Backup exceeds the size limit."}
  val root=JSONObject(bytes.toString(Charsets.UTF_8));val version=root.getInt("schemaVersion");require(version in 1..2) {"Unsupported backup version."}
  val array=root.getJSONArray("records");require(array.length()<=5000) {"Too many records."}
  val entries=(0 until array.length()).map {index->
   val o=array.getJSONObject(index)
   fun value(key:String,default:String=""):String=o.optString(key,default).also {require(it.length<=20000) {"Field too long."}}
   val id=o.getString("id");require(id.isNotBlank() && id.length<=100)
   val kind=o.getString("kind");require(kind in setOf("kit","supply","plan","contact","check","reminder"))
   val title=o.getString("title");require(title.isNotBlank() && title.length<=500)
   val quantity=o.optDouble("quantity",1.0);require(quantity.isFinite() && quantity>=0)
   val expiry=value("expiry");val checked=value("checked");listOf(expiry,checked).filter {it.isNotEmpty()}.forEach {LocalDate.parse(it)}
   run {val purchased=value("purchasedDate");if(purchased.isNotEmpty()) LocalDate.parse(purchased)
   val interval=o.optInt("intervalDays",0);require(interval in 0..3650)
   val extra=value("extraData","{}");JSONObject(extra)
   Entry(id,kind,title,value("detail"),value("category","Other"),value("kitId"),quantity,value("unit","items"),expiry,value("location"),o.optBoolean("done",false),checked,extra,purchased,interval,o.optBoolean("isTemplate",false))}
  }
  require(entries.map {it.id}.toSet().size==entries.size) {"Duplicate record IDs."}
  val ids=entries.associate {it.id to UUID.randomUUID().toString()};val kits=entries.filter {it.kind=="kit"}.map {it.id}.toSet()
  val history=if(version==2) root.getJSONArray("reviews") else JSONArray()
  require(history.length()<=50000) {"Too many review events."}
  val oldReviewIds=mutableSetOf<String>()
  val reviews=(0 until history.length()).map {index->
   val review=history.getJSONObject(index)
   val id=review.getString("id");require(id.isNotBlank() && id.length<=100 && oldReviewIds.add(id)) {"Invalid review ID."}
   val entryId=review.getString("entryId");require(entryId in ids) {"Review has no matching record."}
   val title=review.getString("title");require(title.isNotBlank() && title.length<=500)
   val completedOn=review.getString("completedOn");LocalDate.parse(completedOn)
   ReviewEvent(entryId=ids.getValue(entryId),title=title,completedOn=completedOn)
  }
  return BackupSnapshot(entries.map {it.copy(id=ids.getValue(it.id),kitId=if(it.kitId in kits) ids.getValue(it.kitId) else "")},reviews)
 }
}
