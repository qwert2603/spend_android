package com.qwert2603.spenddemo

import android.app.Application
import com.facebook.stetho.Stetho
import com.qwert2603.andrlib.base.mvi.load_refresh.list.listModelChangerInstance
import com.qwert2603.andrlib.base.mvi.load_refresh.lrModelChangerInstance
import com.qwert2603.andrlib.generated.LRModelChangerImpl
import com.qwert2603.andrlib.generated.ListModelChangerImpl
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.di.DIManager
import com.qwert2603.spenddemo.env.E
import io.reactivex.plugins.RxJavaPlugins

class SpendDemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) Stetho.initializeWithDefaults(this)

        DIHolder.diManager = DIManager(this)

        RxJavaPlugins.setErrorHandler {
            LogUtils.e("RxJavaPlugins.setErrorHandler", it)
            var cause = it.cause
            while (cause != null) {
                LogUtils.e("RxJavaPlugins.setErrorHandler", cause)
                cause = cause.cause
            }
        }

        lrModelChangerInstance = LRModelChangerImpl()
        listModelChangerInstance = ListModelChangerImpl()

        LogUtils.logType = if (BuildConfig.DEBUG || E.env.buildForTesting()) {
            LogUtils.LogType.ANDROID
        } else {
            LogUtils.LogType.ANDROID_ERRORS
        }
    }

    // todo: stat screen.
    // todo: App Shortcuts for frequent spends.
    // todo: widget for adding spends.
}