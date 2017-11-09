package com.qwert2603.syncprocessor

import com.qwert2603.syncprocessor.datasource.LastUpdateRepo
import com.qwert2603.syncprocessor.datasource.LocalChangesDataSource
import com.qwert2603.syncprocessor.datasource.LocalItemsDataSource
import com.qwert2603.syncprocessor.datasource.RemoteItemsDataSource
import com.qwert2603.syncprocessor.entity.*
import com.qwert2603.syncprocessor.inmemory.InMemoryStateHolder
import com.qwert2603.syncprocessor.inmemory.ItemsStatePartialChange
import com.qwert2603.syncprocessor.logger.DefaultLogger
import com.qwert2603.syncprocessor.logger.Logger
import com.qwert2603.syncprocessor.sender.LocalDataSender
import com.qwert2603.syncprocessor.sender.RemoteDataSender
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SyncProcessor<I : Any, T : Identifiable<I>, R>(
        private val remoteItemsDataSource: RemoteItemsDataSource<I, T, R>,
        private val localItemsDataSource: LocalItemsDataSource<I, T>,
        private val localChangesDataSource: LocalChangesDataSource<I>,
        r2t: (R) -> T,
        lastUpdateRepo: LastUpdateRepo,
        logger: Logger = DefaultLogger(),
        sortFun: (List<T>) -> List<T> = { it }
) where R : Identifiable<I>, R : RemoteItem {

    private object LoadServerUpdates

    private val inMemoryStateHolder = InMemoryStateHolder(logger, sortFun)
    private val localDataSender = LocalDataSender(logger)
    private val remoteDataSender = RemoteDataSender(logger)

    init {
        Single
                .zip(
                        localItemsDataSource.getAll(),
                        localChangesDataSource.getAll(),
                        BiFunction { items: List<T>, changes: List<Change<I>> ->
                            ItemsState(
                                    items,
                                    changes.associateBy { it.itemId }
                                            .mapValues { TimedChange(it.value.changeKind, null) },
                                    emptySet()
                            )
                        }
                )
                .subscribeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
                .subscribe { itemsState, throwable ->
                    itemsState?.let { inMemoryStateHolder.changeState(ItemsStatePartialChange.InitLoaded(it)) }
                    throwable?.let { logger.e("SyncProcessor", "error loading init state!", it) }
                }

        Observable.interval(5, TimeUnit.SECONDS)
                .doOnNext {
                    remoteDataSender.send(LoadServerUpdates,
                            remoteItemsDataSource.getAll(lastUpdateRepo.getLastUpdate())
                                    .delaySubscription(inMemoryStateHolder.state)
                                    .filter { it.isNotEmpty() }
                                    .doOnSuccess { serverUpdates ->
                                        val removedItemIds = serverUpdates.filter { it.deleted }.map { it.id }
                                        val updatedItems = serverUpdates.filter { !it.deleted }.map(r2t)

                                        inMemoryStateHolder.changeState(ItemsStatePartialChange.MakeLikeOnRemote(
                                                updatedItems = updatedItems,
                                                removedItemIds = removedItemIds
                                        ))

                                        localDataSender.send(Completable.concat(listOf(
                                                updatedItems
                                                        .filter { inMemoryStateHolder.state.value.changes[it.id]?.changeKind != ChangeKind.EDIT }
                                                        .map { localItemsDataSource.save(it) }
                                                        .let { Completable.concat(it) },
                                                removedItemIds
                                                        .map { localItemsDataSource.remove(it) }
                                                        .let { Completable.concat(it) },
                                                Completable.fromAction {
                                                    lastUpdateRepo.saveLastUpdate(serverUpdates.first().updated)
                                                }
                                        )))
                                    }
                                    .flatMapCompletable { Completable.complete() },
                            false
                    )
                }
                .subscribe()
    }

    fun itemsState(): Observable<ItemsState<I, T>> = inMemoryStateHolder.state

    fun addItem(item: T) {
        val millis = System.currentTimeMillis()
        val timedChange = TimedChange(ChangeKind.CREATE, millis)
        inMemoryStateHolder.changeState(ItemsStatePartialChange.ItemCreatedLocally(item, millis))
        localDataSender.send(Completable.concat(listOf(
                localItemsDataSource.save(item),
                localChangesDataSource.save(Change(item.id, ChangeKind.CREATE))
        )))
        remoteDataSender.send(
                item.id,
                remoteItemsDataSource.add(item)
                        .doOnSubscribe { inMemoryStateHolder.changeState(ItemsStatePartialChange.SyncStatusChanged(item.id, timedChange, true)) }
                        .doOnSuccess { remoteItem -> inMemoryStateHolder.changeState(ItemsStatePartialChange.ItemCreatedRemotely(remoteItem, item.id)) }
                        .doOnError { inMemoryStateHolder.changeState(ItemsStatePartialChange.SyncStatusChanged(item.id, timedChange, false)) }
                        .doOnSuccess { remoteItem ->
                            localDataSender.send(Completable.concat(listOf(
                                    localItemsDataSource.changeId(item.id, remoteItem),
                                    localChangesDataSource.remove(item.id)
                            )))
                        }
                        .toCompletable(),
                true
        )
    }

    fun editItem(item: T) {
        //todo: keep Set<item#id> in ItemsState.
        if (!isExistingItem(item.id)) return
        when (inMemoryStateHolder.state.value.changes[item.id]?.changeKind) {
            ChangeKind.DELETE -> return
            ChangeKind.CREATE -> addItem(item)
            else -> {
                val millis = System.currentTimeMillis()
                inMemoryStateHolder.changeState(ItemsStatePartialChange.ItemChanged(item, millis))
                localDataSender.send(Completable.concat(listOf(
                        localItemsDataSource.save(item),
                        localChangesDataSource.save(Change(item.id, ChangeKind.EDIT))
                )))

                val timedChange = TimedChange(ChangeKind.DELETE, millis)
                remoteDataSender.send(
                        item.id,
                        remoteItemsDataSource.edit(item)
                                .doOnSubscribe { inMemoryStateHolder.changeState(ItemsStatePartialChange.SyncStatusChanged(item.id, timedChange, true)) }
                                .doOnComplete { inMemoryStateHolder.changeState(ItemsStatePartialChange.SyncStatusChanged(item.id, null, false)) }
                                .doOnError { inMemoryStateHolder.changeState(ItemsStatePartialChange.SyncStatusChanged(item.id, timedChange, false)) }
                                .doOnComplete {
                                    localDataSender.send(localChangesDataSource.remove(item.id))
                                },
                        true
                )
            }
        }
    }

    fun removeItem(id: I) {
        if (!isExistingItem(id)) return
        when (inMemoryStateHolder.state.value.changes[id]?.changeKind) {
            ChangeKind.DELETE -> return
            ChangeKind.CREATE -> {
                inMemoryStateHolder.changeState(ItemsStatePartialChange.ItemDeletedCompletely(id))
                localDataSender.send(Completable.concat(listOf(
                        localChangesDataSource.remove(id),
                        localItemsDataSource.remove(id)
                )))
            }
            else -> {
                val timedChange = TimedChange(ChangeKind.DELETE, System.currentTimeMillis())
                inMemoryStateHolder.changeState(ItemsStatePartialChange.SyncStatusChanged(id, timedChange, false))
                localDataSender.send(localChangesDataSource.save(Change(id, ChangeKind.DELETE)))

                remoteDataSender.send(
                        id,
                        remoteItemsDataSource.remove(id)
                                .doOnSubscribe {
                                    if (isChangeActual(id, timedChange)) {
                                        inMemoryStateHolder.changeState(ItemsStatePartialChange.SyncStatusChanged(id, timedChange, true))
                                    } else {
                                        it.dispose()
                                    }
                                }
                                .doOnComplete {
                                    if (isChangeActual(id, timedChange)) {
                                        inMemoryStateHolder.changeState(ItemsStatePartialChange.ItemDeletedCompletely(id))
                                    }
                                }
                                .doOnError {
                                    if (isChangeActual(id, timedChange)) {
                                        inMemoryStateHolder.changeState(ItemsStatePartialChange.SyncStatusChanged(id, timedChange, false))
                                    }
                                }
                                .doOnComplete {
                                    if (isChangeActual(id, timedChange)) {
                                        localDataSender.send(Completable.concat(listOf(
                                                localItemsDataSource.remove(id),
                                                localChangesDataSource.remove(id)
                                        )))
                                    }
                                },
                        true
                )
            }
        }
    }

    private fun isExistingItem(id: I) = id in inMemoryStateHolder.state.value.items.map { it.id }

    private fun isChangeActual(id: I, timedChange: TimedChange)
            = true// inMemoryStateHolder.state.value.changes[id] == timedChange
}