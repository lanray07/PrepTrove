package com.preptrove.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.speech.RecognitionService
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/** Capability metadata only: never starts recognition, speaks, or installs language data. */
class DeviceCapabilitiesTest {
 @get:Rule val rule = createEmptyComposeRule()

 @Test fun reportInstalledSpeechCapabilitiesAndMissingRecognizerFallback() {
  assumeTrue(BuildConfig.BUILD_TYPE == "debug" && !BuildConfig.LOCALIZATION_QA)
  val instrumentation = InstrumentationRegistry.getInstrumentation()
  val context = instrumentation.targetContext
  assumeTrue(context.packageName == "com.preptrove.app")
  val permissionBefore = context.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
  val report = JSONObject()
   .put("kind", "device_capability_evidence_not_human_speech_validation")
   .put("api", Build.VERSION.SDK_INT)
   .put("manufacturer", Build.MANUFACTURER)
   .put("model", Build.MODEL)
   .put("humanTranscriptionVerified", false)
   .put("audioOutputVerified", false)
   .put("microphoneRequestedOrAudioRecorded", false)
   .put("voicesDownloadedOrInstalled", false)
   .put("privateRecordsModified", false)
   .put("voiceViewVerified", false)
   .put("microphonePermissionBefore", permissionBefore)
  val output = File(requireNotNull(context.getExternalFilesDir("acceptance")), "device-capabilities.json")
  fun saveReport() {
   output.parentFile?.mkdirs()
   output.writeText(report.toString(2))
  }
  try {
   @Suppress("DEPRECATION")
   val recognitionServices = context.packageManager.queryIntentServices(Intent(RecognitionService.SERVICE_INTERFACE), 0)
   report.put("recognitionServices", JSONArray().apply {
    recognitionServices.sortedBy { it.serviceInfo.packageName + it.serviceInfo.name }.forEach {
     put(JSONObject().put("package", it.serviceInfo.packageName).put("service", it.serviceInfo.name))
    }
   })
   val recognitionAvailable = SpeechRecognizer.isRecognitionAvailable(context)
   report.put("recognitionAvailable", recognitionAvailable)
   report.put("recognitionSupport", if (recognitionAvailable) "engine available; human speech untested" else "no recognition service available")

   @Suppress("DEPRECATION")
   val ttsServices = context.packageManager.queryIntentServices(Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE), 0)
   report.put("ttsServices", JSONArray().apply {
    ttsServices.sortedBy { it.serviceInfo.packageName + it.serviceInfo.name }.forEach {
     put(JSONObject().put("package", it.serviceInfo.packageName).put("service", it.serviceInfo.name))
    }
   })
   val initialized = CountDownLatch(1)
   val initStatus = AtomicInteger(Int.MIN_VALUE)
   var tts: TextToSpeech? = null
   try {
    instrumentation.runOnMainSync {
     tts = TextToSpeech(context) { status -> initStatus.set(status); initialized.countDown() }
    }
    val completed = initialized.await(8, TimeUnit.SECONDS)
    report.put("ttsInitializationCompletedWithin8Seconds", completed)
    report.put("ttsInitializationStatus", if (completed) initStatus.get() else JSONObject.NULL)
    if (completed && initStatus.get() == TextToSpeech.SUCCESS) {
     val engine = requireNotNull(tts)
     report.put("ttsDefaultEngine", engine.defaultEngine ?: JSONObject.NULL)
     report.put("ttsEngines", JSONArray().apply {
      engine.engines.sortedBy { it.name }.forEach { put(JSONObject().put("package", it.name).put("label", it.label)) }
     })
     report.put("ttsVoiceEnumerationScope", "default engine metadata only; no language selected or utterance requested")
     report.put("ttsVoices", JSONArray().apply {
      engine.voices.orEmpty().sortedBy { it.name }.forEach { voice ->
       put(JSONObject().put("name", voice.name).put("locale", voice.locale.toLanguageTag())
        .put("networkRequired", voice.isNetworkConnectionRequired)
        .put("features", JSONArray(voice.features.orEmpty().sorted()))
        .put("audioVerified", false))
      }
     })
    } else report.put("ttsSupport", "initialization failed or timed out; spoken output unverified")
   } catch (error: Exception) {
    report.put("ttsSupport", "metadata query failed; spoken output unverified")
    report.put("ttsErrorType", error.javaClass.simpleName)
   } finally {
    instrumentation.runOnMainSync { tts?.shutdown() }
   }

   report.put("missingRecognizerFallback", if (recognitionAvailable) "not exercised: recognizer exists; no microphone action taken" else "pending")
   saveReport()
   if (!recognitionAvailable) {
    ActivityScenario.launch(MainActivity::class.java).use { scenario ->
     lateinit var activity: MainActivity
     scenario.onActivity { activity = it }
     rule.waitForIdle()
     val skip = activity.getString(R.string.skip_28d035)
     if (rule.onAllNodesWithText(skip).fetchSemanticsNodes().isNotEmpty()) {
      // Skipping onboarding persists a preference, so leave it untouched in this read-only test.
      report.put("missingRecognizerFallback", "not exercised: onboarding requires a persistent user choice")
     } else {
      rule.onNode(hasText(activity.getString(R.string.nav_voice)) and hasClickAction()).performScrollTo().performClick()
      rule.onNodeWithText(activity.getString(R.string.msg_4c15103c8011)).performScrollTo().performClick()
      rule.onNodeWithText(activity.getString(R.string.msg_79f24e0d59e2)).performScrollTo().assertIsDisplayed()
      rule.onNodeWithText(activity.getString(R.string.transcript_or_typed_command_48008f)).performScrollTo().assertIsDisplayed()
      rule.onNodeWithText(activity.getString(R.string.review_command_399b62)).performScrollTo().assertIsDisplayed()
      report.put("missingRecognizerFallback", "passed: unavailable-service message and typed-command controls visible")
     }
    }
   }
   assertEquals("Capability test must not change microphone permission", permissionBefore, context.checkSelfPermission(Manifest.permission.RECORD_AUDIO))
  } catch (error: Throwable) {
   report.put("testFailureType", error.javaClass.simpleName)
   throw error
  } finally {
   report.put("microphonePermissionAfter", context.checkSelfPermission(Manifest.permission.RECORD_AUDIO))
   saveReport()
   preserveCiEvidence(context)
  }
 }
}
