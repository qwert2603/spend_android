package com.qwert2603.spend.records_list

import com.qwert2603.andrlib.util.mapList
import com.qwert2603.spend.model.entity.*
import com.qwert2603.spend.model.repo.RecordAggregationsRepo
import com.qwert2603.spend.model.repo.RecordsRepo
import com.qwert2603.spend.model.repo.UserSettingsRepo
import io.reactivex.Observable
import io.reactivex.Single

class RecordsListInteractor(
        private val recordsRepo: RecordsRepo,
        private val recordAggregationsRepo: RecordAggregationsRepo,
        userSettingsRepo: UserSettingsRepo
) {
    fun getRecordsList(): Observable<List<Record>> = recordsRepo.getRecordsList()

    fun getLocalChangesCount(recordTypeIds: List<Long>) = recordsRepo.getLocalChangesCount(recordTypeIds)

    fun getSumLastDays(recordTypeId: Long, days: Days, recordsFilters: RecordsFilters?): Observable<Long> =
            recordsRepo.getSumLastDays(recordTypeId, days, recordsFilters)

    fun getSumLastMinutes(recordTypeId: Long, minutes: Minutes, recordsFilters: RecordsFilters?): Observable<Long> =
            recordsRepo.getSumLastMinutes(recordTypeId, minutes, recordsFilters)

    fun getRecordCreatedLocallyEvents(): Observable<String> = recordsRepo.getRecordCreatedLocallyEvents()

    fun getRecordEditedLocallyEvents(): Observable<String> = recordsRepo.getRecordEditedLocallyEvents()

    fun getRecordCombinedLocallyEvents(): Observable<String> = recordsRepo.getRecordCombinedLocallyEvents()

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