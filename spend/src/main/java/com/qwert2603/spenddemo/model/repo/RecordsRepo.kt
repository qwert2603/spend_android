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

    fun getSumLastDays(recordTypeId: Long, days: Days, recordsFilters: RecordsFilters? = null): Observable<Long>

    fun getSumLastMinutes(recordTypeId: Long, minutes: Minutes, recordsFilters: RecordsFilters? = null): Observable<Long>

    fun getDumpFile(): Single<File>

    fun getRecordCreatedLocallyEvents(): Observable<String>

    fun getRecordEditedLocallyEvents(): Observable<String>

    fun getRecordCombinedLocallyEvents(): Observable<String>

    fun getLocalChangesCount(recordTypeIds: List<Long>): Observable<Int>

    fun saveRecords(records: List<RecordDraft>)

    fun removeRecords(recordsUuids: List<String>)

    fun removeAllRecords()

    fun getSyncState(): Observable<SyncState>

    /**
     * Combine records with [Record.uuid] in [recordUuids] in one new [Record].
     * Records with [Record.recordCategory.uuid] != [categoryUuid] or [Record.kind] != [kind] will NOT be combined.
     * Combined records will be locally deleted ([Record.change.isDelete] equal to [true]).
     * New created record will have [Record.date] and [Record.time] same as latest combined [Record].
     * New created record will have [Record.value] equal to sum of [Record.value] of combined records.
     * New created record will have [Record.change.isDelete] equal to [false].
     */
    @Suppress("KDocUnresolvedReference")
    fun combineRecords(
            recordUuids: List<String>,
            categoryUuid: String,
            kind: String
    )

    fun changeRecords(
            recordsUuids: List<String>,
            changedDate: SDate?,
            changedTime: Wrapper<STime>?
    )
}