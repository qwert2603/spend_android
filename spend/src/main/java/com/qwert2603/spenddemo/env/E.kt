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

interface Env {
    val remoteTableNameSpends: String
    val remoteTableNameProfits: String
    val showIdsSetting: Boolean
    val showChangeKindsSetting: Boolean
    val syncWithServer: Boolean

    fun titleSuffix(): String = listOfNotNull(
            BuildConfig.FLAVOR_server,
            BuildConfig.FLAVOR_aim.takeIf { it != "forMarket" },
            BuildConfig.BUILD_TYPE.takeIf { it != "release" }
    ).reduce { acc, s -> "$acc $s" }

    fun buildForTesting() = BuildConfig.FLAVOR_aim == "forTesting"
}

private object NoServer : Env {
    override val remoteTableNameSpends by lazy { null!! } // must never be called.
    override val remoteTableNameProfits by lazy { null!! } // must never be called.
    override val showIdsSetting = true
    override val showChangeKindsSetting = false
    override val syncWithServer = false
}

private object ServerTest : Env {
    override val remoteTableNameSpends = "test_spend"
    override val remoteTableNameProfits = "test_profit"
    override val showIdsSetting = true
    override val showChangeKindsSetting = true
    override val syncWithServer = true
}

private object ServerProd : Env {
    override val remoteTableNameSpends = "spend"
    override val remoteTableNameProfits = "profit"
    override val showIdsSetting = true
    override val showChangeKindsSetting = true
    override val syncWithServer = true
}