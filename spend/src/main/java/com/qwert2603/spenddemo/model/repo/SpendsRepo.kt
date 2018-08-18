package com.qwert2603.spenddemo.model.repo

import android.arch.lifecycle.LiveData
import com.qwert2603.spenddemo.model.entity.CreatingSpend
import com.qwert2603.spenddemo.model.entity.FullSyncStatus
import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.spenddemo.model.local_db.results.RecordResult
import com.qwert2603.spenddemo.utils.SingleLiveEvent

interface SpendsRepo {
    fun addSpend(creatingSpend: CreatingSpend)

    fun addSpends(spends: List<CreatingSpend>)

    fun editSpend(spend: Spend)

    fun removeSpend(spendId: Long)

    fun removeAllSpends()

    /**
     * Spends and Profits merged.
     * Must be sorted be [RecordResult.date] DESC, [RecordResult.time] DECS NULLS LAST,
     * [RecordResult.type] DESC, [RecordResult.id] DESC
     */
    fun getRecordsList(): LiveData<List<RecordResult>>

    fun locallyCreatedSpends(): SingleLiveEvent<Spend>

    fun syncingSpendIds(): LiveData<Set<Long>>

    fun syncStatus(): LiveData<FullSyncStatus>

    suspend fun getDumpText(): String

    fun getSumLastDays(days: Int): LiveData<Long>

    fun getSumLastMinutes(minutes: Int): LiveData<Long>

    fun getChangesCount(): LiveData<Int>
}

