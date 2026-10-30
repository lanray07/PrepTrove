package com.preptrove.app
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
class PlannerJourneyTest {
 @get:Rule val rule=createAndroidComposeRule<MainActivity>()
 @Test fun kitCanBeCreatedAndSurvivesActivityRecreation() {
  val name="QA kit "+System.currentTimeMillis()
  rule.waitForIdle()
  if(rule.onAllNodesWithText("Skip").fetchSemanticsNodes().isNotEmpty()) rule.onNodeWithText("Skip").performClick()
  rule.onNode(hasText("Kits") and hasClickAction()).performClick()
  rule.onNodeWithContentDescription("Add record").performClick()
  rule.onNodeWithText("Name").performTextInput(name)
  rule.onNodeWithText("Save",substring=false).performClick()
  rule.waitUntil(10000) {rule.onAllNodesWithText(name).fetchSemanticsNodes().isNotEmpty()}
  rule.activityRule.scenario.recreate()
  rule.waitForIdle()
  rule.onNode(hasText("Kits") and hasClickAction()).performClick()
  rule.waitUntil(10000) {rule.onAllNodesWithText(name).fetchSemanticsNodes().isNotEmpty()}
  rule.onNodeWithText(name).assertIsDisplayed()
 }
 @Test fun structuredPlanCanBeSavedThroughTheEditor() {
  val name="QA plan "+System.currentTimeMillis()
  rule.waitForIdle()
  if(rule.onAllNodesWithText("Skip").fetchSemanticsNodes().isNotEmpty()) rule.onNodeWithText("Skip").performClick()
  rule.onNode(hasText("Family plans") and hasClickAction()).performClick()
  rule.onNodeWithContentDescription("Add record").performClick()
  rule.onNodeWithText("Name").performTextInput(name)
  rule.onNodeWithText("Meeting points").performScrollTo().performTextInput("Library entrance")
  rule.onNodeWithText("Save",substring=false).performClick()
  rule.waitUntil(10000) {rule.onAllNodesWithText(name).fetchSemanticsNodes().isNotEmpty()}
  rule.onNodeWithText("Library entrance").assertIsDisplayed()
 }
 @Test fun unsavedSupplyDraftSurvivesActivityRecreation() {
  val name="QA draft "+System.currentTimeMillis()
  rule.waitForIdle()
  if(rule.onAllNodesWithText("Skip").fetchSemanticsNodes().isNotEmpty()) rule.onNodeWithText("Skip").performClick()
  rule.onNode(hasText("Supplies") and hasClickAction()).performClick()
  rule.onNodeWithContentDescription("Add record").performClick()
  rule.onNodeWithText("Name").performTextInput(name)
  rule.activityRule.scenario.recreate();rule.waitForIdle()
  rule.onNodeWithText(name).assertIsDisplayed()
  rule.onNodeWithText("Save",substring=false).performClick()
  rule.onNodeWithText("Name",substring=false).assertDoesNotExist()
  rule.waitUntil(10000) {rule.onAllNodesWithText(name).fetchSemanticsNodes().isNotEmpty()}
 }
}

