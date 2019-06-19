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
    override val serverUrl = "http://192.168.43.66:8359"
    override val token = "d75fa6fd-bc6b-4de1-85d1-02b8330a9478"
}

private object ServerProd : Env() {
    override val serverUrl = "http://192.168.1.26:8354"
    override val token = "731ee825-619d-4ba3-a09c-dc24b0b5795b"
}

private object ServerMother : Env() {
    override val serverUrl = "http://192.168.1.26:8354"
    override val token = "56317ae2-437b-4798-b7da-31657d31b43e"
}