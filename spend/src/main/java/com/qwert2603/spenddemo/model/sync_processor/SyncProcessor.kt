package com.qwert2603.spenddemo.model.sync_processor

import android.arch.lifecycle.MutableLiveData
import com.qwert2603.andrlib.model.IdentifiableLong
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.env.E
import com.qwert2603.spenddemo.model.entity.ChangeKind
import com.qwert2603.spenddemo.model.entity.RecordChange
import com.qwert2603.spenddemo.utils.executeAndWait
import java.sql.Timestamp
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean

class SyncProcessor<T : IdentifiableLong, R : RemoteItem, L : LocalItem>(
        private val remoteDBExecutor: ExecutorService,
        private val localDBExecutor: ExecutorService,
        private val lastUpdateStorage: LastUpdateStorage,
        private val remoteDataSource: RemoteDataSource<T, R>,
        private val localDataSource: LocalDataSource<T, L>,
        private val changeIdCounter: IdCounter,
        private val r2t: R.() -> T,
        private val l2t: L.() -> T,
        private val t2l: T.(change: RecordChange?) -> L
) {

    companion object {
        private const val TAG = "SyncProcessor"
    }

    val syncingItemIds = MutableLiveData<Set<Long>>()

    private val pendingClearAll = AtomicBoolean(false)

    fun start() {
        if (!E.env.syncWithServer) return

        remoteDBExecutor.execute {
            while (true) {
                Thread.sleep(10)
                if (pendingClearAll.compareAndSet(true, false)) {
                    localDBExecutor.executeAndWait {
                        localDataSource.clearAll()
                        lastUpdateStorage.lastUpdateInfo = LastUpdateInfo(Timestamp(0), IdentifiableLong.NO_ID)
                    }
                }

                try {
                    while (true) {
                        val (lastUpdateTimestamp, lastUpdatedId) = lastUpdateStorage.lastUpdateInfo
                        val items = remoteDataSource.getUpdates(
                                lastUpdateMillis = lastUpdateTimestamp,
                                lastUpdatedId = lastUpdatedId,
                                count = 50
                        )
                        if (items.isEmpty()) break
                        localDBExecutor.executeAndWait {
                            localDataSource.deleteItems(items
                                    .filter { it.deleted }
                                    .map { it.id })
                            localDataSource.saveChangesFromServer(items
                                    .filter { !it.deleted }
                                    .map(r2t))
                            lastUpdateStorage.lastUpdateInfo = items.last().let { LastUpdateInfo(it.updated, it.id) }
                        }
                    }

                    while (true) {
                        val locallyChangedItems = localDBExecutor.executeAndWait { localDataSource.getLocallyChangedItems(10) }
                        if (locallyChangedItems.isEmpty()) break
                        locallyChangedItems
                                .forEach { localItem ->
                                    val change = localItem.change!!
                                    syncingItemIds.postValue(setOf(localItem.id))
                                    when (change.changeKind) {
                                        ChangeKind.INSERT -> {
                                            val newId = remoteDataSource.addItem(localItem.l2t())
                                            localDBExecutor.executeAndWait {
                                                localDataSource.onItemAddedToServer(localItem.id, newId, change.id)
                                            }
                                        }
                                        ChangeKind.UPDATE -> {
                                            remoteDataSource.editItem(localItem.l2t())
                                            localDBExecutor.executeAndWait {
                                                localDataSource.clearLocalChange(localItem.id, change.id)
                                            }
                                        }
                                        ChangeKind.DELETE -> {
                                            remoteDataSource.deleteItem(localItem.id)
                                            localDBExecutor.executeAndWait {
                                                localDataSource.deleteItems(listOf(localItem.id))
                                            }
                                        }
                                    }
                                    syncingItemIds.postValue(emptySet())
                                }
                    }

                } catch (t: Throwable) {
                    LogUtils.e(TAG, "remoteDBExecutor.execute", t)
                }
            }
        }
    }

    fun addItem(t: T) {
        localDBExecutor.execute {
            localDataSource.saveItem(t.t2l(RecordChange(changeIdCounter.getNext(), ChangeKind.INSERT)))
        }
    }

    fun addItems(ts: List<T>) {
        localDBExecutor.execute {
            localDataSource.addItems(ts.map {
                it.t2l(RecordChange(changeIdCounter.getNext(), ChangeKind.INSERT))
            })
        }
    }

    fun editItem(t: T) {
        localDBExecutor.execute {
            localDataSource.onItemEdited(t, changeIdCounter.getNext())
        }
    }

    fun removeItem(itemId: Long) {
        localDBExecutor.execute {
            if (E.env.syncWithServer) {
                localDataSource.locallyDeleteItem(itemId, changeIdCounter.getNext())
            } else {
                localDataSource.deleteItems(listOf(itemId))
            }
        }
    }

    fun clear() {
        if (E.env.syncWithServer) {
            pendingClearAll.set(true)
        } else {
            localDBExecutor.execute {
                localDataSource.clearAll()
            }
        }
    }
}