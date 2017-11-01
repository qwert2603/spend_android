package com.qwert2603.spenddemo

import android.app.Application
import com.facebook.stetho.Stetho
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.di.DIManager
import com.qwert2603.spenddemo.utils.LogUtils
import io.reactivex.plugins.RxJavaPlugins

class SpendDemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Stetho.initializeWithDefaults(this)

        DIHolder.diManager = DIManager(this)

        try {
            Class.forName(org.postgresql.Driver::class.java.name)
        } catch (e: ClassNotFoundException) {
            LogUtils.e("postgresql", e)
        }

        RxJavaPlugins.setErrorHandler {
            LogUtils.e("RxJavaPlugins.setErrorHandler", it)
            var cause = it.cause
            while (cause != null) {
                LogUtils.e("RxJavaPlugins.setErrorHandler", cause)
                cause = cause.cause
            }
        }
    }
}
