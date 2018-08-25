package com.qwert2603.spenddemo.model.local_db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.qwert2603.spenddemo.model.entity.ChangeKind
import com.qwert2603.spenddemo.model.entity.Profit
import com.qwert2603.spenddemo.model.entity.RecordChange
import com.qwert2603.spenddemo.model.local_db.tables.ProfitTable
import com.qwert2603.spenddemo.model.local_db.tables.toProfitTable
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.*

@Dao
abstract class ProfitsDao {

    @Query("SELECT * FROM ProfitTable WHERE id=:id")
    abstract fun getProfit(id: Long): Flowable<List<ProfitTable>>

    @Transaction
    @Query("SELECT * FROM ProfitTable ORDER BY date DESC, coalesce(time, ${Long.MIN_VALUE}) DESC, id DESC")
    abstract fun getAllProfitsList(): List<ProfitTable>

    @Insert
    abstract fun addProfits(profits: List<ProfitTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveProfit(profit: ProfitTable)

    @Query("DELETE FROM ProfitTable WHERE id IN (:ids)")
    abstract fun deleteProfits(ids: List<Long>)

    @Query("DELETE FROM ProfitTable")
    abstract fun deleteAllProfits()

    @Query("UPDATE ProfitTable SET change_changeKind = 2, change_id = :changeId WHERE id = :id")
    abstract fun locallyDeleteProfit(id: Long, changeId: Long)

    @Query("UPDATE ProfitTable SET change_changeKind = NULL, change_id = NULL WHERE id = :profitId AND change_id = :changeId")
    abstract fun clearLocalChange(profitId: Long, changeId: Long)

    @Transaction
    @Query("SELECT * FROM ProfitTable WHERE change_changeKind IS NOT NULL LIMIT :limit")
    abstract fun getLocallyChangedProfits(limit: Int): List<ProfitTable>

    @Query("SELECT kind FROM ProfitTable GROUP BY kind ORDER BY count(id) DESC")
    abstract fun getKinds(): Single<List<String>>

    @Query("""
        SELECT SUM(p.value)
        FROM ProfitTable p
        WHERE (change_changeKind IS NULL OR change_changeKind != 2) AND p.time IS NOT NULL
            AND (p.date + p.time) >= (:startMillis - :offset)
    """)
    abstract fun getSum(
            startMillis: Long,
            offset: Int = TimeZone.getDefault().getOffset(System.currentTimeMillis())
    ): LiveData<Long?>

    @Query("""
        SELECT SUM(p.value)
        FROM ProfitTable p
        WHERE (change_changeKind IS NULL OR change_changeKind != 2) AND p.date >= :startMillis
    """)
    abstract fun getSumDays(startMillis: Long): LiveData<Long?>

    @Query("SELECT COUNT(*) FROM ProfitTable WHERE change_id IS NOT NULL")
    abstract fun getChangesCount(): LiveData<Int?>

    @Query("""
        UPDATE ProfitTable
        SET
          id                = :newId,
          change_id         = CASE WHEN change_id = :changeId THEN NULL ELSE change_id END,
          change_changeKind = CASE WHEN change_id = :changeId THEN NULL ELSE 1 END
        WHERE id = :localId
    """)
    abstract fun onProfitAddedToServer(localId: Long, newId: Long, changeId: Long)

    @Transaction
    open fun saveChangesFromServer(profits: List<Profit>) {
        profits.forEach {
            if (doGetProfit(it.id)?.change == null) {
                saveProfit(it.toProfitTable(null))
            }
        }
    }

    @Transaction
    open fun onItemEdited(profit: Profit, changeId: Long) {
        val changeKind = if (doGetProfit(profit.id)!!.change?.changeKind == ChangeKind.INSERT) {
            ChangeKind.INSERT
        } else {
            ChangeKind.UPDATE
        }
        saveProfit(profit.toProfitTable(RecordChange(changeId, changeKind)))
    }

    @Query("SELECT * FROM ProfitTable WHERE id = :id")
    protected abstract fun doGetProfit(id: Long): ProfitTable?
}