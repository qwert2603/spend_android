package com.qwert2603.spenddemo.model.local_db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.qwert2603.spenddemo.model.local_db.tables.ProfitTable
import io.reactivex.Single

@Dao
interface ProfitsDao {
    @Query("SELECT * FROM ProfitTable ORDER BY date DESC, id DESC")
    fun getAllProfits(): Single<List<ProfitTable>>

    @Insert
    fun addProfit(profit: ProfitTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun editProfit(profit: ProfitTable)

    @Query("DELETE FROM ProfitTable WHERE id = :id")
    fun removeProfit(id: Long)

    @Query("DELETE FROM ProfitTable")
    fun removeAllProfits()
}