package com.qwert2603.spend.model.local_db.dao

import android.arch.persistence.room.*
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spend.model.entity.*
import com.qwert2603.spend.model.local_db.entity.ChangesFromServer
import com.qwert2603.spend.model.local_db.entity.ItemsIds
import com.qwert2603.spend.model.local_db.results.Dump
import com.qwert2603.spend.model.local_db.results.RecordItemResult
import com.qwert2603.spend.model.local_db.tables.RecordCategoryTable
import com.qwert2603.spend.model.local_db.tables.RecordTable
import com.qwert2603.spend.model.local_db.tables.toRecordCategory
import com.qwert2603.spend.utils.DateUtils
import com.qwert2603.spend.utils.Wrapper
import com.qwert2603.spend.utils.sumByLong
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

@Dao
abstract class RecordsDao {

    val recordsList: Observable<List<Record>> by lazy {
        val behaviorSubject = BehaviorSubject.create<List<Record>>()

        getRecords()
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { behaviorSubject.onNext(it.map { it.toRecord() }) },
                        { LogUtils.e("RecordsDao getRecords error!", it) }
                ).also { }

        behaviorSubject.hide()
    }

    val recordCategoriesList: Observable<List<RecordCategory>> by lazy {
        val behaviorSubject = BehaviorSubject.create<List<RecordCategory>>()

        getRecordCategories()
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { behaviorSubject.onNext(it.map { it.toRecordCategory() }) },
                        { LogUtils.e("RecordsDao getRecords error!", it) }
                ).also { }

        behaviorSubject.hide()
    }

    @Transaction
    @Query("""
            SELECT
                r.uuid,
                c.uuid AS recordCategoryUuid,
                c.recordTypeId,
                c.name AS recordCategoryName,
                r.date,
                r.time,
                r.kind,
                r.value,
                r.change_id AS changeId,
                r.change_isDelete AS isDelete
            FROM RecordTable AS r
            JOIN RecordCategoryTable AS c ON r.recordCategoryUuid = c.uuid
            ORDER BY r.date DESC, coalesce(r.time, -1) DESC, c.recordTypeId, c.name DESC, r.kind DESC, r.uuid
        """)
    protected abstract fun getRecords(): Flowable<List<RecordItemResult>>

    @Transaction
    @Query("SELECT * FROM RecordCategoryTable")
    protected abstract fun getRecordCategories(): Flowable<List<RecordCategoryTable>>

    @Query("SELECT * FROM RecordTable WHERE uuid in (:uuids)")
    protected abstract fun getRecordsByUuid(uuids: List<String>): List<RecordTable>

    @Transaction
    @Query("SELECT uuid FROM RecordTable WHERE change_isDelete IS NOT NULL AND uuid in (:uuids)")
    protected abstract fun getChangedRecordsUuids(uuids: List<String>): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun saveRecordsCategories(recordsCategories: List<RecordCategoryTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveRecords(records: List<RecordTable>)

    @Query("DELETE FROM RecordTable WHERE uuid IN (:uuids)")
    abstract fun deleteRecords(uuids: List<String>)

    @Query("UPDATE RecordTable SET change_isDelete = NULL, change_id = NULL WHERE uuid = :recordUuid AND change_id = :changeId")
    protected abstract fun clearLocalChange(recordUuid: String, changeId: Long)

    @Transaction
    protected open fun clearLocalChanges(recordsIds: List<ItemsIds>) {
        recordsIds.forEach { clearLocalChange(it.recordUuid, it.recordChangeId) }
    }

    @Transaction
    @Query("SELECT * FROM RecordTable WHERE change_isDelete IS NOT NULL LIMIT :limit")
    abstract fun getLocallyChangedRecords(limit: Int): List<RecordTable>

    @Query("UPDATE RecordTable SET change_isDelete = 1, change_id = :changeId WHERE uuid = :uuid")
    protected abstract fun locallyDeleteRecord(uuid: String, changeId: Long)

    @Transaction
    open fun locallyDeleteRecords(recordsIds: List<ItemsIds>) {
        recordsIds.forEach { locallyDeleteRecord(it.recordUuid, it.recordChangeId) }
    }

    @Query("DELETE FROM RecordTable")
    protected abstract fun deleteAllRecords()

    @Query("DELETE FROM RecordCategoryTable")
    protected abstract fun deleteAllRecordsCategories()

    @Transaction
    open fun deleteAll() {
        deleteAllRecords()
        deleteAllRecordsCategories()
    }

    @Transaction
    open fun saveChangesFromServer(changesFromServer: ChangesFromServer) {
        if (changesFromServer.updatedCategories.isNotEmpty()) {
            saveRecordsCategories(changesFromServer.updatedCategories)
        }

        if (changesFromServer.updatedRecords.isNotEmpty()) {
            val changedLocally = getChangedRecordsUuids(changesFromServer.updatedRecords.map { it.uuid })
                    .toHashSet()
            val changesToSave = changesFromServer.updatedRecords
                    .filter { it.uuid !in changedLocally }
            saveRecords(changesToSave)
        }

        if (changesFromServer.deletedRecordsUuid.isNotEmpty()) {
            deleteRecords(changesFromServer.deletedRecordsUuid)
        }
    }

    @Transaction
    open fun onChangesSentToServer(editedRecords: List<ItemsIds>, deletedUuids: List<String>) {
        if (editedRecords.isNotEmpty()) {
            clearLocalChanges(editedRecords)
        }
        if (deletedUuids.isNotEmpty()) {
            deleteRecords(deletedUuids)
        }
    }

    @Query("SELECT * FROM RecordCategoryTable")
    protected abstract fun getDumpRecordCategories(): List<RecordCategoryTable>

    @Query("SELECT * FROM RecordTable")
    protected abstract fun getDumpRecords(): List<RecordTable>

    @Transaction
    open fun getDump() = DateUtils.getNow()
            .let { (sDate, sTime) ->
                Dump(
                        sDate = sDate,
                        sTime = sTime,
                        recordCategories = getDumpRecordCategories(),
                        records = getDumpRecords()
                )
            }

    /**
     * [changeIds] are ids to use when save changes -- locally deleted records and created record.
     * [changeIds.size] MUST be equal [recordUuids.size] + 1.
     * (+ 1) is for created record.
     */
    @Suppress("KDocUnresolvedReference")
    @Transaction
    open fun combineRecords(
            recordUuids: List<String>,
            categoryUuid: String,
            kind: String,
            newRecordUuid: String,
            changeIds: List<Long>
    ) {
        require(changeIds.size == recordUuids.size + 1)

        val recordsToCombine = getRecordsByUuid(recordUuids)
                .filter { it.recordCategoryUuid == categoryUuid && it.kind == kind }
        if (recordsToCombine.size <= 1) return // nothing to combine.

        val dateMultiplier = 10L * 100 * 100
        val lastRecord = recordsToCombine
                .maxBy { it.date * dateMultiplier + if (it.time != null) it.time + 100 * 100 else 0 }!!

        val combinedRecord = RecordTable(
                uuid = newRecordUuid,
                recordCategoryUuid = categoryUuid,
                kind = kind,
                date = lastRecord.date,
                time = lastRecord.time,
                value = recordsToCombine
                        .sumByLong { it.value.toLong() }
                        .let {
                            if (it <= Int.MAX_VALUE) {
                                it.toInt()
                            } else {
                                1
                            }
                        },
                change = RecordChange(changeIds.last(), false)
        )

        locallyDeleteRecords(recordsToCombine
                .mapIndexed { index, recordTable ->
                    ItemsIds(recordTable.uuid, changeIds[index])
                })
        saveRecords(listOf(combinedRecord))
    }

    @Transaction
    open fun changeRecords(
            recordsIds: List<ItemsIds>,
            changedDate: SDate?,
            changedTime: Wrapper<STime>?
    ) {
        val recordsToChange = getRecordsByUuid(recordsIds.map { it.recordUuid })

        if (recordsToChange.isEmpty()) return

        val changeIds = recordsIds.associateBy { it.recordUuid }

        saveRecords(recordsToChange
                .map { recordTable ->
                    recordTable.copy(
                            date = changedDate?.date ?: recordTable.date,
                            time = if (changedTime != null) changedTime.t?.time else recordTable.time,
                            change = RecordChange(
                                    id = changeIds[recordTable.uuid]!!.recordChangeId,
                                    isDelete = recordTable.change?.isDelete == true
                            )
                    )
                })
    }
}