package com.preptrove.app
import android.content.Context
import android.content.res.Configuration
import java.util.Locale
object AppLanguages {
 val targets=listOf("en-GB","en-US","en-AU","es-ES","es-419","fr-FR","fr-CA","de-DE","it-IT","pt-BR","pt-PT","nl-NL","pl-PL","sv-SE","ja-JP","hi-IN","ar","tr-TR","id-ID","ko-KR","zh-Hans","zh-Hant")
 val available get()=approvedResourceLocales
 fun select(context:Context,tag:String) {require(tag.isEmpty() || tag in available);context.getSharedPreferences("app-language",Context.MODE_PRIVATE).edit().putString("tag",tag).commit()}
 fun wrap(base:Context):Context {
  val tag=base.getSharedPreferences("app-language",Context.MODE_PRIVATE).getString("tag","").orEmpty()
  if(tag.isEmpty()) return base
  val locale=Locale.forLanguageTag(tag);val config=Configuration(base.resources.configuration);config.setLocale(locale);config.setLayoutDirection(locale)
  return base.createConfigurationContext(config)
 }
}
fun kindLabel(context:Context,kind:String):String=context.getString(when(kind) {
 "kit"->R.string.kind_kit;"supply"->R.string.kind_supply;"plan"->R.string.kind_plan;"contact"->R.string.kind_contact;"check"->R.string.kind_check;else->R.string.kind_reminder
})
