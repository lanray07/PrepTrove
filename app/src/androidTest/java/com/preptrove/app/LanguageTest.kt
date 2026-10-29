package com.preptrove.app
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.Assert.*
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
 @Test fun pendingPacksCannotBeSelected() {assertTrue(runCatching {AppLanguages.select(context,"fr-FR")}.isFailure)}
 @Test fun remindersUseSingularAndPluralResources() {
  val resources=AppLanguages.wrap(context).resources
  assertTrue(resources.getQuantityString(R.plurals.reminders_due,1,1).contains("1 saved reminder is"))
  assertTrue(resources.getQuantityString(R.plurals.reminders_due,2,2).contains("2 saved reminders are"))
 }
}
