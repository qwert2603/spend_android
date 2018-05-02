package com.qwert2603.spenddemo.model.repo

interface UserSettingsRepo {
    var showIds: Boolean
    var showChangeKinds: Boolean
    var showChangeDateSums: Boolean
    var showChangeSpends: Boolean
    var showChangeProfits: Boolean
}