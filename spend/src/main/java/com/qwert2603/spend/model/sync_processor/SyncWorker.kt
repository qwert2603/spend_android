package com.qwert2603.spend.model.sync_processor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.andrlib.util.addTo
import com.qwert2603.spend.R
import com.qwert2603.spend.SpendApplication
import com.qwert2603.spend.di.DIHolder
import com.qwert2603.spend.model.entity.SyncState
import com.qwert2603.spend.navigation.MainActivity
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class SyncWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    @Inject
    lateinit var syncProcessor: SyncProcessor

    private val disposable = CompositeDisposable()

    @Volatile
    var result: ListenableWorker.Result? = null

    init {
        DIHolder.diManager.viewsComponent.inject(this)
    }

    override fun doWork(): Result {

        LogUtils.d("SyncWorker doWork")
        SpendApplication.debugHolder.logLine { "SyncWorker doWork" }

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