package com.qwert2603.spenddemo.model.repo

import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.RecordDraft
import com.qwert2603.spenddemo.utils.Wrapper
import io.reactivex.Observable
import io.reactivex.Single

interface RecordsRepo {

    /**
     * List of all records sorted by
     * - [Record.date] DESC
     * - [Record.time] DESC NULLS LAST
     * - [Record.recordTypeId]
     * - [Record.kind] DESC
     * - [Record.uuid]
     */
    fun getRecordsList(): Observable<List<Record>>

    fun getRecord(uuid: String): Observable<Wrapper<Record>>

    fun getSumLastDays(recordTypeId: Long, days: Int): Observable<Long>

    fun getSumLastMinutes(recordTypeId: Long, minutes: Int): Observable<Long>

    fun getDumpText(): Single<String>

    fun getSyncingRecordsUuids(): Observable<Set<String>>

    fun getRecordCreatedLocallyEvents(): Observable<String>

    fun getLocalChangesCount(recordTypeIds: List<Long>): Observable<Int>

    fun saveRecords(records: List<RecordDraft>)

    fun removeRecords(recordsUuids: List<String>)

    fun removeAllRecords()
}