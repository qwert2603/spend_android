package com.qwert2603.spenddemo.utils

import android.util.Log
import java.util.*

object LogUtils {

    const val APP_TAG = "AASSDD"
    const val ERROR_MSG = "ERROR!!!"
    const val ANDROID_LOGGING = true

    fun d(msg: String) {
        d(APP_TAG, msg)
    }

    fun d(tag: String, msg: String) {
        if (ANDROID_LOGGING) Log.d(tag, msg) else println("${Date()} $tag $msg")
    }

    fun d(msg: () -> String) {
        d(APP_TAG, msg)
    }

    fun d(tag: String, msg: () -> String) {
        if (ANDROID_LOGGING) Log.d(tag, msg()) else println("${Date()} $tag ${msg()}")
    }

    @JvmOverloads
    fun e(msg: String = ERROR_MSG, t: Throwable? = null) {
        if (ANDROID_LOGGING) Log.e(APP_TAG, "$msg $t", t) else println("${Date()} $APP_TAG $msg $t\n${t?.printStackTrace()}")
    }

    fun e(tag: String = APP_TAG, msg: String = ERROR_MSG, t: Throwable? = null) {
        if (ANDROID_LOGGING) Log.e(tag, "$msg $t", t) else println("${Date()} $tag $msg $t\n${t?.printStackTrace()}")
    }

    fun printCurrentStack() {
        if (ANDROID_LOGGING) Log.v(APP_TAG, "", Exception()) else println("${Date()} $APP_TAG, ${Exception().printStackTrace()}")
    }

}
