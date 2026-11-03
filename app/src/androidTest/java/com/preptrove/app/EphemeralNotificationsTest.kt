package com.preptrove.app

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/** Real notification permission UI, confined to an explicitly disposable emulator. */
class EphemeralNotificationsTest {
 @get:Rule val rule=createEmptyComposeRule()
 private val instrumentation get()=InstrumentationRegistry.getInstrumentation()
 private fun shell(command:String)=ParcelFileDescriptor.AutoCloseInputStream(
  instrumentation.uiAutomation.executeShellCommand(command)).bufferedReader().use {it.readText()}

 private fun await(message:String, predicate:()->Boolean) {
  val deadline=SystemClock.uptimeMillis()+15_000
  while(SystemClock.uptimeMillis()<deadline) {
   if(predicate())return
   SystemClock.sleep(100)
  }
  fail(message)
 }
 private fun choosePermission(allow:Boolean) {
  val id=if(allow)"permission_allow_button" else "permission_deny_button"
  fun find(node:AccessibilityNodeInfo):AccessibilityNodeInfo? {
   if(node.viewIdResourceName?.endsWith(":id/$id")==true && node.isVisibleToUser && node.isEnabled)return node
   for(index in 0 until node.childCount)node.getChild(index)?.let {find(it)?.let {found->return found}}
   return null
  }
  await("Real Android permission choice was not available") {
   val root=instrumentation.uiAutomation.rootInActiveWindow ?: return@await false
   if(root.packageName?.toString() !in setOf("com.android.permissioncontroller","com.google.android.permissioncontroller"))return@await false
   find(root)?.performAction(AccessibilityNodeInfo.ACTION_CLICK)==true
  }
 }

 @Test fun notificationDenialOptInPersistenceAndGenericNotice()=runBlocking<Unit> {
  val context=instrumentation.targetContext
  assumeTrue(BuildConfig.BUILD_TYPE=="debug" && context.packageName=="com.preptrove.app")
  assumeTrue(InstrumentationRegistry.getArguments().getString("ephemeralCi")=="true")
  assumeTrue(Build.VERSION.SDK_INT>=33 && shell("getprop ro.kernel.qemu").trim()=="1")
  assumeTrue(context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
  val key=booleanPreferencesKey("remindersEnabled")
  val manager=context.getSystemService(NotificationManager::class.java)
  val tag="ephemeral-ci-reminders"
  val notices=ReviewNotifications(context,tag)
  AppLanguages.select(context,"en-GB")
  context.preferences.edit {it[booleanPreferencesKey("onboarded")]=true}
  ReminderWorker.setEnabled(context,false)
  val localized=AppLanguages.wrap(context)
  fun enabled()=runBlocking {context.preferences.data.first()[key]==true}
  try {
   ActivityScenario.launch(MainActivity::class.java).use {scenario->
    rule.onNode(hasText(localized.getString(R.string.nav_settings)) and hasClickAction()).performScrollTo().performClick()
    fun enable(){rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(localized.getString(R.string.enable_optional_reminders_4af429)))
     rule.onNodeWithText(localized.getString(R.string.enable_optional_reminders_4af429)).performClick()}
    enable();choosePermission(false)
    await("Denial should keep reminder opt-in disabled"){!enabled() && instrumentation.uiAutomation.rootInActiveWindow?.packageName?.toString()==context.packageName}
    assertEquals(PackageManager.PERMISSION_DENIED,context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS))
    enable();choosePermission(true)
    await("Real permission grant did not enable reminders"){enabled() && manager.areNotificationsEnabled()}
    scenario.recreate();rule.waitForIdle()
    rule.onNode(hasText(localized.getString(R.string.nav_settings)) and hasClickAction()).performScrollTo().performClick()
    rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(localized.getString(R.string.reminders_disable)))
    rule.onNodeWithText(localized.getString(R.string.reminders_disable)).assertIsDisplayed()
    notices.update(true,2)
    val expected=localized.resources.getQuantityString(R.plurals.reminders_due,2,2)
    await("Generic due-count notification was not posted") {
     manager.activeNotifications.singleOrNull {it.tag==tag}?.notification?.extras?.getString(Notification.EXTRA_TEXT)==expected
    }
    notices.update(false,2)
    await("Disabled generic notification did not clear"){manager.activeNotifications.none {it.tag==tag}}
    rule.onNodeWithText(localized.getString(R.string.reminders_disable)).performClick()
    await("Opt-out did not persist"){!enabled()}

    // Decline the real microphone request; this test never grants access or
    // records audio. Missing recognition services take the explicit fallback.
    assertEquals(PackageManager.PERMISSION_DENIED,context.checkSelfPermission(Manifest.permission.RECORD_AUDIO))
    rule.onNode(hasText(localized.getString(R.string.nav_voice)) and hasClickAction()).performScrollTo().performClick()
    rule.onNodeWithText(localized.getString(R.string.msg_4c15103c8011)).performScrollTo().performClick()
    val recognizer=android.speech.SpeechRecognizer.isRecognitionAvailable(context)
    if(recognizer)choosePermission(false)
    val fallback=localized.getString(if(recognizer) R.string.msg_16cfda4cd82d else R.string.msg_79f24e0d59e2)
    rule.waitUntil(15_000) {rule.onAllNodesWithText(fallback).fetchSemanticsNodes().isNotEmpty()}
    rule.onNodeWithText(fallback).performScrollTo().assertIsDisplayed()
    assertEquals(PackageManager.PERMISSION_DENIED,context.checkSelfPermission(Manifest.permission.RECORD_AUDIO))
    rule.onNodeWithText(localized.getString(R.string.transcript_or_typed_command_48008f)).performScrollTo().performTextInput("What supplies are expiring soon?")
    androidx.test.espresso.Espresso.closeSoftKeyboard()
    rule.onNodeWithText(localized.getString(R.string.review_command_399b62)).performScrollTo().performClick()
    rule.onNodeWithText(localized.getString(R.string.read_response_aloud_ac4ff0)).performScrollTo().assertIsDisplayed()
   }
  } finally {
   notices.clear()
   ReminderWorker.setEnabled(context,false)
   // Keep the grant for the independent notification replacement test. The
   // entire disposable emulator is destroyed by CI; revoking here kills this
   // instrumentation process and would hide subsequent test results.
  }
 }
}
