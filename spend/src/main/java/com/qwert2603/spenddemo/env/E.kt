package com.qwert2603.spenddemo.env

import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.BuildConfig

object E {
    val env = when (BuildConfig.FLAVOR_server) {
        "noServer" -> NoServer
        "serverTest" -> ServerTest
        "serverProd" -> ServerProd
        else -> null!!
    }
}

abstract class Env {
    val restBaseUrl: String by lazy { "$serverUrl/api/v1.0/" }
    protected abstract val serverUrl: String
    abstract val syncWithServer: Boolean

    fun buildForTesting() = BuildConfig.FLAVOR_aim == "forTesting"

    val logType =
            if (BuildConfig.DEBUG || buildForTesting()) {
                LogUtils.LogType.ANDROID
            } else {
                LogUtils.LogType.ANDROID_ERRORS
            }
}

private object NoServer : Env() {
    override val serverUrl = "http://nth.ca"
    override val syncWithServer = false
}

private object ServerTest : Env() {
    override val serverUrl = "http://192.168.1.26:8359"
    override val syncWithServer = true
}

private object ServerProd : Env() {
    override val serverUrl = "http://192.168.1.26:8354"
    override val syncWithServer = true
}