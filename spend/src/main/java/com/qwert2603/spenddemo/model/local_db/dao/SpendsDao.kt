package com.qwert2603.spenddemo.model.local_db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.support.annotation.VisibleForTesting
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.model.local_db.results.RecordResult
import com.qwert2603.spenddemo.model.local_db.tables.SpendKindTable
import com.qwert2603.spenddemo.model.local_db.tables.SpendTable

@Dao
abstract class SpendsDao {

    @Query("""
        SELECT * FROM (
            SELECT ${RecordResult.TYPE_SPEND} type, id, kind, value, date FROM SpendTable
        UNION ALL
            SELECT ${RecordResult.TYPE_PROFIT} type, id, kind, value, date FROM ProfitTable
        ) ORDER BY date DESC
        """)
    abstract fun getSpendsAndProfits(): LiveData<List<RecordResult>>

    @Query("SELECT * FROM SpendTable ORDER BY date DESC, id DESC")
    abstract fun getAllSpendsList(): List<SpendTable>

    @Query("SELECT SUM(s.value) FROM SpendTable s WHERE date(s.date/1000, 'unixepoch') > date('now','-30 day')")
    abstract fun get30DaysSum(): LiveData<Long?>

    @Query("SELECT * FROM SpendKindTable ORDER BY spendsCount DESC, lastDate DESC")
    abstract fun getAllKings(): LiveData<List<SpendKindTable>>

    @Query("SELECT * FROM SpendKindTable WHERE kind = :kind")
    abstract fun getKind(kind: String): SpendKindTable?

    @Transaction
    open fun addSpend(spend: SpendTable) {
        val prevKind = getKind(spend.kind)
        doAddSpend(spend)
        if (prevKind == null) {
            doUpdateKind(SpendKindTable(
                    kind = spend.kind,
                    lastDate = spend.date,
                    lastPrice = spend.value,
                    spendsCount = 1
            ))
        } else {
            doUpdateKind(SpendKindTable(
                    kind = prevKind.kind,
                    lastDate = maxOf(prevKind.lastDate, spend.date),
                    lastPrice = if (spend.date > prevKind.lastDate) spend.value else prevKind.lastPrice,
                    spendsCount = prevKind.spendsCount + 1
            ))
        }
    }

    @Transaction
    open fun addSpends(spends: List<SpendTable>) {
        spends.forEach { addSpend(it) }
    }

    @Transaction
    open fun editSpend(spend: SpendTable) {
        val prevKind = getKind(spend.kind)
        if (prevKind == null) {
            LogUtils.e("SpendsDao editSpend prevKind == null spend=$spend")
            return
        }
        doEditSpend(spend)
        val lastSpendOfKind = doGetLastSpendOfKind(spend.kind)
        if (lastSpendOfKind == null) {
            LogUtils.e("SpendsDao editSpend lastSpendOfKind == null spend=$spend")
            return
        }
        doUpdateKind(SpendKindTable(
                kind = prevKind.kind,
                lastDate = lastSpendOfKind.date,
                lastPrice = lastSpendOfKind.value,
                spendsCount = prevKind.spendsCount
        ))
    }

    @Transaction
    open fun deleteSpend(id: Long) {
        val spend = doGetSpend(id)
        if (spend == null) {
            LogUtils.e("SpendsDao editSpend spend == null id=$id")
            return
        }
        val prevKind = getKind(spend.kind)
        if (prevKind == null) {
            LogUtils.e("SpendsDao editSpend prevKind == null spend=$spend")
            return
        }
        doDeleteSpend(id)
        if (prevKind.spendsCount == 1) {
            doDeleteKind(spend.kind)
        } else {
            val lastOfKind = doGetLastSpendOfKind(prevKind.kind)
            if (lastOfKind == null) {
                LogUtils.e("SpendsDao editSpend lastOfKind == null spend=$spend prevKind=$prevKind")
                return
            }
            doUpdateKind(SpendKindTable(
                    kind = prevKind.kind,
                    lastDate = lastOfKind.date,
                    lastPrice = lastOfKind.value,
                    spendsCount = prevKind.spendsCount - 1
            ))
        }
    }

    @Transaction
    open fun deleteAllSpends() {
        doDeleteAllSpends()
        doDeleteAllKinds()
    }


    @Query("SELECT * FROM SpendTable WHERE id = :id")
    protected abstract fun doGetSpend(id: Long): SpendTable?

    @Insert
    protected abstract fun doAddSpend(spend: SpendTable): Long

    @Insert
    protected abstract fun doAddSpends(spends: List<SpendTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun doEditSpend(spend: SpendTable)

    @Query("DELETE FROM SpendTable WHERE id = :spendId")
    protected abstract fun doDeleteSpend(spendId: Long)

    @Query("DELETE FROM SpendTable")
    protected abstract fun doDeleteAllSpends()

    @Query("SELECT * FROM SpendTable WHERE kind = :kind ORDER BY date DESC LIMIT 1")
    protected abstract fun doGetLastSpendOfKind(kind: String): SpendTable?

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

    @Query("SELECT * FROM SpendKindTable ORDER BY spendsCount DESC")
    abstract fun getAllKingsList(): List<SpendKindTable>
}