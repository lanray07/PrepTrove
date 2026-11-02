package com.preptrove.app

import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test

class PlannerDocumentsTest {
 @Test fun transportedBackupsDropLocalPhotoLinksButDraftStateKeepsThem() {
  val uri=create()
  val entry=Entry(kind="supply",title="Synthetic photo record",extraData="{\"photo\":\"00000000-0000-0000-0000-000000000000.jpg\",\"source\":\"Synthetic shop\"}")
  try {
   PlannerDocuments.write(context,uri,BackupSnapshot(listOf(entry)),"json",ExportPolicy.Options(),false,charArrayOf())
   assertFalse(bytes(uri).toString(Charsets.UTF_8).contains("00000000-0000-0000-0000-000000000000.jpg"))
   // A legacy or externally supplied backup must not attach an existing private local image.
   context.contentResolver.openOutputStream(uri,"wt")!!.use {it.write(BackupCodec.encode(listOf(entry)))}
   val restored=PlannerDocuments.read(context,uri,false,charArrayOf()).entries.single()
   assertFalse(StructuredFields.read(restored.extraData).has("photo"))
   assertEquals("Synthetic shop",StructuredFields.read(restored.extraData).getString("source"))
   val draft=with(EntryStateSaver) {(object:androidx.compose.runtime.saveable.SaverScope {override fun canBeSaved(value:Any)=true}).save(entry)}!!
   assertEquals(entry.extraData,EntryStateSaver.restore(draft)!!.extraData)
  } finally {context.contentResolver.delete(uri,null,null)}
 }
 private val instrumentation=InstrumentationRegistry.getInstrumentation()
 private val context=instrumentation.targetContext
 private fun create():Uri = context.contentResolver.insert(Uri.parse("content://"+instrumentation.context.packageName+".documents"),android.content.ContentValues())!!
 private fun bytes(uri:Uri)=context.contentResolver.openInputStream(uri)!!.use {it.readBytes()}

 @Test fun encryptedProviderRoundTripPreservesOnlySelectedHistoryAndRecords() {
  val uri=create();val password="provider test backup password".toCharArray()
  val entry=Entry(kind="reminder",title="Synthetic review")
  val contact=Entry(kind="contact",title="Excluded synthetic contact")
  val history=listOf(entry,contact).map {ReviewEvent(entryId=it.id,title=it.title,completedOn="2026-10-30")}
  try {
   PlannerDocuments.write(context,uri,BackupSnapshot(listOf(entry,contact),history),"ptb",ExportPolicy.Options(),true,password)
   assertEquals("PTB1",bytes(uri).take(4).toByteArray().toString(Charsets.US_ASCII))
   val restored=PlannerDocuments.read(context,uri,true,password)
   assertEquals(listOf(entry.title),restored.entries.map {it.title})
   assertEquals(1,restored.reviews.size)
   assertEquals(restored.entries.single().id,restored.reviews.single().entryId)
   assertTrue(runCatching {PlannerDocuments.read(context,uri,true,"wrong password".toCharArray())}.isFailure)
  } finally {password.fill('\u0000');context.contentResolver.delete(uri,null,null)}
 }

 @Test fun reportsAndJsonUseProviderStreamsAndBadFormatDoesNotTruncate() {
  val uri=create();val snapshot=BackupSnapshot(listOf(Entry(kind="supply",title="=synthetic",quantity=3.0)))
  try {
   val privateFile=java.io.File.createTempFile("qa-uri-",".tmp",context.cacheDir)
   try {
    privateFile.writeText("Synthetic existing file")
    val fileUri=Uri.fromFile(privateFile)
    assertTrue(runCatching {PlannerDocuments.write(context,fileUri,snapshot,"json",ExportPolicy.Options(),false,charArrayOf())}.isFailure)
    assertEquals("Synthetic existing file",privateFile.readText())
    assertTrue(runCatching {PlannerDocuments.read(context,fileUri,false,charArrayOf())}.isFailure)
   } finally {privateFile.delete()}
   PlannerDocuments.write(context,uri,snapshot,"csv",ExportPolicy.Options(),false,charArrayOf())
   assertTrue(bytes(uri).toString(Charsets.UTF_8).contains("'=synthetic"))
   val previous=bytes(uri)
   assertTrue(runCatching {PlannerDocuments.write(context,uri,snapshot,"invalid",ExportPolicy.Options(),false,charArrayOf())}.isFailure)
   assertArrayEquals(previous,bytes(uri))
   PlannerDocuments.write(context,uri,snapshot,"json",ExportPolicy.Options(),false,charArrayOf())
   assertEquals(3.0,PlannerDocuments.read(context,uri,false,charArrayOf()).entries.single().quantity,0.0)
   PlannerDocuments.write(context,uri,snapshot,"pdf",ExportPolicy.Options(),false,charArrayOf())
   context.contentResolver.openFileDescriptor(uri,"r")!!.use {descriptor->PdfRenderer(descriptor).use {assertTrue(it.pageCount>0)}}
  } finally {context.contentResolver.delete(uri,null,null)}
 }

 @Test fun oversizedAndMissingDocumentsAreRejected() {
  val uri=create()
  try {
   context.contentResolver.openOutputStream(uri,"wt")!!.use {it.write(ByteArray(BackupCrypto.MAX_BYTES+1))}
   assertTrue(runCatching {PlannerDocuments.read(context,uri,false,charArrayOf())}.isFailure)
  } finally {context.contentResolver.delete(uri,null,null)}
  assertTrue(runCatching {PlannerDocuments.read(context,uri,false,charArrayOf())}.isFailure)
 }
}
