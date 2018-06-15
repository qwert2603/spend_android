package com.qwert2603.spenddemo.model.local_db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.qwert2603.spenddemo.model.local_db.tables.SpendChangeTable
import io.reactivex.Single

@Dao
interface SpendChangesDao {
    @Query("SELECT * FROM SpendChangeTable ORDER BY spendId")
    fun getAllChanges(): Single<List<SpendChangeTable>>

    @Query("SELECT * FROM SpendChangeTable ORDER BY spendId")
    fun getAllChangesList(): List<SpendChangeTable>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveChange(spendChangeTable: SpendChangeTable)

    @Query("DELETE FROM SpendChangeTable WHERE spendId = :spendId")
    fun removeChange(spendId: Long)

    @Query("DELETE FROM SpendChangeTable WHERE spendId IN (:spendIds)")
    fun removeChanges(spendIds: List<Long>)
}