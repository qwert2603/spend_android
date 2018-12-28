package com.qwert2603.spenddemo.model.repo

import com.qwert2603.spenddemo.model.entity.*
import com.qwert2603.spenddemo.utils.Wrapper
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File

interface RecordsRepo {

    /**
     * List of all records sorted by
     * - [Record.date] DESC
     * - [Record.time] DESC NULLS LAST
     * - [RecordCategory.recordTypeId]
     * - [RecordCategory.name] DESC
     * - [Record.kind] DESC
     * - [Record.uuid]
     */
    fun getRecordsList(): Observable<List<Record>>

    fun getRecord(uuid: String): Observable<Wrapper<Record>>

    fun getSumLastDays(recordTypeId: Long, days: Days): Observable<Long>

    fun getSumLastMinutes(recordTypeId: Long, minutes: Minutes): Observable<Long>

    fun getDumpFile(): Single<File>

    fun getRecordCreatedLocallyEvents(): Observable<String>

    fun getRecordEditedLocallyEvents(): Observable<String>

    fun getLocalChangesCount(recordTypeIds: List<Long>): Observable<Int>

    fun saveRecords(records: List<RecordDraft>)

    fun removeRecords(recordsUuids: List<String>)

    fun removeAllRecords()

    fun getSyncState(): Observable<SyncState>

    fun combineRecords(
            recordUuids: List<String>,
            categoryUuid: String,
            kind: String
    )
}