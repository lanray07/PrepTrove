package com.preptrove.app

import java.util.Locale

internal enum class VoiceAction { ADD_SUPPLY, EXPIRY, CONTACTS, PLANS, CHECKLISTS, QUANTITY, INSPECTION, REMINDER }
internal data class VoicePattern(val action:VoiceAction,val template:String)
internal data class VoiceCommand(val action:VoiceAction,val value:String="",val patternLocale:String="en-GB")

internal object VoiceCommands {
 private val slot=Regex("\\{[^{}]+\\}")
 private fun literal(text:String):String {
  val result=StringBuilder();var start=0
  for(match in Regex("\\s+|['’]").findAll(text)) {
   result.append(Regex.escape(text.substring(start,match.range.first)))
   result.append(if(match.value.first().isWhitespace()) "\\s+" else "['’]")
   start=match.range.last+1
  }
  return result.append(Regex.escape(text.substring(start))).toString()
 }
 fun parse(command:String,localeTag:String="en-GB"):VoiceCommand? = candidates(command,localeTag).firstOrNull()
 fun candidates(command:String,localeTag:String="en-GB"):List<VoiceCommand> {
  val text=command.trim()
  if(text.isEmpty() || text.length>1000 || '\n' in text || '\r' in text)return emptyList()
  val selected=LanguageResolver.resolve(listOf(Locale.forLanguageTag(localeTag)),voicePatterns.keys.toList()).toLanguageTag()
  val results=mutableListOf<VoiceCommand>()
  for(locale in listOf(selected,"en-GB").distinct()) for(pattern in voicePatterns.getValue(locale).sortedByDescending {slot.replace(it.template,"").length}) {
   val match=slot.find(pattern.template)
   val body=if(match==null) literal(pattern.template) else literal(pattern.template.substring(0,match.range.first))+"(.+?)"+literal(pattern.template.substring(match.range.last+1))
   val found=Regex("^"+body+"\\s*[.!?؟。！]*$",RegexOption.IGNORE_CASE).matchEntire(text) ?: continue
   val value=if(match==null) "" else found.groupValues[1].trim()
   if(match!=null && (value.isBlank() || value.length>500))continue
   results.add(VoiceCommand(pattern.action,value,locale))
  }
  return results.distinct()
 }
}
