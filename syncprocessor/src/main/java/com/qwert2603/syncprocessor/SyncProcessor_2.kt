package com.qwert2603.syncprocessor

import com.qwert2603.syncprocessor.entity.*
import com.qwert2603.syncprocessor.stub.StubLocalChangesDataSource
import com.qwert2603.syncprocessor.stub.StubLocalItemsDataSource
import com.qwert2603.syncprocessor.stub.StubRemoteItemsDataSource
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlin.concurrent.thread

data class Item(override val id: Long, val s: String) : Identifiable<Long>
data class ItemRemote(override val id: Long, val s: String, override val updated: Long, override val deleted: Boolean) : Identifiable<Long>, RemoteItem
data class ChangeInfo(val changeKind: ChangeKind, val isSyncing: Boolean)

data class _ItemsState(val state: List<Pair<Item, ChangeInfo?>>) {
    val changeInfos: Map<Long, ChangeInfo> = state.filter { it.second != null }.associate { Pair(it.first.id, it.second!!) }
    val syncingItems: Set<Long> = changeInfos.filter { it.value.isSyncing }.keys
}

sealed class BaseChange(changeId: Long? = null) {
    companion object {
        private val nextChangeIdCreatorLock = Any()
        private var nextChangeId = 1L
        private fun createNextChangeId(): Long {
            synchronized(nextChangeIdCreatorLock) {
                return nextChangeId++
            }
        }
    }

    val changeId: Long = changeId ?: createNextChangeId()

    open val itemId: Long? = null
    open val item: Item? = null

    val itemIdNN: Long by lazy { itemId ?: item!!.id }
}

sealed class ItemChangeDown : BaseChange() {
    class Create constructor(override val item: Item) : ItemChangeDown()
    class Edit constructor(override val item: Item) : ItemChangeDown()
    class Delete constructor(override val itemId: Long) : ItemChangeDown()

    fun getStartChange() = ItemChangeUp.ChangeStarted(itemIdNN)
    fun getFailedChange() = ItemChangeUp.ChangeFailed(itemIdNN)

    fun toUpChange(remoteItem: Item? = null) = when (this) {
        is ItemChangeDown.Create -> ItemChangeUp.CreateDone(remoteItem!!, item.id)
        is ItemChangeDown.Edit -> ItemChangeUp.EditDone(item.id)
        is ItemChangeDown.Delete -> ItemChangeUp.DeleteDone(itemId)
    }
}

sealed class ItemChangeUp : BaseChange() {
    class ChangeStarted constructor(override val itemId: Long) : ItemChangeUp()
    class ChangeFailed constructor(override val itemId: Long) : ItemChangeUp()

    class CreateDone constructor(override val item: Item, val localId: Long) : ItemChangeUp()
    class EditDone constructor(override val itemId: Long) : ItemChangeUp()
    class DeleteDone constructor(override val itemId: Long) : ItemChangeUp()
}

class ActualChangeModel {
    private var actualChanges = emptyMap<Long, BaseChange>()

    fun addChange(baseChange: BaseChange) {
        actualChanges += Pair(baseChange.itemIdNN, baseChange)
    }

    fun onChangeCompleted(itemId: Long) {
        actualChanges -= itemId
    }

    fun isChangeActual(change: BaseChange) = change.changeId == actualChanges[change.itemIdNN]?.changeId

    fun getActualChange(itemId: Long) = actualChanges[itemId]
}

class ChangesContainer<C : BaseChange> {
    private var changes = emptyList<C>()

    fun addChange(change: C) {
        changes += change
    }

    fun getNextChange(): C? {
        return changes.firstOrNull()?.also { changes = changes.drop(1) }
    }
}

class SyncProcessor_2 : ISyncProcessor<Long, Item> {
    private var itemsState = _ItemsState(emptyList())
        set(value) {
            field = value
            itemsStateChanges.onNext(value)
        }
    private val itemsStateChanges = PublishSubject.create<_ItemsState>()
    private val itemEvents = PublishSubject.create<Pair<Item, ItemEvent>>()

    private val inMemoryItemChangesDown = ChangesContainer<ItemChangeDown>()
    private val localItemChangesDown = ChangesContainer<ItemChangeDown>()
    private val remoteItemChanges = ChangesContainer<ItemChangeDown>()
    private val localItemChangesUp = ChangesContainer<ItemChangeUp>()
    private val inMemoryItemChangesUp = ChangesContainer<ItemChangeUp>()

    private val actualChanges = ActualChangeModel()

    private val localItemsDataSource = StubLocalItemsDataSource<Long, Item>()
    private val localChangesDataSource = StubLocalChangesDataSource<Long>()
    private val remoteItemsDataSource = StubRemoteItemsDataSource<Long, Item, ItemRemote>()

    override fun itemsState(): Observable<ItemsState<Long, Item>> = itemsStateChanges.map { ItemsState<Long, Item>(emptyList(), emptyMap(), emptySet()) }
    override fun itemEvents(): Observable<Pair<Item, ItemEvent>> = itemEvents

