package com.qwert2603.syncprocessor.inmemory

import com.qwert2603.syncprocessor.entity.ChangeKind
import com.qwert2603.syncprocessor.entity.Identifiable
import com.qwert2603.syncprocessor.entity.ItemsState
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

internal class InMemoryStateHolder<I, T : Identifiable<I>> {

    val state: BehaviorSubject<ItemsState<I, T>> = BehaviorSubject.create()
    private val stateChanges: PublishSubject<ItemsStatePartialChange> = PublishSubject.create()

    init {
        stateChanges
                .scan(ItemsState(emptyList<T>(), emptyMap(), emptySet()), { state, change ->
                    @Suppress("UNCHECKED_CAST")
                    when (change) {
                        is ItemsStatePartialChange.InitLoaded<*, *> -> change.itemsState as ItemsState<I, T>
                        is ItemsStatePartialChange.ItemCreatedLocally<*, *> -> {
                            change.item as T
                            state.copy(
                                    items = state.items
                                            .addWithId(change.item),
                                    changes = state.changes
                                            .plus(Pair(change.item.id, ChangeKind.CREATE))
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
                                    changes = state.changes.putOrRemove(change.item.id, ChangeKind.EDIT),
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
                            change.newItems as List<T>
                            change.editedItems as List<T>
                            change.removedItemIds as List<I>
                            val editedItems = change.editedItems.associateBy { it.id }
                            state.copy(
                                    items = state.items
                                            .filter { it.id !in change.removedItemIds }
                                            .map { editedItems[it.id] ?: it }
                                            .plus(change.newItems),
                                    changes = state.changes - change.removedItemIds
                            )
                        }
                    }
                })
                .subscribeWith(state)
    }

    fun changeState(itemsStatePartialChange: ItemsStatePartialChange) {
        stateChanges.onNext(itemsStatePartialChange)
    }
}