package com.qwert2603.spend.model.repo

import com.qwert2603.spend.model.entity.*
import com.qwert2603.spend.utils.ObservableField
import io.reactivex.Observable

interface UserSettingsRepo {
    val longSumPeriod: ObservableField<Days>
    val shortSumPeriod: ObservableField<Minutes>
    val showInfo: ObservableField<ShowInfo>
    val sumsShowInfo: ObservableField<SumsShowInfo>

    fun setOldRecordsLock(lock: Boolean)
    fun oldRecordsLockStateChanges(): Observable<OldRecordsLockState>
}