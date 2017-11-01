package com.qwert2603.spenddemo.model.repo

import com.qwert2603.spenddemo.model.entity.CreatingRecord
import com.qwert2603.spenddemo.model.entity.Record
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface RecordsRepo {
    fun addRecord(creatingRecord: CreatingRecord, localId: Long? = null): Single<Record>
    fun editRecord(record: Record): Completable
    fun removeRecord(recordId: Long): Completable

    fun recordsState(): Observable<RecordsState>
}

