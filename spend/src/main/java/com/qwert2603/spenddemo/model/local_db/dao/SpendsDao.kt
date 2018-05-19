package com.qwert2603.spenddemo.model.local_db.dao

import android.arch.persistence.room.*
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

    @Insert
    abstract fun addSpends(spends: List<SpendTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun editSpends(spends: List<SpendTable>)

    @Query("DELETE FROM SpendTable WHERE id IN (:ids)")
    abstract fun removeSpends(ids: List<Long>)

    @Query("select id from (SELECT * from SpendTable UNION SELECT * from ProfitTable) order by id")
    abstract fun getAllRecordsIds(): List<Long>
}