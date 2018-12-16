package com.qwert2603.spenddemo.sums

import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.SyncState
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.model.repo.UserSettingsRepo
import io.reactivex.Observable
import javax.inject.Inject

class SumsInteractor @Inject constructor(
        private val recordsRepo: RecordsRepo,
        private val userSettingsRepo: UserSettingsRepo
) {
    fun getRecordsList(): Observable<List<Record>> = recordsRepo.getRecordsList()

    fun getSyncState(): Observable<SyncState> = recordsRepo.getSyncState()

    var sumsShowInfo: SumsShowInfo
        get() = SumsShowInfo(
                showDaySums = userSettingsRepo.showDaySums,
                showMonthSums = userSettingsRepo.showMonthSums,
                showYearSums = userSettingsRepo.showYearSums,
                showBalances = userSettingsRepo.showBalancesInSums
        )
        set(value) {
            userSettingsRepo.showDaySums = value.showDaySums
            userSettingsRepo.showMonthSums = value.showMonthSums
            userSettingsRepo.showYearSums = value.showYearSums
            userSettingsRepo.showBalancesInSums = value.showBalances
        }

    fun removeAllRecords() {
        recordsRepo.removeAllRecords()
    }
}