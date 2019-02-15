package com.qwert2603.spend.env

import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spend.BuildConfig

object E {
    val env = when (BuildConfig.FLAVOR_server) {
        "serverTest" -> ServerTest
        "serverProd" -> ServerProd
        "serverMother" -> ServerMother
        else -> null!!
    }
}

abstract class Env {
    val restBaseUrl: String by lazy { "$serverUrl/api/v2.0/" }
    protected abstract val serverUrl: String

    fun buildForTesting() = BuildConfig.FLAVOR_aim == "forTesting"

    val logType =
            if (BuildConfig.DEBUG || buildForTesting()) {
                LogUtils.LogType.ANDROID
            } else {
                LogUtils.LogType.ANDROID_ERRORS
            }

    abstract val token: String
}

private object ServerTest : Env() {
    override val serverUrl = "http://192.168.1.26:8359"
    override val token = "137456b7-3824-4927-bc7a-01bfaea0f911"
}

private object ServerProd : Env() {
    override val serverUrl = "http://192.168.1.26:8354"
    override val token = TODO()
}

private object ServerMother : Env() {
    override val serverUrl = "http://192.168.1.26:8361"
    override val token = TODO()
}