package com.qwert2603.spenddemo

import android.app.Application
import android.os.Looper
import androidx.work.*
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spend_android.StethoInstaller
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.di.DIManager
import com.qwert2603.spenddemo.env.E
import com.qwert2603.spenddemo.model.sync_processor.SyncWorker
import com.qwert2603.spenddemo.utils.DebugHolder
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.plugins.RxJavaPlugins
import java.net.ConnectException
import java.util.concurrent.TimeUnit

class SpendDemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        StethoInstaller.install(this)

        DIHolder.diManager = DIManager(this)

        debugHolder = DebugHolder(this)

        RxJavaPlugins.setErrorHandler {
            LogUtils.e("RxJavaPlugins.setErrorHandler", it)
            var cause = it.cause
            while (cause != null) {
                LogUtils.e("RxJavaPlugins.setErrorHandler", cause)
                cause = cause.cause
            }
        }

        RxAndroidPlugins.setInitMainThreadSchedulerHandler {
            AndroidSchedulers.from(Looper.getMainLooper(), true)
        }

//        lrModelChangerInstance = LRModelChangerImpl()
//        listModelChangerInstance = ListModelChangerImpl()

        LogUtils.logType = E.env.logType
        LogUtils.errorsFilter = { it !is ConnectException }

        scheduleSync()
        logSyncWorker()
    }

    companion object {

        lateinit var debugHolder: DebugHolder

        private const val uniqueWorkName = "sync_records"

        fun scheduleSync() {
            val workRequest = OneTimeWorkRequest.Builder(SyncWorker::class.java)
                    .setInitialDelay(1L, TimeUnit.MINUTES)
                    .setConstraints(Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .build()

            WorkManager.getInstance()
                    .enqueueUniqueWork(uniqueWorkName, ExistingWorkPolicy.REPLACE, workRequest)
        }

        private fun logSyncWorker() {
            WorkManager.getInstance()
                    .getWorkInfosForUniqueWorkLiveData(uniqueWorkName)
                    .observeForever {
                        LogUtils.d { "SyncWorker observeForever $it" }
                        debugHolder.logLine { "SyncWorker observeForever $it" }
                    }
        }
    }
}