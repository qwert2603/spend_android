package com.qwert2603.spenddemo.utils

import android.util.Log

object LogUtils {

    const val APP_TAG = "AASSDD"
    const val ERROR_MSG = "ERROR!!!"
    const val ANDROID_LOGGING = true

    fun d(msg: String) {
        d(APP_TAG, msg)
    }

    fun d(tag: String, msg: String) {
        if (ANDROID_LOGGING) Log.d(tag, msg) else println("$tag $msg")
    }

    fun d(msg: () -> String) {
        d(APP_TAG, msg)
    }

    fun d(tag: String, msg: () -> String) {
        if (ANDROID_LOGGING) Log.d(tag, msg()) else println("$tag ${msg()}")
    }

    @JvmOverloads
    fun e(msg: String = ERROR_MSG, t: Throwable? = null) {
        if (ANDROID_LOGGING) Log.e(APP_TAG, "$msg $t", t) else println("$APP_TAG $msg $t\n${t?.printStackTrace()}")
    }

    fun printCurrentStack() {
        if (ANDROID_LOGGING) Log.v(APP_TAG, "", Exception()) else println("$APP_TAG, ${Exception().printStackTrace()}")
    }

}
