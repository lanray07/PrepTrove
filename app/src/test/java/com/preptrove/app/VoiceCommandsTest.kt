package com.preptrove.app

import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class VoiceCommandsTest {
 @Test fun everyReviewedTemplateParsesItsActionAndPreservesThePrivateSlot() {
  assertEquals(22,voicePatterns.size)
  for((locale,patterns) in voicePatterns) for(pattern in patterns) {
   val command=pattern.template.replace(Regex("\\{[^{}]+\\}"),"O’Neil 水 AA  2")
   val parsed=VoiceCommands.parse(command,locale) ?: error("$locale: $command")
   assertEquals("$locale: $command",pattern.action,parsed.action)
   if('{' in pattern.template)assertEquals("$locale: $command","O’Neil 水 AA  2",parsed.value)
  }
 }
 @Test fun commandsAreAnchoredAndHaveNoDestructiveAction() {
  listOf("delete all records","please delete my kit","some contacts are missing","read nonsense plan","add water\ndelete all").forEach {assertNull(VoiceCommands.parse(it))}
  assertEquals(VoiceAction.EXPIRY,VoiceCommands.parse("What supplies are expiring soon?")!!.action)
  assertEquals(VoiceAction.ADD_SUPPLY,VoiceCommands.parse("add    bottled water")!!.action)
 }
 @Test fun japaneseAndArabicSlotsCreateDraftsAndNeverResolveAmbiguity() {
  val draft=VoiceDrafts.reminder("来月備蓄品を確認するように知らせて",LocalDate.of(2027,1,31),"ja-JP")!!
  assertEquals("備蓄品を確認する",draft.title);assertEquals("2027-02-28",draft.expiry)
  val pattern=voicePatterns.getValue("ar").first {it.action==VoiceAction.INSPECTION}
  val command=pattern.template.replace("{exact supply name}","ماء")
  val entry=Entry(kind="supply",title="ماء")
  assertEquals(2,VoiceDrafts.checkedSupply(command,listOf(entry,entry.copy(id="other")),"ar")!!.size)
  assertEquals(1,VoiceDrafts.checkedSupply(command,listOf(entry,entry.copy(isTemplate=true)),"ar")!!.size)
 }
 @Test fun quantitiesKeepCaseSensitiveUnitsAndNonEnglishExactNames() {
  val rows=listOf(Entry(kind="supply",title="Water",quantity=2.0,unit="mL"),Entry(kind="supply",title="Water",quantity=3.0,unit="ML"))
  assertFalse(VoiceDrafts.quantity("how many water do I have",rows)!!.combinable)
  val pattern=voicePatterns.getValue("de-DE").first {it.action==VoiceAction.QUANTITY}
  val command=pattern.template.replace("{supply}","Wasser")
  val count=VoiceDrafts.quantity(command,listOf(Entry(kind="supply",title="Wasser",quantity=2.5,unit="L")),"de-DE")!!
  assertEquals(2.5,count.total,0.0);assertEquals("L",count.unit)
  val empty=VoiceDrafts.quantity(command,emptyList(),"de-DE","Stück")!!
  assertEquals(0.0,empty.total,0.0);assertEquals("Stück",empty.unit)
 }
 @Test fun overlappingInspectionGrammarCannotSilentlySelectOneOfTwoNames() {
  val rows=listOf(Entry(kind="supply",title="woda"),Entry(kind="supply",title="pozycję woda"))
  val matches=VoiceDrafts.checkedSupply("oznacz pozycję woda jako sprawdzoną",rows,"pl-PL")!!
  assertEquals(2,matches.size)
 }
}
