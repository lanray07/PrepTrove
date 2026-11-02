package com.preptrove.app
import java.time.LocalDate
object VoiceDrafts {
 fun reminder(command:String,today:LocalDate=LocalDate.now(),localeTag:String="en-GB"):Entry? {
  val parsed=VoiceCommands.parse(command,localeTag)?.takeIf {it.action==VoiceAction.REMINDER} ?: return null
  return Entry(kind="reminder",title=parsed.value,expiry=today.plusMonths(1).toString())
 }
 fun checkedSupply(command:String,records:List<Entry>,localeTag:String="en-GB"):List<Entry>? {
  val parsed=VoiceCommands.candidates(command,localeTag).filter {it.action==VoiceAction.INSPECTION}
  if(parsed.isEmpty())return null
  return records.filter {entry->entry.kind=="supply" && !entry.isTemplate && parsed.any {candidate->
   val locale=java.util.Locale.forLanguageTag(candidate.patternLocale)
   entry.title.lowercase(locale)==candidate.value.lowercase(locale)
  }}
 }
 data class Quantity(val total:Double,val unit:String,val phrase:String,val combinable:Boolean)
 fun quantity(command:String,records:List<Entry>,localeTag:String="en-GB",defaultUnit:String="items"):Quantity? {
  val parsed=VoiceCommands.parse(command,localeTag)?.takeIf {it.action==VoiceAction.QUANTITY} ?: return null
  val phrase=parsed.value
  fun words(text:String):Set<String> = text.lowercase(java.util.Locale.ROOT).split(Regex("[^\\p{L}\\p{N}]+" )).filter {it.isNotBlank()}.map {when(it) {
   "torches","flashlight","flashlights"->"torch";"batteries"->"battery";"bottles"->"bottle";"banks"->"bank";"tins"->"tin";else->it
  }}.toSet()
  val target=words(phrase)
  if(target.isEmpty())return null
  val locale=java.util.Locale.forLanguageTag(parsed.patternLocale)
  val matches=records.filter {it.kind=="supply" && !it.isTemplate && if(locale.language=="en") words(it.title).containsAll(target) else it.title.lowercase(locale)==phrase.lowercase(locale)}
  val units=matches.map {it.unit.trim()}.distinct()
  val total=matches.sumOf {it.quantity}
  return Quantity(total,units.firstOrNull() ?: defaultUnit,phrase,units.size<=1 && total.isFinite())
 }
}
