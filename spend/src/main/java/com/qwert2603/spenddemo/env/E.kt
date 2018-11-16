package com.qwert2603.spenddemo.env

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

    fun titleSuffix(): String = listOfNotNull(
            BuildConfig.FLAVOR_server,
            BuildConfig.FLAVOR_aim.takeIf { it != "forMarket" },
            BuildConfig.BUILD_TYPE.takeIf { it != "release" }
    ).reduce { acc, s -> "$acc $s" }

    fun buildForTesting() = BuildConfig.FLAVOR_aim == "forTesting"
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