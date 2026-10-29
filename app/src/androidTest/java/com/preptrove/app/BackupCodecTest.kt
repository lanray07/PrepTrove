package com.preptrove.app
import org.junit.Test
import org.junit.Assert.*
class BackupCodecTest {
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
