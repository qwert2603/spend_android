package com.qwert2603.spenddemo.model.local_db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.qwert2603.spenddemo.model.local_db.tables.ChangeTable
import io.reactivex.Single

@Dao
interface ChangesDao {
    @Query("SELECT * FROM ChangeTable ORDER BY recordId")
    fun getAllChanges(): Single<List<ChangeTable>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveChange(changeTable: ChangeTable)

    @Query("DELETE FROM ChangeTable WHERE recordId = :recordId")
    fun removeChange(recordId: Long)

    @Query("DELETE FROM ChangeTable WHERE recordId IN (:recordIds)")
    fun removeChanges(recordIds: List<Long>)
}