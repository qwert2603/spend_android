package com.qwert2603.spenddemo.model.repo_impl

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.model.entity.*
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.results.RecordResult
import com.qwert2603.spenddemo.model.local_db.tables.toCreatingSpend
import com.qwert2603.spenddemo.model.local_db.tables.toSpendTable
import com.qwert2603.spenddemo.model.remote_db.RemoteDBFacade
import com.qwert2603.spenddemo.model.remote_db.sql_wrapper.toSpendTable
import com.qwert2603.spenddemo.model.repo.SpendsRepo
import com.qwert2603.spenddemo.utils.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpendsRepoImpl @Inject constructor(
        private val localDB: LocalDB,
        private val remoteDbFacade: RemoteDBFacade,
        appContext: Context
) : SpendsRepo {

    companion object {
        private const val TAG = "SpendsRepoImpl"
        private const val LOCAL_ID_START = 1_000_000L
    }

    private val prefs = appContext.getSharedPreferences("spends.prefs", Context.MODE_PRIVATE)

    private val localIdCounter = PrefsCounter(
            prefs = prefs,
            key = "last_spend_local_id",
            defaultValue = LOCAL_ID_START
    )

    private val changeIdCounter = PrefsCounter(
            prefs = prefs,
            key = "last_change_id"
    )

    private var lastUpdateTimestamp by PrefsTimestamp(prefs, "lastUpdateTimestamp")
    private var lastUpdatedId by PrefsLong(prefs, "lastUpdatedId")

    private val locallyCreatedSpends = SingleLiveEvent<Spend>()

    private val locallyEditedSpends = SingleLiveEvent<Spend>()

    private val localDBContext = newSingleThreadContext("localDB")
    private val remoteDBContext = newSingleThreadContext("remoteDB")

    private val syncingSpendIds = MutableLiveData<Set<Long>>()

    init {
        launch(remoteDBContext) {
            while (true) {
                try {
                    while (true) {
                        val spends = remoteDbFacade.getSpends(lastUpdateMillis = lastUpdateTimestamp, lastUpdatedId = lastUpdatedId)
                        if (spends.isEmpty()) break
                        async(localDBContext) {
                            spends.forEach {
                                if (it.deleted) {
                                    localDB.spendsDao().deleteSpend(it.id)
                                } else {
                                    if (localDB.spendsDao().getSpend(it.id)?.change == null) {
                                        localDB.spendsDao().saveSpend(it.toSpendTable())
                                    }
                                }
                            }
                        }.await()
                        lastUpdateTimestamp = spends.last().updated
                        lastUpdatedId = spends.last().id
                    }

                    localDB.spendsDao().getLocallyChangedSpends()
                            .forEach { spend ->
                                spend.change!!
                                syncingSpendIds.postValue(setOf(spend.id))
                                when (spend.change.changeKind) {
                                    ChangeKind.INSERT -> {
                                        val remoteId = remoteDbFacade.insertSpend(spend.toCreatingSpend())
                                        async(localDBContext) {
                                            localDB.spendsDao().changeSpendId(spend.id, remoteId)
                                            val currentChange = localDB.spendsDao().getSpend(remoteId)!!.change
                                            if (currentChange!!.id == spend.change.id) {
                                                localDB.spendsDao().clearLocalChange(remoteId)
                                            } else {
                                                localDB.spendsDao().setChangeKindToEdit(remoteId)
                                            }
                                        }.await()
                                    }
                                    ChangeKind.UPDATE -> {
                                        remoteDbFacade.updateSpend(Spend(spend.id, spend.kind, spend.value, spend.date))
                                        async(localDBContext) {
                                            val currentChangeId = localDB.spendsDao().getSpend(spend.id)!!.change!!.id
                                            if (currentChangeId == spend.change.id) {
                                                localDB.spendsDao().clearLocalChange(spend.id)
                                            }
                                        }.await()
                                    }
                                    ChangeKind.DELETE -> {
                                        remoteDbFacade.deleteSpend(spend.id)
                                        async(localDBContext) {
                                            localDB.spendsDao().deleteSpend(spend.id)
                                        }.await()
                                    }
                                }
                                syncingSpendIds.postValue(emptySet())
                            }

                } catch (t: Throwable) {
                    LogUtils.e(TAG, "launch(remoteDBContext)", t)
                }
            }
        }
    }

    override fun addSpend(creatingSpend: CreatingSpend) {
        launch(localDBContext) {
            val localId = localIdCounter.getNext()
            val localSpend = creatingSpend.toSpend(localId)
            locallyCreatedSpends.postValue(localSpend)
            localDB.spendsDao().saveSpend(localSpend.toSpendTable(RecordChange(changeIdCounter.getNext(), ChangeKind.INSERT)))
        }
    }

    override fun addSpends(spends: List<CreatingSpend>) {
        launch(localDBContext) {
            val spendTables = spends.map {
                it
                        .toSpend(localIdCounter.getNext())
                        .toSpendTable(RecordChange(changeIdCounter.getNext(), ChangeKind.INSERT))
            }
            localDB.spendsDao().addSpends(spendTables)
        }
    }

    override fun editSpend(spend: Spend) {
        launch(localDBContext) {
            locallyEditedSpends.postValue(spend)
            val changeKind = if (localDB.spendsDao().getSpend(spend.id)!!.change?.changeKind == ChangeKind.INSERT) {
                ChangeKind.INSERT
            } else {
                ChangeKind.UPDATE
            }
            localDB.spendsDao().saveSpend(spend.toSpendTable(RecordChange(changeIdCounter.getNext(), changeKind)))
        }
    }

    override fun removeSpend(spendId: Long) {
        launch(localDBContext) {
            localDB.spendsDao().locallyDeleteSpend(spendId, changeIdCounter.getNext())
        }
    }

    override fun removeAllSpends() {
        launch(localDBContext) {
            localDB.spendsDao().clearAll()
            lastUpdateTimestamp = Timestamp(0)
            lastUpdatedId = 0
        }
    }

    override fun getRecordsList(): LiveData<List<RecordResult>> = localDB.spendsDao().getSpendsAndProfits()

    override fun locallyCreatedSpends(): SingleLiveEvent<Spend> = locallyCreatedSpends

    override fun locallyEditedSpends(): SingleLiveEvent<Spend> = locallyEditedSpends

    override fun syncingSpendIds(): LiveData<Set<Long>> = syncingSpendIds

    override suspend fun getDumpText(): String = localDB.spendsDao()
            .getAllSpendsList()
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

    override fun get30DaysBalance(): LiveData<Long> = combineLatest(
            localDB.profitsDao().get30DaysSum(),
            localDB.spendsDao().get30DaysSum(),
            { profits, spends -> (profits ?: 0L) - (spends ?: 0L) }
    ).map { it ?: 0 }
}