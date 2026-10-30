package com.preptrove.app
import android.content.*
import androidx.work.*
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.currentCoroutineContext
import java.util.concurrent.TimeUnit
class ReminderWorker(context:Context,params:WorkerParameters):CoroutineWorker(context,params) {
 override suspend fun doWork():Result = notificationMutex.withLock {
  val enabled=applicationContext.preferences.data.first()[booleanPreferencesKey("remindersEnabled")] ?: false
  if(!enabled) {ReviewNotifications(applicationContext).clear();return@withLock Result.success()}
  val entries=(applicationContext as PrepTrove).db.entries().all()
  val due=entries.filter { it.kind=="reminder" && !it.isTemplate && !it.done && it.expiry.isNotEmpty() && it.expiry<=java.time.LocalDate.now().toString() }
  currentCoroutineContext().ensureActive()
  ReviewNotifications(applicationContext).update(enabled,due.size)
  Result.success()
 }
 companion object {
  private val notificationMutex=Mutex()
  suspend fun setEnabled(context:Context,value:Boolean) {
   notificationMutex.withLock {
    context.preferences.edit {it[booleanPreferencesKey("remindersEnabled")]=value}
    if(!value) ReviewNotifications(context).clear()
   }
   refresh(context)
  }
  fun schedule(context:Context) { WorkManager.getInstance(context).enqueueUniquePeriodicWork("reviews",ExistingPeriodicWorkPolicy.KEEP,PeriodicWorkRequestBuilder<ReminderWorker>(24,TimeUnit.HOURS).build()) }
  fun refresh(context:Context) { WorkManager.getInstance(context).enqueueUniqueWork("reviews-now",ExistingWorkPolicy.APPEND_OR_REPLACE,OneTimeWorkRequestBuilder<ReminderWorker>().build()) }
 }
}
