package com.qwert2603.spend

import android.app.Application
import android.os.Looper
import androidx.work.WorkManager
import com.crashlytics.android.Crashlytics
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spend_android.StethoInstaller
import com.qwert2603.spend.di.DIHolder
import com.qwert2603.spend.di.DIManager
import com.qwert2603.spend.env.E
import com.qwert2603.spend.model.sync_processor.SyncWorkReceiver
import com.qwert2603.spend.utils.DateUtils
import com.qwert2603.spend.utils.DebugHolder
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.plugins.RxJavaPlugins
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class SpendApplication : Application() {

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

        setupLogs()
        Crashlytics.setString("launch_time", DateUtils.getNow().toString())

        SyncWorkReceiver.scheduleNext(this)
        logSyncWorker()
    }

    companion object {

        lateinit var debugHolder: DebugHolder

        const val UNIQUE_WORK_NAME = "sync_records"

        private fun logSyncWorker() {
            WorkManager.getInstance()
                    .getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_NAME)
                    .observeForever { LogUtils.d { "SyncWorker observeForever $it" } }
        }

        private fun setupLogs() {
            LogUtils.APP_TAG = "spend"
            LogUtils.logType = E.env.logType
            LogUtils.errorsFilter = {
                when (it) {
                    is ConnectException -> false
                    is UnknownHostException -> false
                    is SocketTimeoutException -> false
                    else -> true
                }
            }
            LogUtils.onErrorLogged = { tag, msg, t ->
                LogUtils.d("onErrorLogged") { "$tag $msg $t" }
                Crashlytics.log("$tag $msg $t")
                Crashlytics.logException(t)
            }
        }
    }
}