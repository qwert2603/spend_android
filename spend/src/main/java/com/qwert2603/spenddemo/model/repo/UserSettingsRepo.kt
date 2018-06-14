package com.qwert2603.spenddemo.model.repo

import io.reactivex.Observable

interface UserSettingsRepo {
    var showIds: Boolean
    var showChangeKinds: Boolean
    var showDateSums: Boolean
    var showMonthSums: Boolean
    var showSpends: Boolean
    var showProfits: Boolean
    var showBalance: Boolean
    var showTimes: Boolean

    fun showTimesChanges(): Observable<Boolean>
}