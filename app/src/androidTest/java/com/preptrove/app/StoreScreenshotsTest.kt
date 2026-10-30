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
  for(locale in listOf("en-US","en-GB","en-AU")) {
   AppLanguages.select(context,locale)
   context.preferences.edit {it[booleanPreferencesKey("onboarded")]=true}
   dao.clear();dao.clearReviews()
   val kit=Entry(id="example-kit",kind="kit",title="Home essentials · example",location=if(locale=="en-US") "Hall closet" else "Hall cupboard",detail="Example household kit. Replace these records with your own supplies.")
   dao.save(kit)
   listOf(Entry(kind="supply",title="Bottled water",quantity=6.0,unit="bottles",expiry="2026-11-15"),Entry(kind="supply",title=if(locale=="en-US") "Flashlight" else "Torch",quantity=2.0,unit="items"),Entry(kind="supply",title="Spare batteries",quantity=12.0,unit="items"),Entry(kind="supply",title="Ready-to-eat food",quantity=8.0,unit="tins",expiry="2026-11-18"),Entry(kind="supply",title="Portable radio"),Entry(kind="supply",title="Power bank",quantity=2.0)).forEach {dao.save(it.copy(kitId=kit.id,location=kit.location))}
   dao.save(Entry(kind="plan",title="Household plan · example",detail="Discuss this plan together and adapt it to your household.",extraData="{\"members\":\"Household member A; Household member B\",\"meeting\":\"Local library entrance (example)\",\"communication\":\"Check in with your agreed out-of-area contact.\",\"pets\":\"Agree who will collect the pet carrier.\",\"checkIn\":\"Agree when and how to check in.\"}"))
   dao.save(Entry(kind="contact",title="Out-of-area contact · example",detail="Add your trusted contact's details.",extraData="{\"relationship\":\"Trusted contact\"}"))
   listOf("Find your torch and spare batteries","Review your household communication plan","Check your power bank").forEach {dao.save(Entry(kind="check",title=it,category="Power outage"))}
   dao.save(Entry(kind="reminder",title="Review home kit",expiry="2026-11-15",intervalDays=30));dao.save(Entry(kind="reminder",title="Inspect spare batteries",expiry="2026-11-20",intervalDays=60))
   ActivityScenario.launch(MainActivity::class.java).use {
    rule.waitUntil(15000) {runCatching {rule.onAllNodesWithText("6",substring=false).fetchSemanticsNodes().isNotEmpty()}.getOrDefault(false)}
    val folder=File(context.getExternalFilesDir("store"),locale).apply {mkdirs()}
    fun capture(name:String) {rule.waitForIdle();instrumentation.uiAutomation.takeScreenshot().let {bitmap->File(folder,name+".png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()}}
    fun section(name:String) {rule.onNode(hasText(name) and hasClickAction()).performClick();rule.waitForIdle()}
    capture("01-overview")
    section("Supplies");rule.waitUntil(10000) {rule.onAllNodesWithText("Bottled water").fetchSemanticsNodes().isNotEmpty()};capture("02-supplies")
    section("Family plans");capture("03-family-plan")
    section("Reminders");capture("04-reminders")
    section("Checklists");capture("05-checklists")
    section("Voice");rule.onNodeWithText("Transcript or typed command").performTextInput("What supplies are expiring soon?");androidx.test.espresso.Espresso.closeSoftKeyboard();rule.onNodeWithText("Review command").performClick();capture("06-voice")
    section("Kits");capture("07-saved-offline-kit")
    section("Settings");rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("PDF",substring=false));rule.onNodeWithText("PDF",substring=false).performClick()
    rule.onNodeWithTag("export-sensitive").performScrollTo().performClick()
    rule.onNodeWithText("PDF preview",substring=false).performScrollTo().performClick()
    rule.waitUntil(15000) {rule.onAllNodesWithContentDescription("PDF preview").fetchSemanticsNodes().isNotEmpty()};capture("08-report-preview")
   }
  }
 }
}
