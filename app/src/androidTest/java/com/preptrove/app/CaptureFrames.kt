package com.preptrove.app

import android.os.Build
import android.os.SystemClock
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
  val focusDeadline=SystemClock.uptimeMillis()+10_000
  while(pending.isEmpty() && SystemClock.uptimeMillis()<focusDeadline) {
   scenario.onActivity {activity->
   val views=if(Build.VERSION.SDK_INT>=29)WindowInspector.getGlobalWindowViews()
    .filter {it.isAttachedToWindow && it.isShown && it.hasWindowFocus()}
    else listOf(activity.window.decorView)
   views.forEach {view->
    val ready=CountDownLatch(1);pending.add(ready)
    if(Build.VERSION.SDK_INT>=29 && view.isHardwareAccelerated) {
     view.viewTreeObserver.registerFrameCommitCallback {ready.countDown()}
     view.postInvalidateOnAnimation()
    } else view.postOnAnimation {ready.countDown()}
   }
   }
   if(pending.isEmpty())SystemClock.sleep(16)
  }
  check(pending.isNotEmpty()) {"No focused app window for native capture within 10 seconds"}
  pending.forEach {check(it.await(10,TimeUnit.SECONDS)) {"App frame was not committed before capture"}}
  instrumentation.waitForIdleSync()
 }
}
