package com.qwert2603.spenddemo.env

import com.qwert2603.spenddemo.BuildConfig

object E {
    val env = when (BuildConfig.FLAVOR) {
        "noServer" -> NoServer
        "serverTest" -> ServerTest
        "serverProd" -> ServerProd
        else -> null!!
    }
}

interface Env {
    val remoteTableName: String
    val showIdsSetting: Boolean
    val showChangeKindsSetting: Boolean

    fun titleSuffix(): String = listOfNotNull(
            BuildConfig.FLAVOR,
            BuildConfig.BUILD_TYPE.takeIf { it != "release" }
    ).reduce { acc, s -> "$acc $s" }

    fun showTestingButtons() = true
}

private object NoServer : Env {
    override val remoteTableName by lazy { null!! } // must never be called.
    override val showIdsSetting = true
    override val showChangeKindsSetting = false
}

private object ServerTest : Env {
    override val remoteTableName = "test_spend"
    override val showIdsSetting = true
    override val showChangeKindsSetting = true
}

private object ServerProd : Env {
    override val remoteTableName = "spend"
    override val showIdsSetting = true
    override val showChangeKindsSetting = true
}