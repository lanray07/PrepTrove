package com.preptrove.app
import org.junit.Test
import org.junit.Assert.*
class BackupCodecTest {
 @Test fun historyIsExplicitScopedAndRemappedWithRestoredRecords() {
  val entry=Entry(kind="reminder",title="Review kit")
  val excluded=Entry(kind="contact",title="Private contact")
  val review=ReviewEvent(entryId=entry.id,title=entry.title,completedOn="2026-10-30")
  val privateReview=ReviewEvent(entryId=excluded.id,title=excluded.title,completedOn="2026-10-30")
  val defaultBackup=BackupCodec.decodeSnapshotCopies(BackupCodec.encode(listOf(entry)))
  assertTrue(defaultBackup.reviews.isEmpty())
  val bytes=BackupCodec.encode(listOf(entry),listOf(review,privateReview))
  val password="test history backup password".toCharArray()
  val encrypted=BackupCrypto.encrypt(bytes,password)
  val restored=BackupCodec.decodeSnapshotCopies(BackupCrypto.decrypt(encrypted,password))
  password.fill('\u0000')
  assertEquals(1,restored.entries.size);assertEquals(1,restored.reviews.size)
  assertEquals(restored.entries.single().id,restored.reviews.single().entryId)
  assertNotEquals(entry.id,restored.entries.single().id)
  assertNotEquals(review.id,restored.reviews.single().id)
  assertFalse(bytes.toString(Charsets.UTF_8).contains(excluded.title))
  val again=BackupCodec.decodeSnapshotCopies(bytes)
  assertNotEquals(restored.reviews.single().id,again.reviews.single().id)
 }
 @Test fun rejectsInvalidHistoryAndBackupsTooLargeToRestore() {
  val entry=Entry(kind="reminder",title="Review kit")
  val review=ReviewEvent(entryId=entry.id,title=entry.title,completedOn="2026-10-30")
  val root=org.json.JSONObject(BackupCodec.encode(listOf(entry),listOf(review)).toString(Charsets.UTF_8))
  root.getJSONArray("reviews").getJSONObject(0).put("entryId","missing")
  assertTrue(runCatching {BackupCodec.decodeSnapshotCopies(root.toString().toByteArray())}.isFailure)
  root.getJSONArray("reviews").getJSONObject(0).put("entryId",entry.id).put("completedOn","not-a-date")
  assertTrue(runCatching {BackupCodec.decodeSnapshotCopies(root.toString().toByteArray())}.isFailure)
  assertTrue(runCatching {BackupCodec.encode(listOf(entry.copy(detail="x".repeat(BackupCrypto.MAX_BYTES))))}.isFailure)
 }
 @Test fun restoreCopiesKeepKitRelationshipsWithNewIds() {
  val kit=Entry(kind="kit",title="Home");val supply=Entry(kind="supply",title="Water",kitId=kit.id,quantity=3.0)
  val restored=BackupCodec.decodeCopies(BackupCodec.encode(listOf(kit,supply)))
  assertEquals(2,restored.size);assertNotEquals(kit.id,restored[0].id);assertEquals(restored[0].id,restored[1].kitId);assertEquals(3.0,restored[1].quantity,0.0)
 }
 @Test fun rejectsMalformedRecordsBeforeAnyDatabaseWrites() {
  val duplicate=Entry(kind="supply",title="Water")
  assertTrue(runCatching {BackupCodec.decodeCopies(BackupCodec.encode(listOf(duplicate,duplicate)))}.isFailure)
  assertTrue(runCatching {BackupCodec.decodeCopies(BackupCodec.encode(listOf(duplicate.copy(expiry="not a date"))))}.isFailure)
  assertTrue(runCatching {BackupCodec.decodeCopies("{\"schemaVersion\":2,\"records\":[]}".toByteArray())}.isFailure)
 }
}
