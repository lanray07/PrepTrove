package com.preptrove.app
import org.junit.Test
import org.junit.Assert.*
class BackupCryptoTest {
 @Test fun roundTripIsAuthenticatedAndRandomised() {
  val input="Household plan with private contacts".toByteArray();val password="a long test password".toCharArray()
  val first=BackupCrypto.encrypt(input,password);val second=BackupCrypto.encrypt(input,password)
  assertFalse(first.contentEquals(second));assertArrayEquals(input,BackupCrypto.decrypt(first,password))
  first[first.lastIndex]=(first.last().toInt() xor 1).toByte()
  assertTrue(runCatching {BackupCrypto.decrypt(first,password)}.isFailure)
  assertTrue(runCatching {BackupCrypto.decrypt(second,"wrong password".toCharArray())}.isFailure)
 }
 @Test fun rejectsWeakPasswordsAndInvalidContainers() {
  assertTrue(runCatching {BackupCrypto.encrypt(byteArrayOf(1),"short".toCharArray())}.isFailure)
  assertTrue(runCatching {BackupCrypto.decrypt(ByteArray(47),"password".toCharArray())}.isFailure)
 }
}
