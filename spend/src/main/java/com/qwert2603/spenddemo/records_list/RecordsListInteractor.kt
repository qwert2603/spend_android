package com.qwert2603.spenddemo.records_list

import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.RecordsState
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.utils.Const
import io.reactivex.Observable
import io.reactivex.Single
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

    fun getRecordsTextToSend(): Single<String> = recordsRepo.recordsState()
            .firstOrError()
            .map {
                if (it.records.isEmpty()) return@map ""
                it.records
                        .reversed()
                        .map { "${Const.DATE_FORMAT.format(it.date)}\t${it.kind}\t${it.value}" }
                        .reduce { s1, s2 -> "$s1\n$s2" }
            }

}