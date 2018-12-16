package com.qwert2603.spenddemo.model.repo

interface UserSettingsRepo {
    var showSpends: Boolean
    var showProfits: Boolean
    var showSums: Boolean
    var showChangeKinds: Boolean
    var showTimes: Boolean

    var longSumPeriodDays: Int
    var shortSumPeriodMinutes: Int

    var showDaySums: Boolean
    var showMonthSums: Boolean
    var showYearSums: Boolean
    var showBalancesInSums: Boolean
}