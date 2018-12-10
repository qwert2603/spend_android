package com.qwert2603.spenddemo.records_list

import com.qwert2603.andrlib.util.mapList
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.RecordCategory
import com.qwert2603.spenddemo.model.entity.RecordDraft
import com.qwert2603.spenddemo.model.entity.SyncState
import com.qwert2603.spenddemo.model.repo.RecordAggregationsRepo
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.model.repo.UserSettingsRepo
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class RecordsListInteractor @Inject constructor(
        private val recordsRepo: RecordsRepo,
        private val recordAggregationsRepo: RecordAggregationsRepo,
        private val userSettingsRepo: UserSettingsRepo
) {
    fun getRecordsList(): Observable<List<Record>> = recordsRepo.getRecordsList()

    fun getLocalChangesCount(recordTypeIds: List<Long>) = recordsRepo.getLocalChangesCount(recordTypeIds)

    fun getSumLastDays(recordTypeId: Long, days: Int): Observable<Long> = recordsRepo.getSumLastDays(recordTypeId, days)

    fun getSumLastMinutes(recordTypeId: Long, minutes: Int): Observable<Long> = recordsRepo.getSumLastMinutes(recordTypeId, minutes)

    fun getRecordCreatedLocallyEvents(): Observable<String> = recordsRepo.getRecordCreatedLocallyEvents()

    fun getRecordEditedLocallyEvents(): Observable<String> = recordsRepo.getRecordEditedLocallyEvents()

    fun addRecords(records: List<RecordDraft>) {
        recordsRepo.saveRecords(records)
    }

    fun removeAllRecords() {
        recordsRepo.removeAllRecords()
    }

    var showInfo: ShowInfo
        get() = ShowInfo(
                userSettingsRepo.showSpends,
                userSettingsRepo.showProfits,
                userSettingsRepo.showSums,
                userSettingsRepo.showChangeKinds,
                userSettingsRepo.showTimes
        )
        set(value) {
            userSettingsRepo.showSpends = value.showSpends
            userSettingsRepo.showProfits = value.showProfits
            userSettingsRepo.showSums = value.showSums
            userSettingsRepo.showChangeKinds = value.showChangeKinds
            userSettingsRepo.showTimes = value.showTimes
        }

    var longSumPeriodDays: Int
        get() = userSettingsRepo.longSumPeriodDays
        set(value) {
            userSettingsRepo.longSumPeriodDays = value
        }

    var shortSumPeriodMinutes: Int
        get() = userSettingsRepo.shortSumPeriodMinutes
        set(value) {
            userSettingsRepo.shortSumPeriodMinutes = value
        }

    fun getRecordCategories(recordTypeId: Long): Single<List<RecordCategory>> = recordAggregationsRepo
            .getRecordCategories(recordTypeId).firstOrError()
            .mapList { it.recordCategory }

    fun getSyncState(): Observable<SyncState> = recordsRepo.getSyncState()
}