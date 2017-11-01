package com.qwert2603.spenddemo.model.repo_impl

import com.qwert2603.spenddemo.model.entity.*
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.tables.ChangeTable
import com.qwert2603.spenddemo.model.local_db.tables.toChange
import com.qwert2603.spenddemo.model.local_db.tables.toRecord
import com.qwert2603.spenddemo.model.local_db.tables.toRecordTable
import com.qwert2603.spenddemo.model.remote_db.RemoteDBFacade
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.model.repo.RecordsState
import com.qwert2603.spenddemo.model.schedulers.ModelSchedulersProvider
import com.qwert2603.spenddemo.utils.LogUtils
import com.qwert2603.spenddemo.utils.mapList
import com.qwert2603.spenddemo.utils.onlyDate
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordsRepoImpl @Inject constructor(
        private val localDB: LocalDB,
        private val remoteDBFacade: RemoteDBFacade,
        private val modelSchedulersProvider: ModelSchedulersProvider
) : RecordsRepo {

    private val recordsState = BehaviorSubject.createDefault<RecordsState>(RecordsState(emptyList(), emptyMap(), emptyMap()))
    private val recordsStateChanges = PublishSubject.create<RecordsStatePartialChange>()

    init {
        Single
                .zip(
                        localDB.recordsDao().getAllRecords().mapList { it.toRecord() },
                        localDB.changesDao().getAllChanges().mapList { it.toChange() },
                        BiFunction { records: List<Record>, changes: List<Change> ->
                            val localRecords = changes.map { it.recordId }
                            RecordsState(
                                    records = records.sortAsNeed(),
                                    syncStatuses = records.map {
                                        Pair(it.id, if (it.id in localRecords) SyncStatus.LOCAL else SyncStatus.REMOTE)
                                    }.toMap(),
                                    changeKinds = changes.map { Pair(it.recordId, it.changeKind) }.toMap()
                            )
                        }
                )
                .map { RecordsStatePartialChange.InitLoaded(it) }
                .subscribeOn(modelSchedulersProvider.io)
                .subscribe { t1, t2 ->
                    t1?.let { recordsStateChanges.onNext(it) }
                    t2?.let { LogUtils.e("RecordsRepoImpl init records state loading error!", it) }
                }

        recordsStateChanges
                .scan(RecordsState(emptyList(), emptyMap(), emptyMap()), { state: RecordsState, change: RecordsStatePartialChange ->
                    LogUtils.d("RecordsRepoImpl", "recordsStateChanges $change")
                    when (change) {
                        is RecordsStatePartialChange.InitLoaded -> change.recordsState
                        is RecordsStatePartialChange.RecordChanged -> state.copy(
                                records = state.records
                                        .map { if (it.id == change.record.id) change.record else it }
                                        .sortAsNeed(),
                                syncStatuses = state.syncStatuses + Pair(change.record.id, change.syncStatus),
                                changeKinds = if (change.changeKind != null) {
                                    state.changeKinds + Pair(change.record.id, change.changeKind)
                                } else {
                                    state.changeKinds - change.record.id
                                }
                        )
                        is RecordsStatePartialChange.SyncStatusChanged -> state.copy(
                                syncStatuses = state.syncStatuses + Pair(change.recordId, change.syncStatus),
                                changeKinds = if (change.changeKind != null) {
                                    state.changeKinds + Pair(change.recordId, change.changeKind)
                                } else {
                                    state.changeKinds - change.recordId
                                }
                        )
                        is RecordsStatePartialChange.RecordCreated -> state.copy(
                                records = state.records
                                        .let { if (change.localId != null) it.filter { it.id != change.localId } else it }
                                        .plus(change.record)
                                        .sortAsNeed(),
                                syncStatuses = state.syncStatuses
                                        .let { if (change.localId != null) it - change.localId else it }
                                        .plus(Pair(change.record.id, change.syncStatus)),
                                changeKinds = state.changeKinds
                                        .let { if (change.localId != null) it - change.localId else it }
                                        .let {
                                            if (change.changeKind != null) {
                                                it + Pair(change.record.id, change.changeKind)
                                            } else {
                                                it - change.record.id
                                            }
                                        }
                        )
                        is RecordsStatePartialChange.RecordDeletedCompletely -> state.copy(
                                records = state.records.filter { it.id != change.recordId },
                                syncStatuses = state.syncStatuses - change.recordId,
                                changeKinds = state.changeKinds - change.recordId
                        )
                    }
                })
                .subscribe { recordsState.onNext(it) }
    }

    override fun addRecord(creatingRecord: CreatingRecord, localId: Long?): Single<Record> = Single
            .just(localId ?: Random().nextInt(1_000_000).toLong()/*todo*/)
            .map { creatingRecord.toRecord(it) }
            .flatMap { localRecord ->
                Single
                        .fromCallable {
                            localDB.recordsDao().addRecord(localRecord.toRecordTable())
                            localDB.changesDao().saveChange(ChangeTable(ChangeKind.INSERT, localRecord.id))
                            localRecord
                        }
                        .doOnSubscribe { recordsStateChanges.onNext(RecordsStatePartialChange.RecordCreated(localRecord, localId, SyncStatus.LOCAL, ChangeKind.INSERT)) }
            }
            .flatMap { localRecord ->
                Single
                        .fromCallable {
                            val serverId = remoteDBFacade.insertRecord(creatingRecord)
                            val serverRecord = creatingRecord.toRecord(serverId)
                            localDB.changesDao().removeChange(localRecord.id)
                            localDB.recordsDao().updateRecordId(localRecord.id, serverRecord.toRecordTable())
                            serverRecord
                        }
                        .doOnSubscribe { recordsStateChanges.onNext(RecordsStatePartialChange.SyncStatusChanged(localRecord.id, SyncStatus.SYNCING, ChangeKind.INSERT)) }
                        .doOnSuccess { recordsStateChanges.onNext(RecordsStatePartialChange.RecordCreated(it, localRecord.id, SyncStatus.REMOTE, null)) }
                        .doOnError { recordsStateChanges.onNext(RecordsStatePartialChange.SyncStatusChanged(localRecord.id, SyncStatus.LOCAL, ChangeKind.INSERT)) }
                        .onErrorReturnItem(localRecord)
            }.executeAsync()

    override fun editRecord(record: Record): Completable = when (recordsState.value.changeKinds[record.id]) {
        ChangeKind.INSERT -> Completable.concat(listOf(
                Completable
                        .fromAction { localDB.recordsDao().editRecord(record.toRecordTable()) }
                        .doOnSubscribe { recordsStateChanges.onNext(RecordsStatePartialChange.RecordChanged(record, SyncStatus.LOCAL, ChangeKind.INSERT)) },
                Single
                        .fromCallable {
                            val serverId = remoteDBFacade.insertRecord(record.toCreatingRecord())
                            val serverRecord = record.copy(id = serverId)
                            localDB.changesDao().removeChange(record.id)
                            localDB.recordsDao().updateRecordId(record.id, serverRecord.toRecordTable())
                            serverRecord
                        }
                        .doOnSubscribe { recordsStateChanges.onNext(RecordsStatePartialChange.SyncStatusChanged(record.id, SyncStatus.SYNCING, ChangeKind.INSERT)) }
                        .doOnSuccess { recordsStateChanges.onNext(RecordsStatePartialChange.RecordCreated(it, record.id, SyncStatus.REMOTE, null)) }
                        .doOnError { recordsStateChanges.onNext(RecordsStatePartialChange.SyncStatusChanged(record.id, SyncStatus.LOCAL, ChangeKind.INSERT)) }
                        .toCompletable()
                        .onErrorComplete()
        ))
        ChangeKind.DELETE -> Completable.error(RuntimeException("can't edit deleted record!"))
        else -> Completable.concat(listOf(
                Completable
                        .fromAction {
                            localDB.recordsDao().editRecord(record.toRecordTable())
                            localDB.changesDao().saveChange(ChangeTable(ChangeKind.UPDATE, record.id))
                        }
                        .doOnSubscribe { recordsStateChanges.onNext(RecordsStatePartialChange.RecordChanged(record, SyncStatus.LOCAL, ChangeKind.UPDATE)) },
                Completable
                        .fromAction {
                            remoteDBFacade.updateRecord(record)
                            localDB.changesDao().removeChange(record.id)
                        }
                        .doOnSubscribe { recordsStateChanges.onNext(RecordsStatePartialChange.SyncStatusChanged(record.id, SyncStatus.SYNCING, ChangeKind.UPDATE)) }
                        .doOnComplete { recordsStateChanges.onNext(RecordsStatePartialChange.SyncStatusChanged(record.id, SyncStatus.REMOTE, null)) }
                        .doOnError { recordsStateChanges.onNext(RecordsStatePartialChange.SyncStatusChanged(record.id, SyncStatus.LOCAL, ChangeKind.UPDATE)) }
                        .onErrorComplete()
        ))
    }.executeAsync()

    override fun removeRecord(recordId: Long): Completable = when (recordsState.value.changeKinds[recordId]) {
        ChangeKind.INSERT -> Completable
                .fromAction {
                    localDB.recordsDao().removeRecord(recordId)
                    localDB.changesDao().removeChange(recordId)
                }
                .doOnComplete { recordsStateChanges.onNext(RecordsStatePartialChange.RecordDeletedCompletely(recordId)) }
        ChangeKind.DELETE -> Completable.error(RuntimeException("can't delete deleted record!"))
        else -> Completable.concat(listOf(
                Completable
                        .fromAction { localDB.changesDao().saveChange(ChangeTable(ChangeKind.DELETE, recordId)) }
                        .doOnSubscribe { recordsStateChanges.onNext(RecordsStatePartialChange.SyncStatusChanged(recordId, SyncStatus.LOCAL, ChangeKind.DELETE)) },
                Completable
                        .fromAction {
                            remoteDBFacade.deleteRecord(recordId)
                            localDB.recordsDao().removeRecord(recordId)
                            localDB.changesDao().removeChange(recordId)
                        }
                        .doOnSubscribe { recordsStateChanges.onNext(RecordsStatePartialChange.SyncStatusChanged(recordId, SyncStatus.SYNCING, ChangeKind.DELETE)) }
                        .doOnComplete { recordsStateChanges.onNext(RecordsStatePartialChange.RecordDeletedCompletely(recordId)) }
                        .doOnError { recordsStateChanges.onNext(RecordsStatePartialChange.SyncStatusChanged(recordId, SyncStatus.LOCAL, ChangeKind.DELETE)) }
                        .onErrorComplete()
        ))
    }.executeAsync()

    override fun recordsState(): Observable<RecordsState> = recordsState

    private fun List<Record>.sortAsNeed() = sortedWith(Comparator { r1, r2 ->
        r2.date.onlyDate().compareTo(r1.date.onlyDate())
                .takeIf { it != 0 }
                ?: r2.id.compareTo(r1.id)
    })

    private fun Completable.executeAsync() = this
            .subscribeOn(modelSchedulersProvider.io)
            .cache()
            .also { it.subscribe({}, { LogUtils.e("RecordsRepoImpl executeAsync error", it) }) }

    private fun <T> Single<T>.executeAsync() = this
            .subscribeOn(modelSchedulersProvider.io)
            .cache()
            .also { it.subscribe({ _, e -> e?.let { LogUtils.e("RecordsRepoImpl executeAsync error", it) } }) }

}