package com.qwert2603.spend.model.sync_processor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.analytics.FirebaseAnalytics
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.andrlib.util.addTo
import com.qwert2603.spend.R
import com.qwert2603.spend.SpendApplication
import com.qwert2603.spend.model.entity.SyncState
import com.qwert2603.spend.navigation.MainActivity
import io.reactivex.disposables.CompositeDisposable
import org.koin.core.KoinComponent
import org.koin.core.inject

class SyncWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams), KoinComponent {

    private val syncProcessor: SyncProcessor by inject()

    private val disposable = CompositeDisposable()

    @Volatile
    var result: Result? = null

    override fun doWork(): Result {

        LogUtils.d("SyncWorker doWork")
        SpendApplication.debugHolder.logLine { "SyncWorker doWork" }

        var updatedRecordsCount: Int? = null

        syncProcessor.syncState
                .skip(1)
                .filter { it is SyncState.Synced || it is SyncState.Error }
                .firstOrError()
                .subscribe { syncState, t ->
                    syncState?.let { LogUtils.d("SyncWorker subscribe $it") }
                    t?.let { LogUtils.e("SyncWorker subscribe", it) }
                    SpendApplication.debugHolder.logLine { "SyncWorker subscribe $syncState $t" }

                    result = if (t == null && syncState is SyncState.Synced) {
                        if (syncState.updatedRecordsCount > 0) {
                            updatedRecordsCount = syncState.updatedRecordsCount
                            showNotification(true)
                        }
                        Result.success()
                    } else {
                        Result.failure()
                    }
                }
                .addTo(disposable)

        syncProcessor.makeOneSync()

        while (result == null) Thread.yield()

        LogUtils.d("SyncWorker return $result")
        SpendApplication.debugHolder.logLine { "SyncWorker return $result" }
        FirebaseAnalytics.getInstance(applicationContext).logEvent(
                "SyncWorker",
                Bundle().also {
                    it.putString("key", (result is Result.Success).toString())
                    val recordsCount = updatedRecordsCount
                    if (recordsCount != null) {
                        it.putInt("records_count", recordsCount)
                    }
                }
        )

        return result!!
    }

    override fun onStopped() {
        super.onStopped()
        disposable.dispose()
        result = Result.failure()
        LogUtils.d("SyncWorker onStopped")
        SpendApplication.debugHolder.logLine { "SyncWorker onStopped" }
    }

    private fun showNotification(success: Boolean) {

        createNotificationChannel()

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val notification = NotificationCompat
                .Builder(
                        applicationContext,
                        applicationContext.getString(R.string.notification_channel_sync_id)
                )
                .setShowWhen(true)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(applicationContext.getString(R.string.app_name))
                .setContentText(applicationContext.getString(if (success) {
                    R.string.notification_text_sync_done
                } else {
                    R.string.notification_text_sync_error
                }))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

        NotificationManagerCompat.from(applicationContext).notify(1, notification)
    }

    private fun createNotificationChannel() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                    applicationContext.getString(R.string.notification_channel_sync_id),
                    applicationContext.getString(R.string.notification_channel_sync_name),
                    NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}