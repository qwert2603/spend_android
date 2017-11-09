package com.qwert2603.syncprocessor.inmemory

import com.qwert2603.syncprocessor.entity.ChangeKind
import com.qwert2603.syncprocessor.entity.Identifiable
import com.qwert2603.syncprocessor.entity.ItemsState
import com.qwert2603.syncprocessor.entity.TimedChange
import com.qwert2603.syncprocessor.logger.Logger
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.Executors

internal class InMemoryStateHolder<I : Any, T : Identifiable<I>>(
        logger: Logger,
        sortFun: (List<T>) -> List<T>
) {

    val state: BehaviorSubject<ItemsState<I, T>> = BehaviorSubject.create()
    private val stateChanges: PublishSubject<ItemsStatePartialChange> = PublishSubject.create()

    val itemCreatedEvents: PublishSubject<T> = PublishSubject.create()

    init {
        stateChanges
                .observeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
                .doAfterNext {
                    if (it is ItemsStatePartialChange.ItemCreatedLocally<*, *>) {
                        @Suppress("UNCHECKED_CAST")
                        itemCreatedEvents.onNext(it.item as T)
                    }
                }
                .scan(ItemsState(emptyList<T>(), emptyMap(), emptySet()), { state, change ->
                    val b = System.currentTimeMillis()
                    logger.d("InMemoryStateHolder", "change $change")
                    @Suppress("UNCHECKED_CAST")
                    when (change) {
                        is ItemsStatePartialChange.InitLoaded<*, *> -> change.itemsState as ItemsState<I, T>
                        is ItemsStatePartialChange.ItemCreatedLocally<*, *> -> {
                            change.item as T
                            state.copy(
                                    items = state.items
                                            .addWithId(change.item),
                                    changes = state.changes
                                            .plus(Pair(change.item.id, TimedChange(ChangeKind.CREATE, change.millis)))
                            )
                        }
                        is ItemsStatePartialChange.ItemCreatedRemotely<*, *> -> {
                            change.localId as I
                            change.item as T
                            state.copy(
                                    items = state.items
                                            .removeWithId(change.localId)
                                            .plus(change.item),
                                    changes = state.changes
                                            .removeKey(change.localId),
                                    syncingItems = state.syncingItems
                                            .removeKey(change.localId)
                            )
                        }
                        is ItemsStatePartialChange.ItemChanged<*, *> -> {
                            change.item as T
                            state.copy(
                                    items = state.items.addWithId(change.item),
                                    changes = state.changes.putOrRemove(change.item.id, TimedChange(ChangeKind.EDIT, change.millis)),
                                    syncingItems = state.syncingItems.minus(change.item.id)
                            )
                        }
                        is ItemsStatePartialChange.SyncStatusChanged<*> -> {
                            change.itemId as I
                            state.copy(
                                    changes = state.changes.putOrRemove(change.itemId, change.changeKind),
                                    syncingItems = state.syncingItems.addOrRemove(change.itemId, change.syncing)
                            )
                        }
                        is ItemsStatePartialChange.ItemDeletedCompletely<*> -> {
                            change.itemId as I
                            state.copy(
                                    items = state.items.removeWithId(change.itemId),
                                    changes = state.changes - change.itemId,
                                    syncingItems = state.syncingItems - change.itemId
                            )
                        }
                        is ItemsStatePartialChange.MakeLikeOnRemote<*, *> -> {
                            change.updatedItems as List<T>
                            change.removedItemIds as List<I>
                            state.copy(
                                    items = state.items
                                            .filter { it.id !in change.removedItemIds }
                                            .addWithIds(change.updatedItems),
                                    changes = state.changes - change.removedItemIds
                            )
                        }
                    }
                            .let { it.copy(items = sortFun(it.items)) }
                            .also {
                                val e = System.currentTimeMillis()
                                logger.d("InMemoryStateHolder", "state ${e - b} ms $it")
                            }
                })
                .subscribeWith(state)
    }

    fun changeState(itemsStatePartialChange: ItemsStatePartialChange) {
        stateChanges.onNext(itemsStatePartialChange)
    }
}