package com.qwert2603.syncprocessor

import com.qwert2603.syncprocessor.datasource.LocalChangesDataSource
import com.qwert2603.syncprocessor.datasource.LocalItemsDataSource
import com.qwert2603.syncprocessor.datasource.RemoteItemsDataSource
import com.qwert2603.syncprocessor.entity.Change
import com.qwert2603.syncprocessor.entity.ChangeKind
import com.qwert2603.syncprocessor.entity.Identifiable
import com.qwert2603.syncprocessor.entity.ItemsState
import com.qwert2603.syncprocessor.inmemory.InMemoryStateHolder
import com.qwert2603.syncprocessor.inmemory.ItemsStatePartialChange
import com.qwert2603.syncprocessor.schedulers.DefaultSchedulersProvider
import com.qwert2603.syncprocessor.schedulers.SchedulersProvider
import com.qwert2603.syncprocessor.sender.LocalDataSender
import com.qwert2603.syncprocessor.sender.RemoteDataSender
import io.reactivex.Completable
import io.reactivex.Observable

class SyncProcessor<I, T : Identifiable<I>>(
        private val remoteItemsDataSource: RemoteItemsDataSource<I, T>,
        private val localItemsDataSource: LocalItemsDataSource<I, T>,
        private val localChangesDataSource: LocalChangesDataSource<I>,
        schedulersProvider: SchedulersProvider = DefaultSchedulersProvider()
) {

    private val inMemoryStateHolder = InMemoryStateHolder<I, T>()
    private val localDataSender = LocalDataSender(schedulersProvider.localDataSourceScheduler)
    private val remoteDataSender = RemoteDataSender<I>(schedulersProvider.remoteDataSourceScheduler)

    fun itemsState(): Observable<ItemsState<I, T>> = inMemoryStateHolder.state

    fun addItem(item: T) {
        inMemoryStateHolder.changeState(ItemsStatePartialChange.ItemCreatedLocally(item))
        localDataSender.send(Completable.concat(listOf(
                localItemsDataSource.save(item),
                localChangesDataSource.save(Change(item.id, ChangeKind.CREATE))
        )))

        remoteDataSender.send(
                item.id,
                remoteItemsDataSource.add(item)
                        .doOnSubscribe { inMemoryStateHolder.changeState(ItemsStatePartialChange.SyncStatusChanged(item.id, ChangeKind.CREATE, true)) }
                        .doOnSuccess { remoteItem -> inMemoryStateHolder.changeState(ItemsStatePartialChange.ItemCreatedRemotely(remoteItem, item.id)) }
                        .doOnError { inMemoryStateHolder.changeState(ItemsStatePartialChange.SyncStatusChanged(item.id, ChangeKind.CREATE, false)) }
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
        if (item.id !in inMemoryStateHolder.state.value.items.map { it.id }) return
        when (inMemoryStateHolder.state.value.changes[item.id]) {
            ChangeKind.DELETE -> return
            ChangeKind.CREATE -> addItem(item)
            else -> {
                inMemoryStateHolder.changeState(ItemsStatePartialChange.ItemChanged(item))
                localDataSender.send(Completable.concat(listOf(
                        localItemsDataSource.save(item),
                        localChangesDataSource.save(Change(item.id, ChangeKind.EDIT))
                )))

                remoteDataSender.send(
                        item.id,
                        remoteItemsDataSource.edit(item)
                                .doOnSubscribe { inMemoryStateHolder.changeState(ItemsStatePartialChange.SyncStatusChanged(item.id, ChangeKind.EDIT, true)) }
                                .doOnComplete { inMemoryStateHolder.changeState(ItemsStatePartialChange.SyncStatusChanged(item.id, null, false)) }
                                .doOnError { inMemoryStateHolder.changeState(ItemsStatePartialChange.SyncStatusChanged(item.id, ChangeKind.EDIT, false)) }
                                .doOnComplete {
                                    localDataSender.send(localChangesDataSource.remove(item.id))
                                },
                        true
                )
            }
        }
    }

    fun removeItem(id: I) {
        if (id !in inMemoryStateHolder.state.value.items.map { it.id }) return
        when (inMemoryStateHolder.state.value.changes[id]) {
            ChangeKind.DELETE -> return
            ChangeKind.CREATE -> {
                inMemoryStateHolder.changeState(ItemsStatePartialChange.ItemDeletedCompletely(id))
                localDataSender.send(localChangesDataSource.remove(id))
            }
            else -> {
                inMemoryStateHolder.changeState(ItemsStatePartialChange.SyncStatusChanged(id, ChangeKind.DELETE, false))
                localDataSender.send(localChangesDataSource.save(Change(id, ChangeKind.DELETE)))

                remoteDataSender.send(
                        id,
                        remoteItemsDataSource.remove(id)
                                .doOnSubscribe { inMemoryStateHolder.changeState(ItemsStatePartialChange.SyncStatusChanged(id, ChangeKind.DELETE, true)) }
                                .doOnComplete { inMemoryStateHolder.changeState(ItemsStatePartialChange.ItemDeletedCompletely(id)) }
                                .doOnError { inMemoryStateHolder.changeState(ItemsStatePartialChange.SyncStatusChanged(id, ChangeKind.DELETE, false)) }
                                .doOnComplete {
                                    localDataSender.send(Completable.concat(listOf(
                                            localItemsDataSource.remove(id),
                                            localChangesDataSource.remove(id)
                                    )))
                                },
                        true
                )
            }
        }
    }
}