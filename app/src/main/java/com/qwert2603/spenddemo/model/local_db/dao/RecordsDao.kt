package com.qwert2603.spenddemo.model.local_db.dao

import android.arch.persistence.room.*
import com.qwert2603.spenddemo.model.local_db.tables.RecordTable
import io.reactivex.Single

@Dao
abstract class RecordsDao {
    @Query("SELECT * FROM RecordTable ORDER BY date DESC, id DESC")
    abstract fun getAllRecords(): Single<List<RecordTable>>

    @Transaction
    open fun rewriteAll(records: List<RecordTable>) {
        removeAll()
        addAll(records)
    }

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
    abstract fun addAll(records: List<RecordTable>)

    @Query("DELETE FROM RecordTable")
    abstract fun removeAll()
}