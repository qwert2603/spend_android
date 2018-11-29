package com.qwert2603.spenddemo.model.local_db.dao

import android.arch.persistence.room.*
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.RecordCategory
import com.qwert2603.spenddemo.model.local_db.entity.ChangesFromServer
import com.qwert2603.spenddemo.model.local_db.entity.ItemsIds
import com.qwert2603.spenddemo.model.local_db.results.RecordItemResult
import com.qwert2603.spenddemo.model.local_db.tables.RecordCategoryTable
import com.qwert2603.spenddemo.model.local_db.tables.RecordTable
import com.qwert2603.spenddemo.model.local_db.tables.toRecordCategory
import com.qwert2603.spenddemo.utils.Const
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
                r.change_changeKindId AS changeKindId
            FROM RecordTable AS r
            JOIN RecordCategoryTable AS c ON r.recordCategoryUuid = c.uuid
            ORDER BY r.date DESC, coalesce(r.time, -1) DESC, c.recordTypeId, c.name DESC, r.kind DESC, r.uuid
        """)
    protected abstract fun getRecords(): Flowable<List<RecordItemResult>>

    @Transaction
    @Query("SELECT * FROM RecordCategoryTable")
    protected abstract fun getRecordCategories(): Flowable<List<RecordCategoryTable>>

    @Transaction
    @Query("SELECT uuid FROM RecordTable WHERE change_changeKindId IS NOT NULL AND uuid in (:uuids)")
    protected abstract fun getChangedRecordsUuids(uuids: List<String>): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun saveRecordsCategories(recordsCategories: List<RecordCategoryTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveRecords(records: List<RecordTable>)

    @Query("DELETE FROM RecordTable WHERE uuid IN (:uuids)")
    abstract fun deleteRecords(uuids: List<String>)

    @Query("UPDATE RecordTable SET change_changeKindId = NULL, change_id = NULL WHERE uuid = :recordUuid AND change_id = :changeId")
    protected abstract fun clearLocalChange(recordUuid: String, changeId: Long)

    @Transaction
    protected open fun clearLocalChanges(recordsIds: List<ItemsIds>) {
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
        saveRecordsCategories(changesFromServer.updatedCategories)
        val changedLocally = getChangedRecordsUuids(changesFromServer.updatedRecords.map { it.uuid })
                .toHashSet()
        val changesToSave = changesFromServer.updatedRecords
                .filter { it.uuid !in changedLocally }
        saveRecords(changesToSave)
        deleteRecords(changesFromServer.deletedRecordsUuid)
    }

    @Transaction
    open fun onChangesSentToServer(editedRecords: List<ItemsIds>, deletedUuids: List<String>) {
        clearLocalChanges(editedRecords)
        deleteRecords(deletedUuids)
    }
}