package com.qwert2603.syncprocessor.inmemory

import com.qwert2603.syncprocessor.entity.Identifiable
import com.qwert2603.syncprocessor.entity.ItemsState
import com.qwert2603.syncprocessor.entity.TimedChange

internal sealed class ItemsStatePartialChange {

    data class InitLoaded<I : Any, out T : Identifiable<I>>(
            val itemsState: ItemsState<I, T>
    ) : ItemsStatePartialChange()

    data class ItemCreatedLocally<out I : Any, out T : Identifiable<I>>(
            val item: T,
            val millis: Long
    ) : ItemsStatePartialChange()

    data class ItemCreatedRemotely<out I : Any, out T : Identifiable<I>>(
            val item: T,
            val localId: I
    ) : ItemsStatePartialChange()

    data class ItemChanged<out I : Any, out T : Identifiable<I>>(
            val item: T,
            val millis: Long
    ) : ItemsStatePartialChange()

    data class ItemDeletedCompletely<out I : Any>(
            val itemId: I
    ) : ItemsStatePartialChange()

    data class SyncStatusChanged<out I : Any>(
            val itemId: I,
            val changeKind: TimedChange?,
            val syncing: Boolean
    ) : ItemsStatePartialChange()

    data class MakeLikeOnRemote<out I : Any, out T : Identifiable<I>>(
            val updatedItems: List<T>,
            val removedItemIds: List<I>
    ) : ItemsStatePartialChange()
}