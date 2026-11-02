package com.preptrove.app

import java.util.Locale
import org.junit.Assert.*
import org.junit.Test

class LanguageResolverTest {
 private fun resolve(vararg tags:String, packs:List<String> = listOf("en-GB","en-US","en-AU"))=
  LanguageResolver.resolve(tags.map(Locale::forLanguageTag),packs).toLanguageTag()
 @Test fun followsDevicePreferenceOrderAndUsesCompleteEnglishFallback() {
  assertEquals("en-US",resolve("ar-EG","en-US","en-GB"))
  assertEquals("en-GB",resolve("ar-EG"))
  assertEquals("en-GB",resolve())
  assertEquals("en-GB",resolve("en-NZ","en-US"))
 }
 @Test fun matchesRegionalLanguagesWithoutCrossingChineseScripts() {
  val packs=listOf("en-GB","fr-FR","zh-Hans","zh-Hant")
  assertEquals("fr-FR",resolve("fr-CH",packs=packs))
  assertEquals("zh-Hant",resolve("zh-TW",packs=packs))
  assertEquals("zh-Hant",resolve("zh-HK",packs=packs))
  assertEquals("zh-Hans",resolve("zh-CN",packs=packs))
  assertEquals("en-GB",resolve("zh-TW",packs=listOf("en-GB","zh-Hans")))
 }
}
