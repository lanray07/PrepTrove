package com.preptrove.app
import java.time.LocalDate
object VoiceDrafts {
 fun reminder(command:String,today:LocalDate=LocalDate.now()):Entry? {
  val match=Regex("^remind me to (.+) next month[.!]?$",RegexOption.IGNORE_CASE).matchEntire(command.trim()) ?: return null
  return Entry(kind="reminder",title=match.groupValues[1].trim(),expiry=today.plusMonths(1).toString())
 }
 fun checkedSupply(command:String,records:List<Entry>):List<Entry>? {
  val match=Regex("^mark (.+) as checked[.!]?$",RegexOption.IGNORE_CASE).matchEntire(command.trim()) ?: return null
  return records.filter {it.kind=="supply" && !it.isTemplate && it.title.equals(match.groupValues[1].trim(),true)}
 }
 data class Quantity(val total:Double,val unit:String,val phrase:String,val combinable:Boolean)
 fun quantity(command:String,records:List<Entry>):Quantity? {
  val match=Regex("^how many (.+?)(?: do i have)?[?.!]?$",RegexOption.IGNORE_CASE).matchEntire(command.trim()) ?: return null
  val phrase=match.groupValues[1].trim()
  fun words(text:String):Set<String> = text.lowercase(java.util.Locale.ROOT).split(Regex("[^\\p{L}\\p{N}]+" )).filter {it.isNotBlank()}.map {when(it) {
   "torches","flashlight","flashlights"->"torch";"batteries"->"battery";"bottles"->"bottle";"banks"->"bank";"tins"->"tin";else->it
  }}.toSet()
  val target=words(phrase);val matches=records.filter {it.kind=="supply" && !it.isTemplate && words(it.title).containsAll(target)}
  val units=matches.map {it.unit.trim().lowercase(java.util.Locale.ROOT)}.distinct()
  val total=matches.sumOf {it.quantity}
  return Quantity(total,units.firstOrNull() ?: "items",phrase,units.size<=1 && total.isFinite())
 }
}
