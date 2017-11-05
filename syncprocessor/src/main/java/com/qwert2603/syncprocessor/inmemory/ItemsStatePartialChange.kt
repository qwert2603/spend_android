package com.qwert2603.syncprocessor.inmemory

import com.qwert2603.syncprocessor.entity.ChangeKind
import com.qwert2603.syncprocessor.entity.Identifiable
import com.qwert2603.syncprocessor.entity.ItemsState

internal sealed class ItemsStatePartialChange {

    data class InitLoaded<I, out T : Identifiable<I>>(
            val itemsState: ItemsState<I, T>
    ) : ItemsStatePartialChange()

    data class ItemCreatedLocally<out I, out T : Identifiable<I>>(
            val item: T
    ) : ItemsStatePartialChange()

    data class ItemCreatedRemotely<out I, out T : Identifiable<I>>(
            val item: T,
            val localId: I
    ) : ItemsStatePartialChange()

    data class ItemChanged<out I, out T : Identifiable<I>>(
            val item: T
    ) : ItemsStatePartialChange()

    data class ItemDeletedCompletely<out I>(
            val itemId: I
    ) : ItemsStatePartialChange()

    data class SyncStatusChanged<out I>(
            val itemId: I,
            val changeKind: ChangeKind?,
            val syncing: Boolean
    ) : ItemsStatePartialChange()

    data class MakeLikeOnRemote<out I, out T : Identifiable<I>>(
            val newItems: List<T>,
            val editedItems: List<T>,
            val removedItemIds: List<I>
    ) : ItemsStatePartialChange()
}