package com.preptrove.app

import android.speech.SpeechRecognizer
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeFalse
import org.junit.Rule
import org.junit.Test

class VoiceFallbackTest {
 @get:Rule val rule=createAndroidComposeRule<MainActivity>()
 @Test fun missingRecognitionServiceKeepsTypedCommandsAvailableWithoutPermissionPrompt() {
  assumeFalse(SpeechRecognizer.isRecognitionAvailable(rule.activity))
  val originalPermission=rule.activity.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
  rule.waitForIdle()
  if(rule.onAllNodesWithText("Skip").fetchSemanticsNodes().isNotEmpty()) rule.onNodeWithText("Skip").performClick()
  rule.onNode(hasText("Voice") and hasClickAction()).performScrollTo().performClick()
  rule.onNodeWithText("Start listening").performScrollTo().performClick()
  rule.onNodeWithText(rule.activity.getString(R.string.msg_79f24e0d59e2)).performScrollTo().assertIsDisplayed()
  assertEquals(originalPermission,rule.activity.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO))
  rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Transcript or typed command"))
  rule.onNodeWithText("Transcript or typed command").performTextInput("What supplies are expiring soon?")
  androidx.test.espresso.Espresso.closeSoftKeyboard()
  rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Review command"))
  rule.onNodeWithText("Review command").performClick()
  rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Read response aloud"))
  rule.onNodeWithText("Read response aloud").assertIsDisplayed()
 }
}
