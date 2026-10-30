package com.preptrove.app

import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class AccessibilityLayoutTest {
 @get:Rule val rule=createAndroidComposeRule<MainActivity>()
 @Test fun doubleSizeTextAndMirroredLayoutKeepSettingsReachable() {
  rule.activityRule.scenario.onActivity {activity->activity.setContent {
   val density=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(density.density,2f),LocalLayoutDirection provides LayoutDirection.Rtl) {
    activity.Planner()
   }
  }}
  rule.waitForIdle()
  if(rule.onAllNodesWithText("Skip").fetchSemanticsNodes().isNotEmpty()) rule.onNodeWithText("Skip").performClick()
  rule.onNode(hasText("Settings") and hasClickAction()).performScrollTo().performClick()
  rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("JSON",substring=false))
  rule.onNodeWithText("JSON",substring=false).assertIsDisplayed().performClick()
  rule.onNodeWithTag("export-history").performScrollTo().assertIsDisplayed().assertIsOff()
  rule.onNodeWithText("Cancel",substring=false).assertIsDisplayed().performClick()
 }
}
