package com.preptrove.app
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.Rule
class LockGateTest {
 @get:Rule val rule=createEmptyComposeRule()
 @Test fun lockedActivityDoesNotRenderSavedPlans() {
  val context=InstrumentationRegistry.getInstrumentation().targetContext;val prefs=context.getSharedPreferences("privacy",0);val original=prefs.getBoolean("lock",false)
  try {prefs.edit().putBoolean("lock",true).commit()
   ActivityScenario.launch(MainActivity::class.java).use {
    rule.onNodeWithText("PrepTrove is locked").assertIsDisplayed()
    rule.onNodeWithText("Overview").assertDoesNotExist()
   }
  } finally {prefs.edit().putBoolean("lock",original).commit()}
 }
}
