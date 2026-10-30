package com.preptrove.app

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

/** Generic count-only notices. A tag allows independent notification streams to be cleared safely. */
internal class ReviewNotifications(private val context:Context,private val tag:String?=null) {
 private val manager=context.getSystemService(NotificationManager::class.java)
 fun clear() {manager.cancel(tag,1)}
 fun update(enabled:Boolean,dueCount:Int) {
  if(!enabled || dueCount<=0 || !manager.areNotificationsEnabled() ||
   (Build.VERSION.SDK_INT>=33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)) {
   clear();return
  }
  val local=AppLanguages.wrap(context)
  manager.createNotificationChannel(NotificationChannel("reviews",local.getString(R.string.notification_channel),NotificationManager.IMPORTANCE_DEFAULT))
  val pending=PendingIntent.getActivity(context,0,Intent(context,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
  val notification=Notification.Builder(context,"reviews").setSmallIcon(R.drawable.ic_preptrove)
   .setContentTitle(local.getString(R.string.notification_title))
   .setContentText(local.resources.getQuantityString(R.plurals.reminders_due,dueCount,dueCount))
   .setContentIntent(pending).setAutoCancel(true).build()
  try {manager.notify(tag,1,notification)} catch(error:SecurityException) {clear()}
 }
}
