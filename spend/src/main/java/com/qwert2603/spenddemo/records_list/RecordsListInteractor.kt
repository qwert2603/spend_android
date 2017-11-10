package com.qwert2603.spenddemo.records_list

import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.RecordsState
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import io.reactivex.Observable
import javax.inject.Inject

class RecordsListInteractor @Inject constructor(
        private val recordsRepo: RecordsRepo
) {

    fun deleteRecord(id: Long) {
        recordsRepo.removeRecord(id)
    }

    fun editRecord(record: Record) {
        recordsRepo.editRecord(record)
    }

    fun recordsState(): Observable<RecordsState> = recordsRepo.recordsState()

    fun recordCreatedEvents(): Observable<Record> = recordsRepo.recordCreatedEvents()
}