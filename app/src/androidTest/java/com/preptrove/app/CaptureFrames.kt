package com.preptrove.app

import android.os.Build
import android.view.inspector.WindowInspector
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Await actual app/dialog frame submission, not just the updated semantics tree. */
internal fun awaitCaptureFrames(scenario:ActivityScenario<MainActivity>) {
 val instrumentation=InstrumentationRegistry.getInstrumentation()
 repeat(2) {
  val pending=mutableListOf<CountDownLatch>()
  scenario.onActivity {activity->
   val views=if(Build.VERSION.SDK_INT>=29)WindowInspector.getGlobalWindowViews()
    .filter {it.isAttachedToWindow && it.isShown && it.hasWindowFocus()}
    else listOf(activity.window.decorView)
   check(views.isNotEmpty()) {"No focused app window for native capture"}
   views.forEach {view->
    val ready=CountDownLatch(1);pending.add(ready)
    if(Build.VERSION.SDK_INT>=29 && view.isHardwareAccelerated) {
     view.viewTreeObserver.registerFrameCommitCallback {ready.countDown()}
     view.postInvalidateOnAnimation()
    } else view.postOnAnimation {ready.countDown()}
   }
  }
  pending.forEach {check(it.await(10,TimeUnit.SECONDS)) {"App frame was not committed before capture"}}
  instrumentation.waitForIdleSync()
 }
}
