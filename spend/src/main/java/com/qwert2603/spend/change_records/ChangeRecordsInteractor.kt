package com.qwert2603.spend.change_records

import com.qwert2603.spend.model.entity.OldRecordsLockState
import com.qwert2603.spend.model.entity.SDate
import com.qwert2603.spend.model.entity.STime
import com.qwert2603.spend.model.repo.RecordsRepo
import com.qwert2603.spend.model.repo.UserSettingsRepo
import com.qwert2603.spend.utils.Wrapper
import io.reactivex.Observable

class ChangeRecordsInteractor(
        private val recordsRepo: RecordsRepo,
        private val userSettingsRepo: UserSettingsRepo
) {
    fun changeRecords(
            recordsUuids: List<String>,
            changedDate: SDate?,
            changedTime: Wrapper<STime>?
    ) = recordsRepo.changeRecords(recordsUuids, changedDate, changedTime)

    fun oldRecordsLockStateChanges(): Observable<OldRecordsLockState> = userSettingsRepo.oldRecordsLockStateChanges()
}