    override fun addItem(item: Item) = processChange(ItemChangeDown.Create(item))
    override fun editItem(item: Item) = processChange(ItemChangeDown.Edit(item))
    override fun removeItem(itemId: Long) = processChange(ItemChangeDown.Delete(itemId))

    private fun processChange(change: ItemChangeDown) {
        inMemoryItemChangesDown.addChange(change)
        actualChanges.addChange(change)
    }

    init {
        thread {
            while (true) {
                val change = inMemoryItemChangesDown.getNextChange() ?: continue
                if (!actualChanges.isChangeActual(change)) continue
                itemsState = when (change) {
                    is ItemChangeDown.Create -> itemsState.copy(state = itemsState.state
                            .plus(Pair(change.item, ChangeInfo(ChangeKind.CREATE, false)))
                    )
                    is ItemChangeDown.Edit -> itemsState.copy(state = itemsState.state.map {
                        if (it.first.id == change.item.id) Pair(change.item, ChangeInfo(ChangeKind.EDIT, false)) else it
                    })
                    is ItemChangeDown.Delete -> itemsState.copy(state = itemsState.state.map {
                        if (it.first.id == change.itemId) it.copy(second = ChangeInfo(ChangeKind.DELETE, false)) else it
                    })
                }
                when (change) {
                    is ItemChangeDown.Create -> itemEvents.onNext(Pair(change.item, ItemEvent.CREATED_LOCALLY))
                    is ItemChangeDown.Edit -> itemEvents.onNext(Pair(change.item, ItemEvent.EDITED_LOCALLY))
                }
                localItemChangesDown.addChange(change)
            }
        }

        thread {
            while (true) {
                val change = localItemChangesDown.getNextChange() ?: continue
                if (!actualChanges.isChangeActual(change)) continue
                val completable = when (change) {
                    is ItemChangeDown.Create -> localItemsDataSource.save(change.item)
                            .concatWith(localChangesDataSource.save(Change(change.item.id, ChangeKind.CREATE)))
                    is ItemChangeDown.Edit -> localItemsDataSource.save(change.item)
                            .concatWith(localChangesDataSource.save(Change(change.item.id, ChangeKind.EDIT)))
                    is ItemChangeDown.Delete -> localItemsDataSource.remove(change.itemId)
                            .concatWith(localChangesDataSource.save(Change(change.itemId, ChangeKind.DELETE)))
                }
                completable.blockingAwait()
                remoteItemChanges.addChange(change)
            }
        }

        thread {
            while (true) {
                val change = remoteItemChanges.getNextChange() ?: continue
                if (!actualChanges.isChangeActual(change)) continue
                localItemChangesUp.addChange(change.getStartChange())
                localItemChangesUp.addChange(try {
                    val blockingGet = when (change) {
                        is ItemChangeDown.Create -> remoteItemsDataSource.add(change.item)
                        is ItemChangeDown.Edit -> remoteItemsDataSource.edit(change.item).toSingleDefault(Unit)
                        is ItemChangeDown.Delete -> remoteItemsDataSource.remove(change.itemId).toSingleDefault(Unit)
                    }.blockingGet()
                    change.toUpChange(blockingGet as? Item)
                } catch (e: Exception) {
                    change.getFailedChange()
                })
            }
        }

        thread {
            while (true) {
                val change = localItemChangesUp.getNextChange() ?: continue
                if (!actualChanges.isChangeActual(change)) continue
                when (change) {
                    is ItemChangeUp.CreateDone -> localChangesDataSource.remove(change.item.id)
                    is ItemChangeUp.EditDone -> localChangesDataSource.remove(change.itemId)
                    is ItemChangeUp.DeleteDone -> localChangesDataSource.remove(change.itemId)
                    else -> null
                }?.blockingAwait()
                inMemoryItemChangesUp.addChange(change)
            }
        }

        // todo: update id from remote item even if change is not actual.
        thread {
            while (true) {
                val change = inMemoryItemChangesUp.getNextChange() ?: continue
                if (!actualChanges.isChangeActual(change)) continue
                itemsState = when (change) {
                    is ItemChangeUp.ChangeStarted -> itemsState.copy(state = itemsState.state.map {
                        if (it.first.id == change.itemId) it.copy(second = it.second?.copy(isSyncing = true)) else it
                    })
                    is ItemChangeUp.ChangeFailed -> itemsState.copy(state = itemsState.state.map {
                        if (it.first.id == change.itemId) it.copy(second = it.second?.copy(isSyncing = false)) else it
                    })
                    is ItemChangeUp.CreateDone -> itemsState.copy(state = itemsState.state
                            .filter { it.first.id != change.localId }
                            .plus(Pair(change.item, null))
                    )
                    is ItemChangeUp.EditDone -> itemsState.copy(state = itemsState.state.map {
                        if (it.first.id == change.itemId) it.copy(second = it.second?.copy(isSyncing = false)) else it
                    })
                    is ItemChangeUp.DeleteDone -> itemsState.copy(state = itemsState.state
                            .filter { it.first.id != change.itemId }
                    )
                }
                actualChanges.onChangeCompleted(change.itemIdNN)
            }
        }
    }
}