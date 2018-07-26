package com.qwert2603.spenddemo.model.repo

import com.qwert2603.spenddemo.model.entity.ServerInfo
import io.reactivex.Observable

interface UserSettingsRepo {
    var showIds: Boolean
    var showChangeKinds: Boolean
    var showDateSums: Boolean
    var showMonthSums: Boolean
    var showSpends: Boolean
    var showProfits: Boolean
    var showTimes: Boolean
    var showDeleted: Boolean

    var longSumPeriodDays: Int
    var shortSumPeriodMinutes: Int

    var serverInfo: ServerInfo

    fun serverInfoChanges(): Observable<ServerInfo>
}