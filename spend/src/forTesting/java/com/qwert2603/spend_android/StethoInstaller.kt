package com.qwert2603.spend_android

import android.content.Context
import com.facebook.stetho.Stetho

object StethoInstaller {
    fun install(appContext: Context) {
        Stetho.initializeWithDefaults(appContext)
    }
}