package com.preptrove.app
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.Assert.*
import android.content.res.Configuration
import android.os.LocaleList
import android.view.View
import java.util.Locale
class LanguageTest {
 private val context get()=InstrumentationRegistry.getInstrumentation().targetContext
 @Test fun selectionPersistsAndRegionalResourcesLoad() {
  val prefs=context.getSharedPreferences("app-language",0);val original=prefs.getString("tag","") ?: ""
  try {
   AppLanguages.select(context,"en-US")
   assertEquals("en-US",AppLanguages.wrap(context).resources.configuration.locales[0].toLanguageTag())
   assertEquals("Organize your kit",AppLanguages.wrap(context).getString(R.string.organise_your_kit_e4829c))
   AppLanguages.select(context,"en-GB")
   assertEquals("Organise your kit",AppLanguages.wrap(context).getString(R.string.organise_your_kit_e4829c))
  } finally {prefs.edit().putString("tag",original).commit()}
 }
 @Test fun unknownPacksCannotBeSelected() {assertTrue(runCatching {AppLanguages.select(context,"xx-YY")}.isFailure)}
 @Test fun devicePreferenceKeepsArabicResourcesSpeechAndDirectionTogether() {
  val prefs=context.getSharedPreferences("app-language",0);val original=prefs.getString("tag","") ?: ""
  try {
   AppLanguages.select(context,"")
   val config=Configuration(context.resources.configuration)
   config.setLocales(LocaleList(Locale.forLanguageTag("ar-EG"),Locale.US))
   config.setLayoutDirection(Locale.forLanguageTag("ar-EG"))
   val wrapped=AppLanguages.wrap(context.createConfigurationContext(config))
   assertEquals("ar",wrapped.resources.configuration.locales[0].toLanguageTag())
   assertEquals(View.LAYOUT_DIRECTION_RTL,wrapped.resources.configuration.layoutDirection)
   assertNotEquals("Organize your kit",wrapped.getString(R.string.organise_your_kit_e4829c))
   // A once-approved pack may be withdrawn by a later build. Never restore it
   // just because an old preference still exists on the device.
   prefs.edit().putString("tag","xx-YY").commit()
   assertEquals("ar",AppLanguages.wrap(context.createConfigurationContext(config)).resources.configuration.locales[0].toLanguageTag())
  } finally {prefs.edit().putString("tag",original).commit()}
 }
 @Test fun remindersUseSingularAndPluralResources() {
  val resources=AppLanguages.wrap(context).resources
  assertTrue(resources.getQuantityString(R.plurals.reminders_due,1,1).contains("1 saved reminder is"))
  assertTrue(resources.getQuantityString(R.plurals.reminders_due,2,2).contains("2 saved reminders are"))
 }
}
