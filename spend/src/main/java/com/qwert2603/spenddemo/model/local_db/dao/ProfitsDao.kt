package com.qwert2603.spenddemo.model.local_db.dao

import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
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

    @Insert
    fun addProfits(profits: List<ProfitTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun editProfit(profit: ProfitTable)

    @Query("DELETE FROM ProfitTable WHERE id = :id")
    fun removeProfit(id: Long)

    @Query("DELETE FROM ProfitTable")
    fun removeAllProfits()

    @Query("SELECT kind FROM ProfitTable GROUP BY kind ORDER BY count(id) DESC")
    fun getKinds(): Single<List<String>>

    @Query("SELECT * FROM ProfitTable ORDER BY date DESC, id DESC")
    fun getProfits(): DataSource.Factory<Int, ProfitTable>

    @Query("SELECT * FROM ProfitTable ORDER BY date DESC, id DESC")
    fun getProfitsLiveData(): LiveData<List<ProfitTable>>

    @Query("SELECT SUM(p.value) FROM ProfitTable p WHERE date(p.date/1000, 'unixepoch') > date('now','-30 day')")
    fun get30DaysSum(): LiveData<Long?>
}