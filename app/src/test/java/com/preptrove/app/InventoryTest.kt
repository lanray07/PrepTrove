package com.preptrove.app
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
class InventoryTest {
 @Test fun expiryBoundaryIncludesExpiredAndThirtyDaysButIgnoresUndated() {
  val today=LocalDate.of(2026,10,28)
  val data=listOf(Entry(kind="supply",title="Old",expiry="2026-10-01"),Entry(kind="supply",title="Boundary",expiry="2026-11-27"),Entry(kind="supply",title="Later",expiry="2026-11-28"),Entry(kind="supply",title="Undated"),Entry(kind="supply",title="Bad date",expiry="bad"),Entry(kind="reminder",title="Reminder",expiry="2026-10-01"))
  assertEquals(listOf("Old","Boundary"),Inventory.attention(data,today).map {it.title})
 }
 @Test fun csvEscapesQuotesNewlinesAndFormulaPrefixes() {
  val result=Inventory.csv(listOf(Entry(kind="supply",title="=HYPERLINK(1)",location="Shelf \"A\"\nB"),Entry(kind="contact",title="Private contact")))
  assertTrue(result.contains("'=HYPERLINK"));assertFalse(result.contains("Private contact"))
 }
}
