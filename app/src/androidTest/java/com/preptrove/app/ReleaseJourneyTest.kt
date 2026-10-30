package com.preptrove.app

import android.content.pm.ActivityInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class ReleaseJourneyTest {
 @get:Rule val rule=createAndroidComposeRule<MainActivity>()

 @Test fun portraitNavigationAndReportPreviewRemainUsable() {
  var originalOrientation=ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
  rule.activityRule.scenario.onActivity {
   originalOrientation=it.requestedOrientation
   it.requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
  }
  try {
   rule.waitUntil(15000) {
    rule.activity.resources.configuration.screenWidthDp<rule.activity.resources.configuration.screenHeightDp
   }
   rule.waitForIdle()
   if(rule.onAllNodesWithText("Skip").fetchSemanticsNodes().isNotEmpty()) rule.onNodeWithText("Skip").performClick()
   rule.onNode(hasText("Settings") and hasClickAction()).performScrollTo().performClick()
   rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("PDF",substring=false))
   rule.onNodeWithText("PDF",substring=false).performClick()
   rule.onNodeWithText("PDF preview",substring=false).performScrollTo().performClick()
   rule.waitUntil(15000) {rule.onAllNodesWithContentDescription("PDF preview").fetchSemanticsNodes().isNotEmpty()}
   rule.onNodeWithContentDescription("PDF preview").assertIsDisplayed()
   rule.onNodeWithTag("report-close").assertIsDisplayed().performClick()
   rule.onNodeWithText("Choose destination",substring=false).assertIsDisplayed()
   rule.onNodeWithText("Cancel",substring=false).performClick()
  } finally {
   rule.activityRule.scenario.onActivity {it.requestedOrientation=originalOrientation}
  }
 }
}
