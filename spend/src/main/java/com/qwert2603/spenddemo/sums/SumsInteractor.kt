package com.qwert2603.spenddemo.sums

import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.SumsShowInfo
import com.qwert2603.spenddemo.model.entity.SyncState
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.model.repo.UserSettingsRepo
import com.qwert2603.spenddemo.utils.ObservableField
import io.reactivex.Observable
import javax.inject.Inject

class SumsInteractor @Inject constructor(
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