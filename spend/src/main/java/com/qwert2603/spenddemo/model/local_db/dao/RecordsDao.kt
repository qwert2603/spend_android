package com.qwert2603.spenddemo.model.local_db.dao

import android.arch.persistence.room.*
import com.qwert2603.spenddemo.model.local_db.tables.RecordTable
import io.reactivex.Single

@Dao
abstract class RecordsDao {
    @Query("SELECT * FROM RecordTable ORDER BY date DESC, id DESC")
    abstract fun getAllRecords(): Single<List<RecordTable>>

    @Insert
    abstract fun addRecord(record: RecordTable): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun editRecord(record: RecordTable)

    @Transaction
    open fun updateRecordId(oldId: Long, record: RecordTable) {
        removeRecord(oldId)
        addRecord(record)
    }

    @Query("DELETE FROM RecordTable WHERE id = :recordId")
    abstract fun removeRecord(recordId: Long)

    @Insert
    abstract fun addRecords(records: List<RecordTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun editRecords(records: List<RecordTable>)

    @Query("DELETE FROM RecordTable WHERE id IN (:ids)")
    abstract fun removeRecords(ids: List<Long>)
}