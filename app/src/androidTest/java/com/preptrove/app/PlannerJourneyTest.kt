package com.preptrove.app
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
class PlannerJourneyTest {
 @get:Rule val rule=createAndroidComposeRule<MainActivity>()
 private fun findRecord(section:String,name:String) {
  rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Search $section"))
  rule.onNodeWithText("Search $section").performTextClearance()
  rule.onNodeWithText("Search $section").performTextInput(name)
  androidx.test.espresso.Espresso.closeSoftKeyboard()
  rule.waitForIdle()
 }
 @Test fun kitCanBeCreatedAndSurvivesActivityRecreation() {
  val name="QA kit "+System.currentTimeMillis()
  rule.waitForIdle()
  if(rule.onAllNodesWithText("Skip").fetchSemanticsNodes().isNotEmpty()) rule.onNodeWithText("Skip").performClick()
  rule.onNode(hasText("Kits") and hasClickAction()).performClick()
  rule.onNodeWithContentDescription("Add record").performClick()
  rule.onNodeWithText("Name").performTextInput(name)
  rule.onNodeWithText("Save",substring=false).performClick()
  findRecord("Kits",name)
  rule.waitUntil(10000) {rule.onAllNodes(hasText(name) and !hasSetTextAction()).fetchSemanticsNodes().isNotEmpty()}
  rule.activityRule.scenario.recreate()
  rule.waitForIdle()
  rule.onNode(hasText("Kits") and hasClickAction()).performClick()
  findRecord("Kits",name)
  rule.waitUntil(10000) {rule.onAllNodes(hasText(name) and !hasSetTextAction()).fetchSemanticsNodes().isNotEmpty()}
  rule.onNode(hasText(name) and !hasSetTextAction()).assertIsDisplayed()
 }
 @Test fun structuredPlanCanBeSavedThroughTheEditor() {
  val name="QA plan "+System.currentTimeMillis()
  rule.waitForIdle()
  if(rule.onAllNodesWithText("Skip").fetchSemanticsNodes().isNotEmpty()) rule.onNodeWithText("Skip").performClick()
  rule.onNode(hasText("Family plans") and hasClickAction()).performClick()
  rule.waitForIdle()
  rule.onNodeWithContentDescription("Add record").performClick()
  rule.onNodeWithText("Name").performTextInput(name)
  try {rule.onNodeWithText("Meeting points").performScrollTo().performTextInput("Library entrance")} catch(error:AssertionError) {throw AssertionError("Plan editor state: "+rule.onNode(isDialog()).printToString(),error)}
  rule.onNodeWithText("Save",substring=false).performClick()
  findRecord("Family plans",name)
  rule.waitUntil(10000) {rule.onAllNodes(hasText(name) and !hasSetTextAction()).fetchSemanticsNodes().isNotEmpty()}
  rule.onNodeWithText("Library entrance").performScrollTo().assertIsDisplayed()
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
  findRecord("Supplies",name)
  rule.waitUntil(10000) {rule.onAllNodes(hasText(name) and !hasSetTextAction()).fetchSemanticsNodes().isNotEmpty()}
 }
}

