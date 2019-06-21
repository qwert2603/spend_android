package com.qwert2603.spend.records_list_view

import com.qwert2603.spend.model.entity.OldRecordsLockState
import com.qwert2603.spend.model.entity.Record
import com.qwert2603.spend.model.repo.RecordsRepo
import com.qwert2603.spend.model.repo.UserSettingsRepo
import io.reactivex.Observable

class RecordsListInteractor(
        private val recordsRepo: RecordsRepo,
        private val userSettingsRepo: UserSettingsRepo
) {
    fun getRecordsList(recordsUuids: List<String>): Observable<List<Record>> = recordsRepo
            .getRecordsList()
            .map { records ->
                records
                        .filter { it.uuid in recordsUuids }
                        .reversed()
            }
            .distinctUntilChanged()

    fun oldRecordsLockStateChanges(): Observable<OldRecordsLockState> = userSettingsRepo.oldRecordsLockStateChanges()
}