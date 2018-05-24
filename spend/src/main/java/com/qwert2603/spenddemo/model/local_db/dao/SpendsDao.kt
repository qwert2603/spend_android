package com.qwert2603.spenddemo.model.local_db.dao

import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.persistence.room.*
import com.qwert2603.spenddemo.model.local_db.results.RecordResult
import com.qwert2603.spenddemo.model.local_db.tables.SpendTable
import io.reactivex.Single

@Dao
abstract class SpendsDao {
    @Query("SELECT * FROM SpendTable ORDER BY date DESC, id DESC")
    abstract fun getAllSpends(): Single<List<SpendTable>>

    @Query("SELECT * FROM SpendTable ORDER BY date DESC, id DESC")
    abstract fun getAllSpendsList(): List<SpendTable>

    @Insert
    abstract fun addSpend(spend: SpendTable): Long

    @Query("SELECT SUM(value) FROM SpendTable")
    abstract fun getTotal(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun editSpend(spend: SpendTable)

    @Transaction
    open fun updateSpendId(oldId: Long, spend: SpendTable) {
        removeSpend(oldId)
        addSpend(spend)
    }

    @Query("DELETE FROM SpendTable WHERE id = :spendId")
    abstract fun removeSpend(spendId: Long)

    @Query("DELETE FROM SpendTable")
    abstract fun removeAllSpends()

    @Insert
    abstract fun addSpends(spends: List<SpendTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun editSpends(spends: List<SpendTable>)

    @Query("DELETE FROM SpendTable WHERE id IN (:ids)")
    abstract fun removeSpends(ids: List<Long>)

    @Query("select id from (SELECT * from SpendTable UNION SELECT * from ProfitTable) order by id")
    abstract fun getAllRecordsIds(): List<Long>

    @Query("SELECT * FROM SpendTable ORDER BY date DESC, id DESC")
    abstract fun getSpends(): DataSource.Factory<Int, SpendTable>

    @Query("SELECT * FROM (SELECT COUNT (*) FROM SpendTable UNION ALL SELECT COUNT (*) FROM ProfitTable)")
    abstract fun getCounts(): LiveData<List<Int>>

    @Query("""
        SELECT * FROM (
            SELECT ${RecordResult.TYPE_SPEND} type, id, kind, value, date FROM SpendTable
        UNION ALL
            SELECT ${RecordResult.TYPE_PROFIT} type, id, kind, value, date FROM ProfitTable
        ) ORDER BY date DESC
        """)
    abstract fun getSpendsAndProfits(): LiveData<List<RecordResult>>
}