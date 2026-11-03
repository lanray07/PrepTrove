package com.preptrove.app

import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.Locale
import java.util.UUID

/** Opt-in acceptance on a disposable emulator; never runs against a physical installation. */
class EphemeralUiAcceptanceTest {
 @get:Rule val rule = createEmptyComposeRule()

 @Test fun countryChoicePersistsAndJsonUsesRealSystemCreateDocument() = runBlocking<Unit> {
  val instrumentation = InstrumentationRegistry.getInstrumentation()
  val context = instrumentation.targetContext
  // Every mutation, including fixture setup and evidence files, follows all three guards.
  assumeTrue(InstrumentationRegistry.getArguments().getString("ephemeralCi") == "true")
  assumeTrue(BuildConfig.BUILD_TYPE == "debug" && !BuildConfig.LOCALIZATION_QA && context.packageName == "com.preptrove.app")
  val qemu = ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand("getprop ro.kernel.qemu"))
   .bufferedReader().use { it.readText().trim() }
  assumeTrue(qemu == "1")

  val output = requireNotNull(context.getExternalFilesDir("acceptance"))
  val report = JSONObject().put("kind", "ephemeral_emulator_real_saf_acceptance")
   .put("ephemeralCi", true).put("qemu", qemu).put("systemDocumentsUiUsed", false)
   .put("microphoneRequested", false).put("browserLaunched", false)
  val filename = "PrepTrove-ci-${UUID.randomUUID()}.json"
  report.put("syntheticExportFilename", filename)
  try {
   val dao = (context.applicationContext as PrepTrove).db.entries()
   assertTrue("Requires a fresh CI app database; this test never clears existing records", dao.all().isEmpty())
   assertTrue("Requires a fresh CI review history", dao.allReviews().isEmpty())
   AppLanguages.select(context, "en-GB")
   context.preferences.edit { it[booleanPreferencesKey("onboarded")] = true }
   val original=Entry(id = "ci-saf-${UUID.randomUUID()}", kind = "supply", title = "Synthetic CI export item", quantity = 2.0, unit = "items", detail = "Synthetic acceptance record; no personal information.")
   dao.save(original)
   val localized = AppLanguages.wrap(context)
   ActivityScenario.launch(MainActivity::class.java).use { scenario ->
    fun section(id: Int) {
     rule.onNode(hasText(localized.getString(id)) and hasClickAction()).performScrollTo().performClick()
     rule.waitForIdle()
    }
    section(R.string.nav_reference)
    rule.onNodeWithTag("guidance-country").performScrollTo().performClick()
    val country = Locale("", "GB").getDisplayCountry(Locale.UK)
    rule.onNode(hasText(country) and hasClickAction()).performScrollTo().performClick()
    rule.onNodeWithTag("guidance-country").assertTextContains(country)
    assertEquals("GB", GuidanceDirectory.selected(context))
    scenario.recreate()
    rule.waitForIdle()
    section(R.string.nav_reference)
    rule.onNodeWithTag("guidance-country").performScrollTo().assertTextContains(country)
    assertEquals("GB", GuidanceDirectory.selected(context))
    report.put("guidanceGbSelectionPersistedAfterRecreation", true)

    section(R.string.nav_settings)
    rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("JSON",substring=false))
    rule.onNode(hasText("JSON") and hasClickAction()).performClick()
    rule.onNodeWithText(localized.getString(R.string.choose_destination_5853e2)).performClick()

    val automation = instrumentation.uiAutomation
    val deadline = SystemClock.uptimeMillis() + 15_000
    var filenameSet = false
    var saveClicked = false
    var lastTree = "No active accessibility window"
    while (SystemClock.uptimeMillis() < deadline && !saveClicked) {
     val root = automation.rootInActiveWindow
     if (root != null) {
      lastTree = describeTree(root)
      val packageName = root.packageName?.toString().orEmpty()
      if (packageName in DOCUMENTS_PACKAGES) {
       report.put("systemDocumentsUiPackage", packageName).put("systemDocumentsUiUsed", true)
       val nodes = descendants(root)
       if (!filenameSet) {
        val editors = nodes.filter { it.isEditable && it.isEnabled && it.isVisibleToUser }
        if (editors.size == 1) {
         filenameSet = editors.single().performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, Bundle().apply {
          putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, filename)
         })
        }
       } else if (nodes.any { it.isEditable && it.text?.toString() == filename }) {
        val save = nodes.firstOrNull {
         it.text?.toString()?.equals("Save", ignoreCase = true) == true && it.isEnabled && it.isVisibleToUser
        }
        if (save != null) saveClicked = clickNodeOrAncestor(save)
       }
      }
     }
     if (!saveClicked) SystemClock.sleep(150)
    }
    if (!filenameSet || !saveClicked) {
     output.mkdirs()
     File(output, "saf-selector-failure.txt").writeText(lastTree)
     fail("Real DocumentsUI filename/Save selector was not verified within 15 seconds; see acceptance/saf-selector-failure.txt")
    }
    report.put("documentsUiSaveClicked", true)
    // Activity-result delivery and the asynchronous document write must both finish.
    rule.waitUntil(20_000) {
     runCatching {
      rule.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
      rule.onAllNodesWithText(localized.getString(R.string.msg_3b92ecba1a57)).fetchSemanticsNodes().isNotEmpty()
     }.getOrDefault(false)
    }
    rule.onNodeWithText(localized.getString(R.string.msg_3b92ecba1a57)).performScrollTo().assertIsDisplayed()
    report.put("localizedExportSavedMessageVisible", true)
    report.put("exportResult", "passed: real CreateDocument selection followed by app write-success message")
    rule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(localized.getString(R.string.msg_96276c8b3a4b)))
    rule.onNodeWithText(localized.getString(R.string.msg_96276c8b3a4b)).performClick()
    rule.onNodeWithText(localized.getString(R.string.msg_b09c73d9d3f8)).performClick()
    val importDeadline=SystemClock.uptimeMillis()+15_000
    var documentChosen=false
    while(SystemClock.uptimeMillis()<importDeadline && !documentChosen) {
     val root=automation.rootInActiveWindow
     if(root!=null && root.packageName?.toString() in DOCUMENTS_PACKAGES) {
      lastTree=describeTree(root)
      descendants(root).firstOrNull {it.text?.toString()==filename && it.isVisibleToUser && it.isEnabled}
       ?.let {documentChosen=clickNodeOrAncestor(it)}
     }
     if(!documentChosen)SystemClock.sleep(150)
    }
    if(!documentChosen) {
     output.mkdirs()
     File(output,"saf-open-selector-failure.txt").writeText(lastTree)
     fail("Real DocumentsUI could not select the freshly exported synthetic backup")
    }
    report.put("realOpenDocumentSelection",true)
    rule.waitUntil(20_000) {rule.onAllNodesWithText(localized.getString(R.string.msg_f539a84db776)).fetchSemanticsNodes().isNotEmpty()}
    rule.onNodeWithText(localized.getString(R.string.msg_f539a84db776)).performClick()
    rule.waitUntil(20_000) {runBlocking {dao.all().size==2}}
    val restored=dao.all()
    assertEquals(original,restored.single {it.id==original.id})
    val copied=restored.single {it.id!=original.id}
    assertEquals(original.title,copied.title)
    assertEquals(original.quantity,copied.quantity,0.0)
    assertEquals(original.unit,copied.unit)
    report.put("realDocumentRestoreCreatesCopyAndPreservesOriginal",true)
    // No URI interception, fake provider, direct filesystem substitute, cloud account or network flow.
   }
  } catch (error: Throwable) {
   report.put("failureType", error.javaClass.simpleName)
   output.mkdirs()
   runCatching {
    instrumentation.uiAutomation.rootInActiveWindow?.let { File(output, "ephemeral-ui-failure.txt").writeText(describeTree(it)) }
   }
   throw error
  } finally {
   output.mkdirs()
   File(output, "ephemeral-ui-acceptance.json").writeText(report.toString(2))
   preserveCiEvidence(context)
  }
 }

 private fun descendants(root: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
  val result = mutableListOf<AccessibilityNodeInfo>()
  fun visit(node: AccessibilityNodeInfo, depth: Int) {
   if (depth > 40 || result.size >= 500) return
   result.add(node)
   for (index in 0 until node.childCount) node.getChild(index)?.let { visit(it, depth + 1) }
  }
  visit(root, 0)
  return result
 }

 private fun clickNodeOrAncestor(node: AccessibilityNodeInfo): Boolean {
  var current: AccessibilityNodeInfo? = node
  repeat(5) {
   val candidate = current ?: return false
   if (candidate.packageName?.toString() !in DOCUMENTS_PACKAGES) return false
   if (candidate.isClickable && candidate.isEnabled) return candidate.performAction(AccessibilityNodeInfo.ACTION_CLICK)
   current = candidate.parent
  }
  return false
 }

 private fun describeTree(root: AccessibilityNodeInfo): String = descendants(root).joinToString("\n") {
  "package=${it.packageName} class=${it.className} id=${it.viewIdResourceName} text=${it.text} description=${it.contentDescription} enabled=${it.isEnabled} clickable=${it.isClickable} editable=${it.isEditable} visible=${it.isVisibleToUser}"
 }

 private companion object {
  val DOCUMENTS_PACKAGES = setOf("com.android.documentsui", "com.google.android.documentsui")
 }
}
