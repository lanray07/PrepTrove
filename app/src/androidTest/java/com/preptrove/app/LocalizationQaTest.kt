package com.preptrove.app

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Isolated draft-language QA. English safety text and a visible banner prevent marketing reuse. */
class LocalizationQaTest {
 @get:Rule val rule=createEmptyComposeRule()
 @Test fun resourceRoundTripAndExpandedLayoutsAcrossAllTargets()=runBlocking {
  assumeTrue(BuildConfig.LOCALIZATION_QA)
  val instrumentation=InstrumentationRegistry.getInstrumentation()
  val context=instrumentation.targetContext
  require(context.packageName=="com.preptrove.app.localizationqa")
  assertFalse(BuildConfig.AMAZON_PURCHASES_ENABLED)
  val expectations=JSONObject(context.assets.open("qa-expectations.json").bufferedReader().use {it.readText()})
  val app=context.applicationContext as PrepTrove
  val dao=app.db.entries()
  val output=File(context.getExternalFilesDir(null),"localization-qa").apply {mkdirs()}
  val results=JSONObject()
  for(locale in AppLanguages.targets) {
   AppLanguages.select(context,locale)
   val localized=AppLanguages.wrap(context)
   assertEquals(locale,localized.resources.configuration.locales[0].toLanguageTag())
   assertEquals(if(locale=="ar") View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR,localized.resources.configuration.layoutDirection)
   val expected=expectations.getJSONObject(locale)
   val strings=expected.getJSONObject("strings")
   val keys=strings.keys()
   var checked=0
   while(keys.hasNext()) {
    val key=keys.next()
    val id=localized.resources.getIdentifier(key,"string",context.packageName)
    assertTrue("$locale missing $key",id!=0)
    assertEquals("$locale $key",strings.getString(key),localized.getString(id))
    checked++
   }
   val plurals=expected.getJSONObject("plurals")
   val names=plurals.keys()
   while(names.hasNext()) {
    val key=names.next();val forms=plurals.getJSONObject(key)
    val id=localized.resources.getIdentifier(key,"plurals",context.packageName)
    assertTrue(id!=0)
    val values=forms.keys().asSequence().map {forms.getString(it)}.toSet()
    for(quantity in listOf(0,1,2,3,5,11,101,1000000)) {
     val actual=localized.resources.getQuantityString(id,quantity)
     assertTrue("$locale $key $quantity",actual in values)
     if(locale=="ar") {
      val form=when(quantity) {0->"zero";1->"one";2->"two";3,5->"few";11->"many";else->"other"}
      assertEquals("$locale $key $quantity",forms.getString(form),actual)
     }
     if(locale=="pl-PL") {
      val form=when(quantity) {1->"one";2,3->"few";else->"many"}
      assertEquals("$locale $key $quantity",forms.getString(form),actual)
     }
    }
   }
   context.preferences.edit {it[booleanPreferencesKey("onboarded")]=true}
   dao.clear();dao.clearReviews()
   dao.save(Entry(kind="supply",title=localized.getString(R.string.kind_supply)+" QA",quantity=2.5,unit="L",expiry=java.time.LocalDate.now().plusDays(1).toString(),location="QA",category=localized.getString(R.string.default_category_other)))
   ActivityScenario.launch(MainActivity::class.java).use {scenario->
    scenario.onActivity {it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)}
    rule.waitUntil(15000) {rule.onAllNodesWithText(LOCALIZATION_QA_NOTICE).fetchSemanticsNodes().isNotEmpty()}
    val folder=File(output,locale).apply {mkdirs()}
    fun capture(name:String) {
     rule.waitForIdle()
     val bitmap=instrumentation.uiAutomation.takeScreenshot()
     File(folder,"$name.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
     bitmap.recycle()
    }
    fun section(id:Int) {rule.onNode(hasText(localized.getString(id)) and hasClickAction()).performScrollTo().performClick();rule.waitForIdle()}
    capture("01-overview")
    section(R.string.nav_voice)
    rule.onNodeWithText(localized.getString(R.string.transcript_or_typed_command_48008f),substring=false).performTextInput(expected.getString("typedExpiryCommand"))
    androidx.test.espresso.Espresso.closeSoftKeyboard()
    rule.onNodeWithText(localized.getString(R.string.review_command_399b62),substring=false).performScrollTo().performClick()
    val answer=localized.resources.getQuantityString(R.plurals.supplies_attention,1,1)
    rule.onNodeWithText(answer,substring=false).performScrollTo().assertIsDisplayed()
    capture("04-typed-voice")
    if(!android.speech.SpeechRecognizer.isRecognitionAvailable(context)) {
     rule.onNodeWithText(localized.getString(R.string.msg_4c15103c8011)).performScrollTo().performClick()
     rule.onNodeWithText(localized.getString(R.string.msg_79f24e0d59e2)).performScrollTo().assertIsDisplayed()
    }
    scenario.onActivity {GuidanceDirectory.select(it,"GB")}
    // Re-enter to render a freshly persisted manual directory choice.
    section(R.string.nav_reference)
    rule.onNodeWithTag("guidance-country").performClick()
    val country=java.util.Locale("","GB").getDisplayCountry(java.util.Locale.forLanguageTag(locale))
    rule.onNode(hasText(country) and hasAnyAncestor(isDialog())).performScrollTo().performClick()
     rule.onNodeWithText("GOV.UK · Be Ready",substring=true).performScrollTo().assertIsDisplayed()
    capture("05-official-links")
    section(R.string.nav_supplies)
    rule.onNodeWithContentDescription(localized.getString(R.string.msg_950eea0391cc)).performClick()
    rule.onNodeWithText(localized.getString(R.string.save_1509f5),substring=false).performClick()
    rule.onNodeWithText(localized.getString(R.string.msg_099f9079ba79),substring=false).performScrollTo().assertIsDisplayed()
    capture("02-editor-error")
    rule.onNodeWithText(localized.getString(R.string.cancel_19766e),substring=false).performClick()
    scenario.onActivity {activity->activity.setContent {
     val density=LocalDensity.current
     CompositionLocalProvider(LocalDensity provides Density(density.density,2f)) {activity.Planner()}
    }}
    rule.waitForIdle()
    section(R.string.nav_settings)
    rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(localized.getString(R.string.no_account_ads_analytics_location_tracking_or_au_75a32c)))
    capture("03-settings-200percent")
    scenario.onActivity {it.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)}
   }
   results.put(locale,JSONObject().put("stringResourcesChecked",checked).put("captures",5).put("typedCommandPassed",true).put("fontScaleStress",2.0).put("safetyTranslationApproved",false))
  }
  File(output,"results.json").writeText(JSONObject().put("kind","qa_only_not_store").put("locales",results).toString(2))
 }
}
