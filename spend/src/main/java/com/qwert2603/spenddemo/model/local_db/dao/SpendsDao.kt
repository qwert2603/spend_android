package com.qwert2603.spenddemo.model.local_db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.support.annotation.VisibleForTesting
import com.qwert2603.spenddemo.model.entity.ChangeKind
import com.qwert2603.spenddemo.model.entity.RecordChange
import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.spenddemo.model.entity.SpendChange
import com.qwert2603.spenddemo.model.local_db.results.RecordResult
import com.qwert2603.spenddemo.model.local_db.tables.SpendKindTable
import com.qwert2603.spenddemo.model.local_db.tables.SpendTable
import com.qwert2603.spenddemo.model.local_db.tables.toSpendTable
import io.reactivex.Single

@Dao
abstract class SpendsDao {

    @Transaction
    @Query("""
        SELECT * FROM (
            SELECT ${RecordResult.TYPE_PROFIT} type, id, kind, value, date, change_changeKind changeKind FROM ProfitTable
        UNION ALL
            SELECT ${RecordResult.TYPE_SPEND} type, id, kind, value, date, change_changeKind changeKind FROM SpendTable
        ) ORDER BY date DESC, type DESC, id DESC
        """)
    abstract fun getSpendsAndProfits(): LiveData<List<RecordResult>>

    @Transaction
    @Query("SELECT * FROM SpendTable ORDER BY date DESC, id DESC")
    abstract fun getAllSpendsList(): List<SpendTable>

    @Transaction
    @Query("SELECT * FROM SpendTable WHERE id = :id")
    abstract fun getSpend(id: Long): SpendTable?

    @Query("SELECT SUM(s.value) FROM SpendTable s WHERE change_changeKind != 2 AND date(s.date/1000, 'unixepoch') > date('now','-30 day')")
    abstract fun get30DaysSum(): LiveData<Long?>

    @Query("SELECT * FROM SpendKindTable ORDER BY spendsCount DESC, lastDate DESC")
    abstract fun getAllKings(): LiveData<List<SpendKindTable>>

    @Query("SELECT * FROM SpendKindTable WHERE kind = :kind")
    abstract fun getKind(kind: String): SpendKindTable?

    @Transaction
    open fun addSpends(spends: List<SpendTable>) {
        spends.forEach { saveSpend(it) }
    }

    /** add or update Spend. */
    @Transaction
    open fun saveSpend(spend: SpendTable) {
        val prevSpend = getSpend(spend.id)
        doSaveSpend(spend)
        if (prevSpend != null && spend.kind != prevSpend.kind) updateKind(prevSpend.kind)
        updateKind(spend.kind)
    }

    @Transaction
    open fun deleteSpend(id: Long) {
        val spend = getSpend(id) ?: return
        doDeleteSpend(id)
        updateKind(spend.kind)
    }

    private fun updateKind(kind: String) {
        val spendsCount = doGetSpendCountOfKind(kind)
        if (spendsCount == 0) {
            doDeleteKind(kind)
        } else {
            val lastSpendOfKind = doGetLastSpendOfKind(kind)!!
            doUpdateKind(SpendKindTable(
                    kind = kind,
                    lastDate = lastSpendOfKind.date,
                    lastPrice = lastSpendOfKind.value,
                    spendsCount = spendsCount
            ))
        }
    }

    @Transaction
    open fun clearAll() {
        doDeleteAllSpends()
        doDeleteAllKinds()
    }

    @Query("UPDATE SpendTable SET id = :newId WHERE id = :prevId")
    abstract fun changeSpendId(prevId: Long, newId: Long)

    @Query("UPDATE SpendTable SET change_changeKind = 2, change_id = :changeId WHERE id = :id")
    abstract fun locallyDeleteSpend(id: Long, changeId: Long)

    @Query("UPDATE SpendTable SET change_changeKind = 1 WHERE id = :spendId")
    abstract fun setChangeKindToEdit(spendId: Long)

    @Query("UPDATE SpendTable SET change_changeKind = NULL, change_id = NULL WHERE id = :spendId AND change_id = :changeId")
    abstract fun clearLocalChange(spendId: Long, changeId: Long)

    @Transaction
    @Query("SELECT * FROM SpendTable WHERE change_changeKind IS NOT NULL LIMIT :limit")
    abstract fun getLocallyChangedSpends(limit: Int = 10): List<SpendTable>

    @Query("SELECT id spendId, change_changeKind changeKind, change_id id FROM SpendTable WHERE change_changeKind IS NOT NULL ORDER BY change_id DESC")
    abstract fun getAllLocallyChangedSpends(): Single<List<SpendChange>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun doSaveSpend(spend: SpendTable)

    @Query("DELETE FROM SpendTable WHERE id = :spendId")
    protected abstract fun doDeleteSpend(spendId: Long)

    @Query("DELETE FROM SpendTable")
    protected abstract fun doDeleteAllSpends()

    @Query("SELECT * FROM SpendTable WHERE kind = :kind ORDER BY date DESC LIMIT 1")
    protected abstract fun doGetLastSpendOfKind(kind: String): SpendTable?

    @Query("SELECT COUNT(*) FROM SpendTable WHERE kind = :kind")
    protected abstract fun doGetSpendCountOfKind(kind: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun doUpdateKind(spendKindTable: SpendKindTable)

    @Query("DELETE FROM SpendKindTable WHERE kind = :kind")
    protected abstract fun doDeleteKind(kind: String)

    @Query("DELETE FROM SpendKindTable")
    protected abstract fun doDeleteAllKinds()


    @VisibleForTesting
    @Query("select id from (SELECT * from SpendTable UNION SELECT * from ProfitTable) order by id")
    abstract fun getAllRecordsIds(): List<Long>

    @VisibleForTesting
    @Query("SELECT SUM(value) FROM SpendTable")
    abstract fun getTotal(): Long

    @VisibleForTesting
    @Query("SELECT * FROM SpendKindTable ORDER BY spendsCount DESC, lastDate DESC")
    abstract fun getAllKingsList(): List<SpendKindTable>

    @Query("""
        UPDATE SpendTable
        SET
          id                = :newId,
          change_id         = CASE WHEN change_id = :changeId THEN NULL ELSE change_id END,
          change_changeKind = CASE WHEN change_id = :changeId THEN NULL ELSE 1 END
        WHERE id = :localId
    """)
    abstract fun onSpendAddedToServer(localId: Long, newId: Long, changeId: Long)

    @Transaction
    open fun saveChangeFromServer(spend: Spend) {
        if (getSpend(spend.id)?.change == null) {
            saveSpend(spend.toSpendTable(null))
        }
    }

    @Transaction
    open fun onItemEdited(spend: Spend, changeId: Long) {
        val changeKind = if (getSpend(spend.id)!!.change?.changeKind == ChangeKind.INSERT) {
            ChangeKind.INSERT
        } else {
            ChangeKind.UPDATE
        }
        saveSpend(spend.toSpendTable(RecordChange(changeId, changeKind)))
    }
}