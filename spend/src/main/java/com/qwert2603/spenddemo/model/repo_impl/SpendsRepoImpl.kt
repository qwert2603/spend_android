package com.qwert2603.spenddemo.model.repo_impl

import android.arch.lifecycle.LiveData
import android.content.Context
import com.qwert2603.spenddemo.di.LocalDBExecutor
import com.qwert2603.spenddemo.di.RemoteDBExecutor
import com.qwert2603.spenddemo.model.entity.CreatingSpend
import com.qwert2603.spenddemo.model.entity.FullSyncStatus
import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.spenddemo.model.entity.toSpend
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.results.RecordResult
import com.qwert2603.spenddemo.model.local_db.tables.SpendTable
import com.qwert2603.spenddemo.model.local_db.tables.toSpend
import com.qwert2603.spenddemo.model.local_db.tables.toSpendTable
import com.qwert2603.spenddemo.model.remote_db.RemoteDBFacade
import com.qwert2603.spenddemo.model.remote_db.sql_wrapper.RemoteSpend
import com.qwert2603.spenddemo.model.remote_db.sql_wrapper.toSpend
import com.qwert2603.spenddemo.model.repo.SpendsRepo
import com.qwert2603.spenddemo.model.sync_processor.LocalDataSource
import com.qwert2603.spenddemo.model.sync_processor.RemoteDataSource
import com.qwert2603.spenddemo.model.sync_processor.SyncProcessor
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpendsRepoImpl @Inject constructor(
        private val localDB: LocalDB,
        private val remoteDbFacade: RemoteDBFacade,
        appContext: Context,
        @RemoteDBExecutor remoteDBExecutor: ExecutorService,
        @LocalDBExecutor localDBExecutor: ExecutorService
) : SpendsRepo {

    private val prefs = appContext.getSharedPreferences("spends.prefs", Context.MODE_PRIVATE)

    private val localIdCounter = PrefsCounter(
            prefs = prefs,
            key = "last_spend_local_id",
            defaultValue = 1_000_000L
    )

    private val syncProcessor: SyncProcessor<Spend, RemoteSpend, SpendTable> = SyncProcessor(
            remoteDBExecutor = remoteDBExecutor,
            localDBExecutor = localDBExecutor,
            lastUpdateStorage = PrefsLastUpdateStorage(prefs, "last_update"),
            remoteDataSource = object : RemoteDataSource<Spend, RemoteSpend> {
                override fun getUpdates(lastUpdateMillis: Timestamp, lastUpdatedId: Long, count: Int): List<RemoteSpend> = remoteDbFacade.getSpends(lastUpdateMillis, lastUpdatedId, count)
                override fun addItem(t: Spend): Long = remoteDbFacade.insertSpend(t)
                override fun editItem(t: Spend) = remoteDbFacade.updateSpend(t)
                override fun deleteItem(id: Long) = remoteDbFacade.deleteSpend(id)
            },
            localDataSource = object : LocalDataSource<Spend, SpendTable> {
                override fun saveItem(t: SpendTable) = localDB.spendsDao().saveSpend(t)
                override fun addItems(ts: List<SpendTable>) = localDB.spendsDao().addSpends(ts)
                override fun deleteItems(ids: List<Long>) = localDB.spendsDao().deleteSpends(ids)
                override fun clearLocalChange(itemId: Long, changeId: Long) = localDB.spendsDao().clearLocalChange(itemId, changeId)
                override fun onItemAddedToServer(localId: Long, newId: Long, changeId: Long) = localDB.spendsDao().onSpendAddedToServer(localId, newId, changeId)
                override fun getLocallyChangedItems(count: Int): List<SpendTable> = localDB.spendsDao().getLocallyChangedSpends(count)
                override fun locallyDeleteItem(itemId: Long, changeId: Long) = localDB.spendsDao().locallyDeleteSpend(itemId, changeId)
                override fun clearAll() = localDB.spendsDao().clearAll()
                override fun saveChangesFromServer(ts: List<Spend>) = localDB.spendsDao().saveChangesFromServer(ts)
                override fun onItemEdited(t: Spend, changeId: Long) = localDB.spendsDao().onItemEdited(t, changeId)
            },
            changeIdCounter = PrefsCounter(prefs = prefs, key = "last_change_id"),
            lastFullSyncStorage = PrefsLastFullSyncStorage(prefs, "last_full_sync"),
            r2t = RemoteSpend::toSpend,
            l2t = SpendTable::toSpend,
            t2l = Spend::toSpendTable
    )

    private val locallyCreatedSpends = SingleLiveEvent<Spend>()

    init {
        syncProcessor.start()
    }

    override fun addSpend(creatingSpend: CreatingSpend) {
        val spend = creatingSpend.toSpend(localIdCounter.getNext())
        locallyCreatedSpends.value = spend
        syncProcessor.addItem(spend)
    }

    override fun addSpends(spends: List<CreatingSpend>) {
        syncProcessor.addItems(spends.map { it.toSpend(localIdCounter.getNext()) })
    }

    override fun editSpend(spend: Spend) {
        syncProcessor.editItem(spend)
    }

    override fun removeSpend(spendId: Long) {
        syncProcessor.removeItem(spendId)
    }

    override fun removeAllSpends() {
        syncProcessor.clear()
    }

    override fun getRecordsList(): LiveData<List<RecordResult>> = localDB.spendsDao().getSpendsAndProfits()

    override fun getSpend(id: Long): Observable<Wrapper<Spend>> = localDB.spendsDao()
            .getSpend(id)
            .distinctUntilChanged()
            .onBackpressureLatest()
            .toObservable()
            .map { it.firstOrNull()?.toSpend().wrap() }

    override fun locallyCreatedSpends(): SingleLiveEvent<Spend> = locallyCreatedSpends

    override fun syncingSpendIds(): LiveData<Set<Long>> = syncProcessor.syncingItemIds

    override fun syncStatus(): LiveData<FullSyncStatus> = syncProcessor.syncStatus

    override suspend fun getDumpText(): String = localDB.spendsDao()
            .getAllSpendsList()
            .let {
                if (it.isEmpty()) return@let "nth"
                val dateFormat = SimpleDateFormat(Const.DATE_FORMAT_PATTERN, Locale.getDefault())
                val timeFormat = SimpleDateFormat(Const.TIME_FORMAT_PATTERN, Locale.getDefault())
                it
                        .reversed()
                        .map {
                            listOf(
                                    it.kind,
                                    dateFormat.format(it.date),
                                    it.time?.let { timeFormat.format(it) }.toString(),
                                    it.value.toString()
                            ).reduce { s1, s2 -> "$s1,$s2" }
                        }
                        .reduce { s1, s2 -> "$s1\n$s2" }
            }

    override fun getSumLastDays(days: Int): LiveData<Long> {
        val startMillis = Calendar.getInstance()
                .also { it.time = Date().onlyDate() }
                .also { it.add(Calendar.DAY_OF_MONTH, -days + 1) }
                .timeInMillis
        return localDB.spendsDao().getSumDays(startMillis).map { it ?: 0 }
    }

    override fun getSumLastMinutes(minutes: Int): LiveData<Long> {
        val startMillis = Calendar.getInstance()
                .also { it.time = Date().secondsToZero() }
                .also { it.add(Calendar.MINUTE, -minutes + 1) }
                .timeInMillis
        return localDB.spendsDao().getSum(startMillis).map { it ?: 0 }
    }

    override fun getChangesCount(): LiveData<Int> = localDB.spendsDao().getChangesCount()
            .map { it ?: 0 }
}