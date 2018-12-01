package com.qwert2603.spenddemo.model.repo

import com.qwert2603.spenddemo.model.entity.SDate
import com.qwert2603.spenddemo.model.entity.STime

interface UserSettingsRepo {
    var showSpends: Boolean
    var showProfits: Boolean
    var showSums: Boolean
    var showChangeKinds: Boolean
    var showTimes: Boolean

    var longSumPeriodDays: Int
    var shortSumPeriodMinutes: Int

    // todo: use it
    val fixedTime: Pair<SDate, STime?>?
}