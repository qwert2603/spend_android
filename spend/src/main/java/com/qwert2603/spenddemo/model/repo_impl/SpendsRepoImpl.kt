package com.qwert2603.spenddemo.model.repo_impl

import android.content.Context
import com.qwert2603.spenddemo.model.entity.*
import com.qwert2603.spenddemo.model.repo.SpendsRepo
import com.qwert2603.spenddemo.model.syncprocessor.SyncingSpend
import com.qwert2603.spenddemo.model.syncprocessor.toSpend
import com.qwert2603.spenddemo.model.syncprocessor.toSyncingSpend
import com.qwert2603.spenddemo.utils.Const
import com.qwert2603.spenddemo.utils.PrefsCounter
import com.qwert2603.syncprocessor.ISyncProcessor
import com.qwert2603.syncprocessor.entity.ItemEvent
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpendsRepoImpl @Inject constructor(
        private val syncProcessor: ISyncProcessor<Long, SyncingSpend>,
        appContext: Context
) : SpendsRepo {

    private val localIdCounter = PrefsCounter(
            prefs = appContext.getSharedPreferences("spends.prefs", Context.MODE_PRIVATE),
            key = "last_spend_local_id"
    )

    override fun addSpend(creatingSpend: CreatingSpend) {
        val localId = localIdCounter.getNext()
        val localSpend = creatingSpend.toSpend(localId)
        syncProcessor.addItem(localSpend.toSyncingSpend())
    }

    override fun editSpend(spend: Spend) {
        syncProcessor.editItem(spend.toSyncingSpend())
    }

    override fun removeSpend(spendId: Long) {
        syncProcessor.removeItem(spendId)
    }

    override fun removeAllSpends() {
        // todo: OMG =\
        syncProcessor.itemsState()
                .blockingFirst()
                .items
                .forEach { removeSpend(it.id) }
    }

    override fun spendsState(): Observable<SpendsState> = syncProcessor.itemsState()
            .map { itemsState ->
                SpendsState(
                        itemsState.items.map { it.toSpend() },
                        itemsState.items.map {
                            Pair(it.id, when {
                                it.id in itemsState.syncingItems -> SyncStatus.SYNCING
                                itemsState.changes[it.id] != null -> SyncStatus.LOCAL
                                else -> SyncStatus.REMOTE
                            })
                        }.toMap(),
                        itemsState.changes.mapValues { it.value.changeKind.toChangeKind() }
                )
            }

    override fun spendCreatedEvents(): Observable<Spend> = syncProcessor.itemEvents()
            .filter { it.second == ItemEvent.CREATED_LOCALLY }
            .map { it.first.toSpend() }

    override fun getDumpText(): Single<String> = spendsState()
            .firstOrError()
            .map {
                if (it.spends.isEmpty()) return@map "nth"
                it.spends
                        .reversed()
                        .map {
                            listOf(
                                    it.kind,
                                    Const.DATE_FORMAT.format(it.date),
                                    it.value.toString()
                            ).reduce { s1, s2 -> "$s1,$s2" }
                        }
                        .reduce { s1, s2 -> "$s1\n$s2" }
            }
}