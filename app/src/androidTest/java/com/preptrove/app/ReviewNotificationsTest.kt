package com.preptrove.app

import android.app.Notification
import android.app.NotificationManager
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test

class ReviewNotificationsTest {
 @Test fun countOnlyNoticeIsReplacedAndClearedWhenDisabledOrNoLongerDue() {
  val context=InstrumentationRegistry.getInstrumentation().targetContext
  val manager=context.getSystemService(NotificationManager::class.java)
  assumeTrue(manager.areNotificationsEnabled())
  assumeTrue(Build.VERSION.SDK_INT<33 || context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)==android.content.pm.PackageManager.PERMISSION_GRANTED)
  val tag="qa-reminder-"+java.util.UUID.randomUUID()
  val notices=ReviewNotifications(context,tag)
  fun current()=manager.activeNotifications.filter {it.tag==tag}
  fun await(count:Int,body:String?=null) {
   val deadline=android.os.SystemClock.uptimeMillis()+5000
   while(android.os.SystemClock.uptimeMillis()<deadline) {
    val posted=current()
    if(posted.size==count && (body==null || posted.single().notification.extras.getString(Notification.EXTRA_TEXT)==body))return
    android.os.SystemClock.sleep(20)
   }
   assertEquals(count,current().size)
   if(body!=null)assertEquals(body,current().single().notification.extras.getString(Notification.EXTRA_TEXT))
  }
  try {
   notices.update(false,3);await(0)
   notices.update(true,2)
   await(1,AppLanguages.wrap(context).resources.getQuantityString(R.plurals.reminders_due,2,2))
   notices.update(true,1);await(1,AppLanguages.wrap(context).resources.getQuantityString(R.plurals.reminders_due,1,1))
   notices.update(true,0);await(0)
   notices.update(true,1);await(1);notices.update(false,1);await(0)
  } finally {notices.clear()}
 }
}
