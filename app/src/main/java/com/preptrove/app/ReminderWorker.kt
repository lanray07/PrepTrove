package com.preptrove.app
import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import androidx.work.*
import java.util.concurrent.TimeUnit
class ReminderWorker(context:Context,params:WorkerParameters):CoroutineWorker(context,params) {
 override suspend fun doWork():Result {
  val entries=(applicationContext as PrepTrove).db.entries().all()
  val due=entries.filter { it.kind=="reminder" && !it.done && it.expiry.isNotEmpty() && it.expiry<=java.time.LocalDate.now().toString() }
  if(due.isNotEmpty() && (Build.VERSION.SDK_INT<33 || applicationContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED)) {
   val manager=applicationContext.getSystemService(NotificationManager::class.java)
   manager.createNotificationChannel(NotificationChannel("reviews","Preparedness reviews",NotificationManager.IMPORTANCE_DEFAULT))
   val pending=PendingIntent.getActivity(applicationContext,0,Intent(applicationContext,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
   manager.notify(1,Notification.Builder(applicationContext,"reviews").setSmallIcon(com.preptrove.app.R.drawable.ic_preptrove).setContentTitle("PrepTrove review").setContentText("${due.size} saved reminders are due. Open your planner to review.").setContentIntent(pending).setAutoCancel(true).build())
  };return Result.success()
 }
 companion object { fun schedule(context:Context) { WorkManager.getInstance(context).enqueueUniquePeriodicWork("reviews",ExistingPeriodicWorkPolicy.KEEP,PeriodicWorkRequestBuilder<ReminderWorker>(24,TimeUnit.HOURS).build()) } }
}
