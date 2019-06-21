package com.qwert2603.spend.sums

import com.qwert2603.spend.model.entity.Record
import com.qwert2603.spend.model.entity.SumsShowInfo
import com.qwert2603.spend.model.entity.SyncState
import com.qwert2603.spend.model.repo.RecordsRepo
import com.qwert2603.spend.model.repo.UserSettingsRepo
import com.qwert2603.spend.utils.ObservableField
import io.reactivex.Observable

class SumsInteractor(
        private val recordsRepo: RecordsRepo,
        userSettingsRepo: UserSettingsRepo
) {
    fun getRecordsList(): Observable<List<Record>> = recordsRepo.getRecordsList()

    fun getSyncState(): Observable<SyncState> = recordsRepo.getSyncState()

    val sumsShowInfo: ObservableField<SumsShowInfo> = userSettingsRepo.sumsShowInfo

    fun removeAllRecords() {
        recordsRepo.removeAllRecords()
    }
}