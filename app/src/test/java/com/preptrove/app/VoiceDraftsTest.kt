package com.preptrove.app
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
class VoiceDraftsTest {
 @Test fun reminderDraftUsesCalendarMonthAndRequiresSupportedPhrase() {
  val draft=VoiceDrafts.reminder("Remind me to check the kit next month",LocalDate.of(2027,1,31))!!
  assertEquals("check the kit",draft.title);assertEquals("2027-02-28",draft.expiry)
  assertNull(VoiceDrafts.reminder("remind me tomorrow"))
 }
 @Test fun inspectionNeverSelectsTemplatesOrAmbiguousNamesAutomatically() {
  val entry=Entry(kind="supply",title="Batteries")
  assertEquals(1,VoiceDrafts.checkedSupply("Mark batteries as checked",listOf(entry,entry.copy(isTemplate=true)))!!.size)
  assertEquals(2,VoiceDrafts.checkedSupply("Mark batteries as checked",listOf(entry,entry.copy(id="second")))!!.size)
  assertNull(VoiceDrafts.checkedSupply("delete batteries",listOf(entry)))
 }
 @Test fun quantityUnderstandsRegionalLightNamesWithoutAddingMixedUnits() {
  val rows=listOf(Entry(kind="supply",title="Emergency torch",quantity=2.0),Entry(kind="supply",title="Car flashlight",quantity=1.0))
  val count=VoiceDrafts.quantity("How many torches do I have?",rows)!!
  assertEquals(3.0,count.total,0.0);assertTrue(count.combinable)
  assertFalse(VoiceDrafts.quantity("How many torches do I have?",rows+Entry(kind="supply",title="Torches",quantity=2.0,unit="packs"))!!.combinable)
 }
}
