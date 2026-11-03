package com.preptrove.app

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/** Mutates credentials ONLY on an explicitly opted-in, initially unsecured API 36 emulator. */
class EphemeralCredentialTest {
 @get:Rule val rule = createEmptyComposeRule()
 private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
 private val automation get() = instrumentation.uiAutomation

 private fun shell(command: String): String =
  ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(command))
   .bufferedReader().use { it.readText() }

 private fun eventually(message: String, condition: () -> Boolean) {
  val deadline = android.os.SystemClock.uptimeMillis() + 15_000
  while (android.os.SystemClock.uptimeMillis() < deadline) {
   if (condition()) return
   android.os.SystemClock.sleep(100)
  }
  fail(message)
 }

 /** Native framework UI only: never inspect or log field contents, labels or credentials. */
 private fun editable(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
  if (node.isEditable && node.isVisibleToUser) return AccessibilityNodeInfo.obtain(node)
  for (index in 0 until node.childCount) {
   val child = node.getChild(index) ?: continue
   val found = try { editable(child) } finally { child.recycle() }
   if (found != null) return found
  }
  return null
 }

 private fun credentialField(): AccessibilityNodeInfo? {
  // The active window is available without opting into all interactive windows.
  val active = automation.rootInActiveWindow
  if (active != null) {
   try {
    if (active.packageName?.toString() in setOf("com.android.settings", "com.android.systemui")) {
     editable(active)?.let { return it }
    }
   } finally { active.recycle() }
  }
  for (window in automation.windows) {
   val root = window.root ?: continue
   try {
    // A PrepTrove edit control can never satisfy credential confirmation.
    if (root.packageName?.toString() == "com.android.settings" ||
        root.packageName?.toString() == "com.android.systemui") {
     editable(root)?.let { return it }
    }
   } finally { root.recycle() }
  }
  return null
 }

 private fun awaitCredentialUi() {
  eventually("Android credential confirmation did not expose a native editable field") {
   credentialField()?.let { it.recycle(); true } ?: false
  }
 }

 private fun authenticateSyntheticPin() {
  awaitCredentialUi()
  val field = credentialField() ?: error("Native credential field disappeared")
  try {
   // A focused platform password field can reject a redundant ACTION_FOCUS.
   if (!field.isFocused) {
    field.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
    if (!field.refresh() || !field.isFocused) field.performAction(AccessibilityNodeInfo.ACTION_CLICK)
   }
   val arguments = Bundle().apply {
    putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, SYNTHETIC_PIN)
   }
   if (!field.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)) {
    // Emulator-only fallback for platform password fields that reject accessibility SET_TEXT.
    shell("input text $SYNTHETIC_PIN")
   }
   shell("input keyevent KEYCODE_ENTER")
  } finally { field.recycle() }
 }

 @Test fun actualDeviceCredentialEnablesLocksCancelsAndUnlocks() = runBlocking {
  val context = instrumentation.targetContext
  val manager = context.getSystemService(KeyguardManager::class.java)
  // Every guard precedes preferences, activity launches and credential changes.
  assumeTrue("Normal debug package required", BuildConfig.BUILD_TYPE == "debug" &&
   BuildConfig.DEBUG && context.packageName == "com.preptrove.app")
  assumeTrue("Explicit ephemeral CI opt-in required",
   InstrumentationRegistry.getArguments().getString("ephemeralCi") == "true")
  assumeTrue("API 36 emulator required", Build.VERSION.SDK_INT == 36 &&
   shell("getprop ro.kernel.qemu").trim() == "1")
  assumeTrue("Existing secure credentials must never be touched", !manager.isDeviceSecure)
  val privacy = context.getSharedPreferences("privacy", Context.MODE_PRIVATE)
  assertFalse("Fresh emulator must not already have app lock enabled", privacy.getBoolean("lock", false))

  var credentialAttempted = false
  var scenario: ActivityScenario<MainActivity>? = null
  try {
   credentialAttempted = true
   // Output deliberately discarded: failures must not echo credential command arguments.
   shell("locksettings set-pin $SYNTHETIC_PIN")
   eventually("Synthetic emulator credential was not installed") { manager.isDeviceSecure }
   AppLanguages.select(context, "en-GB")
   context.preferences.edit { it[booleanPreferencesKey("onboarded")] = true }
   scenario = ActivityScenario.launch(MainActivity::class.java)
   val localized = AppLanguages.wrap(context)
   rule.onNode(hasText(localized.getString(R.string.nav_settings)) and hasClickAction())
    .performScrollTo().performClick()
   rule.onNodeWithText(localized.getString(R.string.lock_enable)).performScrollTo().performClick()
   authenticateSyntheticPin()
   eventually("Enabling lock did not persist after real credential authentication") {
    privacy.getBoolean("lock", false)
   }
   rule.waitUntil(15_000) {runCatching {rule.onNodeWithText(localized.getString(R.string.lock_disable)).assertExists();true}.getOrDefault(false)}

   // Background and recreate the real activity; no private lock-state hooks or stubs.
   scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
   scenario.close()
   scenario = ActivityScenario.launch(MainActivity::class.java)
   rule.waitUntil(15_000) {runCatching {rule.onNodeWithText(localized.getString(R.string.lock_title)).assertIsDisplayed();true}.getOrDefault(false)}
   rule.onNodeWithText(localized.getString(R.string.lock_unlock)).performClick()
   awaitCredentialUi()
   shell("input keyevent KEYCODE_BACK")
   eventually("Cancelled credential UI did not close") {
    credentialField()?.let { it.recycle(); false } ?: true
   }
   rule.waitUntil(15_000) {runCatching {rule.onNodeWithText(localized.getString(R.string.lock_title)).assertIsDisplayed();true}.getOrDefault(false)}
   assertTrue("Cancellation disabled app lock", privacy.getBoolean("lock", false))
   rule.onNodeWithText(localized.getString(R.string.lock_unlock)).performClick()
   authenticateSyntheticPin()
   rule.waitUntil(15_000) {
    runCatching {rule.onNode(hasText(localized.getString(R.string.nav_overview)) and hasClickAction()).assertExists();true}.getOrDefault(false)
   }
   rule.onNode(hasText(localized.getString(R.string.nav_overview)) and hasClickAction()).assertExists()
   assertTrue("Unlocking unexpectedly disabled app lock", privacy.getBoolean("lock", false))
  } finally {
   try { scenario?.close() } finally {
    // This is solely the app privacy flag. Never clear planner records or general preferences.
    privacy.edit().putBoolean("lock", false).commit()
    if (credentialAttempted) {
     shell("locksettings clear --old $SYNTHETIC_PIN")
     eventually("Synthetic credential cleanup failed; discard this CI emulator") {
      !manager.isDeviceSecure
     }
    }
   }
  }
 }

 private companion object {
  // Public, disposable fixture; never a credential obtained from a person or existing device.
  const val SYNTHETIC_PIN = "864209"
 }
}
