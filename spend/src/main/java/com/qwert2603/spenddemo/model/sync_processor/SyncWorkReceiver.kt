package com.qwert2603.spenddemo.model.sync_processor

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.support.v4.app.AlarmManagerCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.qwert2603.andrlib.util.Const
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.SpendDemoApplication

class SyncWorkReceiver : BroadcastReceiver() {

    companion object {
        fun scheduleNext(appContext: Context) {
            val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            AlarmManagerCompat.setAndAllowWhileIdle(
                    alarmManager,
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 1 * Const.MILLIS_PER_MINUTE,
                    PendingIntent.getBroadcast(appContext, 0, Intent(appContext, SyncWorkReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
            )
        }
    }

    override fun onReceive(context: Context, intent: Intent) {

        LogUtils.d("SyncWorkReceiver onReceive")
        SpendDemoApplication.debugHolder.logLine { "SyncWorkReceiver onReceive" }

        scheduleNext(context.applicationContext)

        val workRequest = OneTimeWorkRequest.Builder(SyncWorker::class.java)
                .build()

        WorkManager.getInstance()
                .enqueueUniqueWork(
                        SpendDemoApplication.uniqueWorkName,
                        ExistingWorkPolicy.REPLACE,
                        workRequest
                )
    }
}