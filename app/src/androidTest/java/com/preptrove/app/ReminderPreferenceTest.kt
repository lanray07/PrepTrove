package com.preptrove.app

import android.os.Build
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

class ReminderPreferenceTest {
 @get:Rule val rule=createAndroidComposeRule<MainActivity>()
 @Test fun reminderOptInSurvivesRecreationAndCanBeDisabled() {
  // API 33+ permission dialog acceptance remains a separate device/owner acceptance check.
  assumeTrue(Build.VERSION.SDK_INT<33)
  val context=rule.activity.applicationContext
  val key=booleanPreferencesKey("remindersEnabled")
  val original=runBlocking {context.preferences.data.first()[key]}
  try {
   runBlocking {ReminderWorker.setEnabled(context,false)}
   rule.waitForIdle()
   if(rule.onAllNodesWithText("Skip").fetchSemanticsNodes().isNotEmpty())rule.onNodeWithText("Skip").performClick()
   rule.onNode(hasText("Settings") and hasClickAction()).performScrollTo().performClick()
   rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Enable optional reminders"))
   rule.onNodeWithText("Enable optional reminders").performClick()
   rule.waitUntil(10000) {rule.onAllNodesWithText("Disable reminder notifications").fetchSemanticsNodes().isNotEmpty()}
   assertEquals(true,runBlocking {context.preferences.data.first()[key]})
   rule.activityRule.scenario.recreate();rule.waitForIdle()
   rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Disable reminder notifications"))
   rule.onNodeWithText("Disable reminder notifications").assertIsDisplayed().performClick()
   rule.waitUntil(10000) {rule.onAllNodesWithText("Enable optional reminders").fetchSemanticsNodes().isNotEmpty()}
   assertEquals(false,runBlocking {context.preferences.data.first()[key]})
  } finally {
   runBlocking {
    ReminderWorker.setEnabled(context,original ?: false)
    if(original==null)context.preferences.edit {it.remove(key)}
   }
  }
 }
}
