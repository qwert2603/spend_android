package com.qwert2603.spenddemo.model.repo

import com.qwert2603.spenddemo.model.entity.CreatingRecord
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.RecordsState
import io.reactivex.Observable
import io.reactivex.Single

interface RecordsRepo {
    fun addRecord(creatingRecord: CreatingRecord)
    fun editRecord(record: Record)
    fun removeRecord(recordId: Long)
    fun removeAllRecords()

    fun recordsState(): Observable<RecordsState>
    fun recordCreatedEvents(): Observable<Record>

    fun getDumpText(): Single<String>
}

