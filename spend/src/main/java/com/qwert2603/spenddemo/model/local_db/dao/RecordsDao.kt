package com.qwert2603.spenddemo.model.local_db.dao

import android.arch.persistence.room.*
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.local_db.tables.RecordTable
import com.qwert2603.spenddemo.model.local_db.tables.toRecord
import com.qwert2603.spenddemo.model.rest.entity.RecordServer
import com.qwert2603.spenddemo.model.rest.toRecordTable
import com.qwert2603.spenddemo.model.sync_processor.ItemsIds
import com.qwert2603.spenddemo.utils.Const
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

@Dao
abstract class RecordsDao {

    val recordsList: Observable<List<Record>> by lazy {
        val behaviorSubject = BehaviorSubject.create<List<Record>>()

        getRecords()
                .subscribe(
                        { behaviorSubject.onNext(it.map { it.toRecord() }) },
                        { LogUtils.e("RecordsDao getRecords error!", it) }
                ).also { }

        behaviorSubject.hide()
    }

    @Transaction
    @Query("""
            SELECT * FROM RecordTable
            ORDER BY date DESC, coalesce(time, ${Long.MIN_VALUE}) DESC, recordTypeId, kind DESC, uuid
        """)
    protected abstract fun getRecords(): Flowable<List<RecordTable>>

    @Query("SELECT * FROM RecordTable WHERE uuid = :uuid")
    abstract fun getRecord(uuid: String): Flowable<List<RecordTable>>

    @Query("""
        SELECT SUM(value)
        FROM RecordTable
        WHERE recordTypeId = :recordTypeId AND (change_changeKindId IS NULL OR change_changeKindId != ${Const.CHANGE_KIND_DELETE}) AND time IS NOT NULL
            AND ((date > :startDate) OR (date = :startDate AND time >= :startTime))
    """)
    abstract fun getSum(recordTypeId: Long, startDate: Int, startTime: Int): Flowable<Long>

    @Query("""
        SELECT SUM(value)
        FROM RecordTable
        WHERE recordTypeId = :recordTypeId AND (change_changeKindId IS NULL OR change_changeKindId != ${Const.CHANGE_KIND_DELETE}) AND date >= :startDate
    """)
    abstract fun getSumDays(recordTypeId: Long, startDate: Int): Flowable<Long>

    @Query("SELECT COUNT(*) FROM RecordTable WHERE recordTypeId in (:recordTypeIds) AND change_id IS NOT NULL")
    abstract fun getChangesCount(recordTypeIds: List<Long>): Flowable<Int>

    @Query("SELECT uuid FROM RecordTable WHERE change_changeKindId IS NOT NULL AND uuid in (:uuids)")
    abstract fun getChangedRecordsUuids(uuids: List<String>): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveRecords(records: List<RecordTable>)

    @Query("DELETE FROM RecordTable WHERE uuid IN (:uuids)")
    abstract fun deleteRecords(uuids: List<String>)

    @Query("UPDATE RecordTable SET change_changeKindId = NULL, change_id = NULL WHERE uuid = :recordUuid AND change_id = :changeId")
    protected abstract fun clearLocalChange(recordUuid: String, changeId: Long)

    @Transaction
    open fun clearLocalChanges(recordsIds: List<ItemsIds>) {
        recordsIds.forEach { clearLocalChange(it.recordUuid, it.recordChangeId) }
    }

    @Transaction
    @Query("SELECT * FROM RecordTable WHERE change_changeKindId IS NOT NULL LIMIT :limit")
    abstract fun getLocallyChangedRecords(limit: Int): List<RecordTable>

    @Query("UPDATE RecordTable SET change_changeKindId = ${Const.CHANGE_KIND_DELETE}, change_id = :changeId WHERE uuid = :uuid")
    protected abstract fun locallyDeleteRecord(uuid: String, changeId: Long)

    @Transaction
    open fun locallyDeleteRecords(recordsIds: List<ItemsIds>) {
        recordsIds.forEach { locallyDeleteRecord(it.recordUuid, it.recordChangeId) }
    }

    @Query("DELETE FROM RecordTable")
    abstract fun deleteAllRecords()

    @Transaction
    open fun saveChangesFromServer(updatedRecords: List<RecordServer>, deletedRecordsUuid: List<String>) {
        val changedLocally = getChangedRecordsUuids(updatedRecords.map { it.uuid })
                .toHashSet()
        val changesToSave = updatedRecords
                .filter { it.uuid !in changedLocally }
                .map { it.toRecordTable(null) }
        saveRecords(changesToSave)
        deleteRecords(deletedRecordsUuid)
    }
}