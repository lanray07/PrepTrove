package com.preptrove.app
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.datastore.preferences.core.*
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import java.io.File
class StoreScreenshotsTest {
 @get:Rule val rule=createEmptyComposeRule()
 @Test fun captureLocalisedDemoScreens()=runBlocking {
  assumeTrue(BuildConfig.BUILD_TYPE=="screenshots")
  val instrumentation=InstrumentationRegistry.getInstrumentation();val context=instrumentation.targetContext
  require(context.packageName.endsWith(".storecapture"))
  val application=context.applicationContext as PrepTrove;val dao=application.db.entries()
  for(locale in approvedResourceLocales) {
   val fixture=org.json.JSONObject(instrumentation.context.assets.open("store-fixtures/"+locale+".json").bufferedReader().use {it.readText()})
   require(fixture.getString("locale")==locale)
   AppLanguages.select(context,locale)
   val localized=AppLanguages.wrap(context)
   fun label(id:Int)=localized.getString(id)
   context.preferences.edit {it[booleanPreferencesKey("onboarded")]=true;it[booleanPreferencesKey("darkTheme")]=false}
   dao.clear();dao.clearReviews()
   val kit=Entry(id="example-kit",kind="kit",title=fixture.getString("kitTitle"),location=fixture.getString("kitLocation"),detail=fixture.getString("kitDetail"))
   dao.save(kit)
   val supplies=fixture.getJSONArray("supplies")
   val today=java.time.LocalDate.now()
   val quantities=listOf(6.0,2.0,12.0,8.0,1.0,2.0)
   for(index in 0 until supplies.length()) dao.save(Entry(kind="supply",title=supplies.getString(index),quantity=quantities[index],unit=fixture.getString(when(index){0->"unitBottles";3->"unitTins";else->"unitItems"}),category=fixture.getString("category"),expiry=when(index){0->today.plusDays(13).toString();3->today.plusDays(16).toString();else->""},kitId=kit.id,location=kit.location))
   val details=org.json.JSONObject()
   for(key in listOf("members","meeting","communication","pets","checkIn"))details.put(key,fixture.getString(key))
   dao.save(Entry(kind="plan",title=fixture.getString("planTitle"),detail=fixture.getString("planDetail"),extraData=details.toString()))
   dao.save(Entry(kind="contact",title=fixture.getString("contactTitle"),detail=fixture.getString("contactDetail"),extraData=org.json.JSONObject().put("relationship",fixture.getString("relationship")).toString()))
   val checks=fixture.getJSONArray("checkTitles")
   for(index in 0 until checks.length())dao.save(Entry(kind="check",title=checks.getString(index),category=fixture.getString("checkGroup")))
   dao.save(Entry(kind="reminder",title=fixture.getString("reminderKit"),expiry=today.plusDays(13).toString(),intervalDays=30))
   dao.save(Entry(kind="reminder",title=fixture.getString("reminderBatteries"),expiry=today.plusDays(18).toString(),intervalDays=60))
   ActivityScenario.launch(MainActivity::class.java).use {scenario->
    scenario.onActivity {it.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)}
    rule.waitUntil(15000) {runCatching {rule.onAllNodesWithText(java.text.NumberFormat.getIntegerInstance(java.util.Locale.forLanguageTag(locale)).format(6),substring=false).fetchSemanticsNodes().isNotEmpty()}.getOrDefault(false)}
    val folder=File(context.getExternalFilesDir("store"),locale).apply {mkdirs()}
    val marketing=File(context.getExternalFilesDir("marketing"),locale).apply {mkdirs()}
    var panelIndex=0
    fun capture(name:String) {
     rule.waitForIdle()
     val content=android.graphics.Rect()
     scenario.onActivity {it.window.decorView.getWindowVisibleDisplayFrame(content)}
     val bitmap=instrumentation.uiAutomation.takeScreenshot()
     File(folder,name+".png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
     val panel=storePanel(bitmap,content,fixture.getJSONObject("headlines").getString(name),java.util.Locale.forLanguageTag(locale),++panelIndex)
     File(marketing,name+".png").outputStream().use {panel.compress(Bitmap.CompressFormat.PNG,100,it)}
     panel.recycle();bitmap.recycle()
    }
    fun section(id:Int) {rule.onNode(hasText(label(id)) and hasClickAction()).performClick();rule.waitForIdle()}
    capture("01-overview")
    section(R.string.nav_supplies)
    // Translated names have a different database sort order in each locale.
    rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(supplies.getString(0)))
    rule.onNodeWithText(supplies.getString(0)).assertIsDisplayed()
    rule.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
    capture("02-supplies")
    section(R.string.nav_family_plans);capture("03-family-plan")
    section(R.string.nav_reminders);capture("04-reminders")
    section(R.string.nav_checklists);capture("05-checklists")
    section(R.string.nav_voice)
    val command=voicePatterns.getValue(locale).first {it.action==VoiceAction.EXPIRY}.template
    rule.onNodeWithText(label(R.string.transcript_or_typed_command_48008f)).performTextInput(command)
    androidx.test.espresso.Espresso.closeSoftKeyboard()
    rule.onNodeWithText(label(R.string.review_command_399b62)).performClick();capture("06-voice")
    section(R.string.nav_kits);capture("07-saved-offline-kit")
    section(R.string.nav_settings);rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("PDF",substring=false));rule.onNodeWithText("PDF",substring=false).performClick()
    rule.onNodeWithTag("export-sensitive").performScrollTo().performClick()
    rule.onNodeWithText(label(R.string.report_preview),substring=false).performScrollTo().performClick()
    rule.waitUntil(15000) {rule.onAllNodesWithContentDescription(label(R.string.report_preview)).fetchSemanticsNodes().isNotEmpty()};capture("08-report-preview")
    scenario.onActivity {it.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)}
   }
  }
 }
}
