package com.qwert2603.spenddemo.records_list_view

import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import io.reactivex.Observable
import javax.inject.Inject

class RecordsListInteractor @Inject constructor(
        private val recordsRepo: RecordsRepo
) {
    fun getRecordsList(recordsUuids: List<String>): Observable<List<Record>> = recordsRepo
            .getRecordsList()
            .map { records -> records.filter { it.uuid in recordsUuids } }
            .distinctUntilChanged()
}