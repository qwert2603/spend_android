package com.qwert2603.spenddemo.model.repo_impl

import com.qwert2603.spenddemo.model.entity.*
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.model.syncprocessor.SyncingRecord
import com.qwert2603.spenddemo.model.syncprocessor.toRecord
import com.qwert2603.spenddemo.model.syncprocessor.toSyncingRecord
import com.qwert2603.syncprocessor.ISyncProcessor
import com.qwert2603.syncprocessor.entity.ItemEvent
import io.reactivex.Observable
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordsRepoImpl @Inject constructor(
        private val syncProcessor: ISyncProcessor<Long, SyncingRecord>
) : RecordsRepo {

    override fun addRecord(creatingRecord: CreatingRecord) {
        val localId = Random().nextInt(1_000_000).toLong()/*todo*/
        val localRecord = creatingRecord.toRecord(localId)
        syncProcessor.addItem(localRecord.toSyncingRecord())
    }

    override fun editRecord(record: Record) {
        syncProcessor.editItem(record.toSyncingRecord())
    }

    override fun removeRecord(recordId: Long) {
        syncProcessor.removeItem(recordId)
    }

    override fun recordsState(): Observable<RecordsState> = syncProcessor.itemsState()
            .map { itemsState ->
                RecordsState(
                        itemsState.items.map { it.toRecord() },
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

    override fun recordCreatedEvents(): Observable<Record> = syncProcessor.itemEvents()
            .filter { it.second == ItemEvent.CREATED_LOCALLY }
            .map { it.first.toRecord() }
}