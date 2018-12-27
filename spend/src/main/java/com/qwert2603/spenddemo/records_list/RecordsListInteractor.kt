package com.qwert2603.spenddemo.records_list

import com.qwert2603.andrlib.util.mapList
import com.qwert2603.spenddemo.model.entity.*
import com.qwert2603.spenddemo.model.repo.RecordAggregationsRepo
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.model.repo.UserSettingsRepo
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class RecordsListInteractor @Inject constructor(
        private val recordsRepo: RecordsRepo,
        private val recordAggregationsRepo: RecordAggregationsRepo,
        userSettingsRepo: UserSettingsRepo
) {
    fun getRecordsList(): Observable<List<Record>> = recordsRepo.getRecordsList()

    fun getLocalChangesCount(recordTypeIds: List<Long>) = recordsRepo.getLocalChangesCount(recordTypeIds)

    fun getSumLastDays(recordTypeId: Long, days: Days): Observable<Long> = recordsRepo.getSumLastDays(recordTypeId, days)

    fun getSumLastMinutes(recordTypeId: Long, minutes: Minutes): Observable<Long> = recordsRepo.getSumLastMinutes(recordTypeId, minutes)

    fun getRecordCreatedLocallyEvents(): Observable<String> = recordsRepo.getRecordCreatedLocallyEvents()

    fun getRecordEditedLocallyEvents(): Observable<String> = recordsRepo.getRecordEditedLocallyEvents()

    fun addRecords(records: List<RecordDraft>) {
        recordsRepo.saveRecords(records)
    }

    fun removeAllRecords() {
        recordsRepo.removeAllRecords()
    }

    val shortSumPeriod = userSettingsRepo.shortSumPeriod
    val longSumPeriod = userSettingsRepo.longSumPeriod
    val showInfo = userSettingsRepo.showInfo

    fun getRecordCategories(recordTypeId: Long): Single<List<RecordCategory>> = recordAggregationsRepo
            .getRecordCategories(recordTypeId).firstOrError()
            .mapList { it.recordCategory }

    fun getSyncState(): Observable<SyncState> = recordsRepo.getSyncState()
}