package com.qwert2603.spenddemo.model.repo

interface UserSettingsRepo {
    var showIds: Boolean
    var showChangeKinds: Boolean
    var showDateSums: Boolean
    var showMonthSums: Boolean
    var showSpends: Boolean
    var showProfits: Boolean
}