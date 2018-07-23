package com.qwert2603.spenddemo.model.repo_impl

import android.arch.lifecycle.LiveData
import android.content.Context
import android.content.SharedPreferences
import com.qwert2603.spenddemo.model.entity.CreatingProfit
import com.qwert2603.spenddemo.model.entity.Profit
import com.qwert2603.spenddemo.model.entity.toProfit
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.tables.ProfitTable
import com.qwert2603.spenddemo.model.local_db.tables.toProfit
import com.qwert2603.spenddemo.model.local_db.tables.toProfitTable
import com.qwert2603.spenddemo.model.remote_db.RemoteDBFacade
import com.qwert2603.spenddemo.model.remote_db.sql_wrapper.RemoteProfit
import com.qwert2603.spenddemo.model.remote_db.sql_wrapper.toProfit
import com.qwert2603.spenddemo.model.repo.ProfitsRepo
import com.qwert2603.spenddemo.model.sync_processor.LocalDataSource
import com.qwert2603.spenddemo.model.sync_processor.RemoteDataSource
import com.qwert2603.spenddemo.model.sync_processor.SyncProcessor
import com.qwert2603.spenddemo.utils.*
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfitsRepoImpl @Inject constructor(
        private val localDB: LocalDB,
        private val remoteDbFacade: RemoteDBFacade,
        appContext: Context
) : ProfitsRepo {

    private val prefs: SharedPreferences = appContext.getSharedPreferences("profits.prefs", Context.MODE_PRIVATE)

    private val localIdCounter = PrefsCounter(
            prefs = prefs,
            key = "last_profit_local_id",
            defaultValue = 1_000_000
    )

    private val syncProcessor: SyncProcessor<Profit, RemoteProfit, ProfitTable> = SyncProcessor(
            remoteDBExecutor = Executors.newSingleThreadExecutor(),
            localDBExecutor = Executors.newSingleThreadExecutor(),
            lastUpdateStorage = PrefsLastUpdateStorage(prefs, "last_update"),
            remoteDataSource = object : RemoteDataSource<Profit, RemoteProfit> {
                override fun getUpdates(lastUpdateMillis: Timestamp, lastUpdatedId: Long, count: Int): List<RemoteProfit> = remoteDbFacade.getProfits(lastUpdateMillis, lastUpdatedId, count)
                override fun addItem(t: Profit): Long = remoteDbFacade.insertProfit(t)
                override fun editItem(t: Profit) = remoteDbFacade.updateProfit(t)
                override fun deleteItem(id: Long) = remoteDbFacade.deleteProfit(id)
            },
            localDataSource = object : LocalDataSource<Profit, ProfitTable> {
                override fun saveItem(t: ProfitTable) = localDB.profitsDao().saveProfit(t)
                override fun addItems(ts: List<ProfitTable>) = localDB.profitsDao().addProfits(ts)
                override fun deleteItems(ids: List<Long>) = localDB.profitsDao().deleteProfits(ids)
                override fun clearLocalChange(itemId: Long, changeId: Long) = localDB.profitsDao().clearLocalChange(itemId, changeId)
                override fun getLocallyChangedItems(count: Int): List<ProfitTable> = localDB.profitsDao().getLocallyChangedProfits(count)
                override fun locallyDeleteItem(itemId: Long, changeId: Long) = localDB.profitsDao().locallyDeleteProfit(itemId, changeId)
                override fun clearAll() = localDB.profitsDao().deleteAllProfits()
                override fun onItemAddedToServer(localId: Long, newId: Long, changeId: Long) = localDB.profitsDao().onProfitAddedToServer(localId, newId, changeId)
                override fun saveChangesFromServer(ts: List<Profit>) = localDB.profitsDao().saveChangesFromServer(ts)
                override fun onItemEdited(t: Profit, changeId: Long) = localDB.profitsDao().onItemEdited(t, changeId)
            },
            changeIdCounter = PrefsCounter(prefs = prefs, key = "last_change_id"),
            r2t = RemoteProfit::toProfit,
            l2t = ProfitTable::toProfit,
            t2l = Profit::toProfitTable
    )

    private val locallyCreatedProfits = SingleLiveEvent<Profit>()

    init {
        syncProcessor.start()
    }

    override fun addProfit(creatingProfit: CreatingProfit) {
        val profit = creatingProfit.toProfit(localIdCounter.getNext())
        locallyCreatedProfits.value = profit
        syncProcessor.addItem(profit)
    }

    override fun addProfits(profits: List<CreatingProfit>) {
        syncProcessor.addItems(profits.map { it.toProfit(localIdCounter.getNext()) })
    }

    override fun editProfit(profit: Profit) {
        syncProcessor.editItem(profit)
    }

    override fun removeProfit(profitId: Long) {
        syncProcessor.removeItem(profitId)
    }

    override fun removeAllProfits() {
        syncProcessor.clear()
    }

    override fun locallyCreatedProfits(): SingleLiveEvent<Profit> = locallyCreatedProfits

    override fun syncingProfitIds(): LiveData<Set<Long>> = syncProcessor.syncingItemIds

    override suspend fun getDumpText(): String = localDB.profitsDao()
            .getAllProfitsList()
            .let {
                if (it.isEmpty()) return@let "nth"
                val dateFormat = SimpleDateFormat(Const.DATE_FORMAT_PATTERN, Locale.getDefault())
                it
                        .reversed()
                        .map {
                            listOf(
                                    it.kind,
                                    dateFormat.format(it.date),
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
        return localDB.profitsDao().getSum(startMillis).map { it ?: 0 }
    }

    override fun getSumLastMinutes(minutes: Int): LiveData<Long> {
        val startMillis = Calendar.getInstance()
                .also { it.time = Date().secondsToZero() }
                .also { it.add(Calendar.MINUTE, -minutes + 1) }
                .timeInMillis
        return localDB.profitsDao().getSum(startMillis).map { it ?: 0 }
    }

    override fun getChangesCount(): LiveData<Int> = localDB.profitsDao().getChangesCount()
            .map { it ?: 0 }
}