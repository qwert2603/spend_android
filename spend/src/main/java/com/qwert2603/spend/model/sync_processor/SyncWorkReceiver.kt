package com.qwert2603.spend.model.sync_processor

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.core.app.AlarmManagerCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.qwert2603.andrlib.util.Const
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spend.SpendApplication

class SyncWorkReceiver : BroadcastReceiver() {

    companion object {
        fun scheduleNext(appContext: Context) {
            val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = PendingIntent.getBroadcast(
                appContext,
                0,
                Intent(appContext, SyncWorkReceiver::class.java),
                PendingIntent.FLAG_IMMUTABLE,
            )
            AlarmManagerCompat.setAndAllowWhileIdle(
                alarmManager,
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 42 * Const.MILLIS_PER_MINUTE,
                pendingIntent
            )
        }
    }

    override fun onReceive(context: Context, intent: Intent) {

        LogUtils.d("SyncWorkReceiver onReceive")
        SpendApplication.debugHolder.logLine { "SyncWorkReceiver onReceive" }

        scheduleNext(context.applicationContext)

        val workRequest = OneTimeWorkRequest.Builder(SyncWorker::class.java)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                SpendApplication.UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }
}