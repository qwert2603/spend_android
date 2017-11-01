package com.qwert2603.spenddemo.records_list

import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.model.repo.RecordsState
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class RecordsListInteractor @Inject constructor(
        private val recordsRepo: RecordsRepo
) {

    fun deleteRecord(id: Long): Completable = recordsRepo.removeRecord(id)

    fun editRecord(record: Record): Completable = recordsRepo.editRecord(record)

    fun recordsState(): Observable<RecordsState> = recordsRepo.recordsState()
}