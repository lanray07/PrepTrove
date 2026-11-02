package com.preptrove.app

import java.util.Locale

/** Select a complete resource pack, respecting device preference order and Chinese scripts. */
internal object LanguageResolver {
 fun resolve(requested:List<Locale>, available:List<String>, fallback:String="en-GB"):Locale {
  require(fallback in available)
  val packs=available.map(Locale::forLanguageTag)
  for (wanted in requested) {
   packs.firstOrNull {it.toLanguageTag().equals(wanted.toLanguageTag(),ignoreCase=true)}?.let {return it}
   packs.firstOrNull {it.language==wanted.language && script(it)==script(wanted)}?.let {return it}
  }
  return Locale.forLanguageTag(fallback)
 }
 private fun script(locale:Locale):String = if(locale.language=="zh") {
  locale.script.ifEmpty {if(locale.country in listOf("TW","HK","MO")) "Hant" else "Hans"}
 } else locale.script
}
