package com.preptrove.app
import java.util.Locale
import org.junit.Test
import org.junit.Assert.*
class LocalNumbersTest {
 @Test fun decimalSeparatorsFollowLocale() {
  assertEquals(1.5,LocalNumbers.parse("1,5",Locale.GERMANY)!!,0.0)
  assertEquals(1.5,LocalNumbers.parse("1.5",Locale.US)!!,0.0)
  assertNull(LocalNumbers.parse("1,5",Locale.US))
  assertEquals("1,5",LocalNumbers.input(1.5,Locale.GERMANY))
 }
 @Test fun rejectsPartialNegativeAndNonfiniteValues() {
  listOf("1 item","-1","NaN","", "Infinity").forEach {assertNull(LocalNumbers.parse(it,Locale.UK))}
 }
}
