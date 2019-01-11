package com.qwert2603.spenddemo

import android.app.Application
import android.os.Looper
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spend_android.StethoInstaller
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.di.DIManager
import com.qwert2603.spenddemo.env.E
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.plugins.RxJavaPlugins
import java.net.ConnectException

class SpendDemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        StethoInstaller.install(this)

        DIHolder.diManager = DIManager(this)

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
    }
}