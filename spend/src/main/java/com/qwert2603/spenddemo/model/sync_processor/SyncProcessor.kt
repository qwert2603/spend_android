package com.qwert2603.spenddemo.model.sync_processor

import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.env.E
import com.qwert2603.spenddemo.model.entity.RecordChange
import com.qwert2603.spenddemo.model.entity.RecordDraft
import com.qwert2603.spenddemo.model.entity.SyncState
import com.qwert2603.spenddemo.model.local_db.dao.RecordsDao
import com.qwert2603.spenddemo.model.local_db.entity.ItemsIds
import com.qwert2603.spenddemo.model.rest.ApiHelper
import com.qwert2603.spenddemo.utils.executeAndWait
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class SyncProcessor(
        private val remoteDBExecutor: ExecutorService,
        private val localDBExecutor: ExecutorService,
        private val lastChangeStorage: LastChangeStorage,
        private val apiHelper: ApiHelper,
        private val recordsDao: RecordsDao,
        private val changeIdCounter: IdCounter
) {

    companion object {
        private const val TAG = "SyncProcessor"
    }

    private val pendingClearAll = AtomicBoolean(false)

    val syncState = BehaviorSubject.create<SyncState>()

    fun start() {
        if (!E.env.syncWithServer) return
        Executors.newSingleThreadExecutor().execute {
            while (true) {
                try {
                    Thread.yield()
                    Thread.sleep(42)

                    if (pendingClearAll.compareAndSet(true, false)) {
                        localDBExecutor.executeAndWait {
                            recordsDao.deleteAll()
                            lastChangeStorage.lastChangeInfo = null
                        }
                    }

                    while (true) {
                        val locallyChangedItems = localDBExecutor.executeAndWait {
                            recordsDao.getLocallyChangedRecords(50)
                        }
                        if (locallyChangedItems.isEmpty()) break

                        syncState.onNext(SyncState.SYNCING)
                        val (deleted, updated) = locallyChangedItems.partition { it.change!!.isDelete }
                        val deletedUuids = deleted.map { it.uuid }
                        remoteDBExecutor.executeAndWait {
                            apiHelper.saveChanges(
                                    updated = updated.map { it.toRecordServer() },
                                    deletedUuids = deletedUuids
                            )
                        }
                        localDBExecutor.executeAndWait {
                            recordsDao.onChangesSentToServer(
                                    editedRecords = updated.map { ItemsIds(it.uuid, it.change!!.id) },
                                    deletedUuids = deletedUuids
                            )
                        }
                    }

                    while (true) {
                        val updatesFromRemote = remoteDBExecutor.executeAndWait {
                            apiHelper.getUpdates(lastChangeStorage.lastChangeInfo, 50)
                        }
                        if (updatesFromRemote.isEmpty()) break

                        syncState.onNext(SyncState.SYNCING)
                        localDBExecutor.executeAndWait {
                            recordsDao.saveChangesFromServer(updatesFromRemote.toChangesFromServer())
                            lastChangeStorage.lastChangeInfo = updatesFromRemote.lastChangeInfo
                        }
                    }

                    syncState.onNext(SyncState.SYNCED)
                } catch (t: Throwable) {
                    syncState.onNext(SyncState.ERROR)
                    LogUtils.e(TAG, "remoteDBExecutor.execute", t)
                    Thread.sleep(1000)
                }
            }
        }
    }

    fun saveItems(ts: List<RecordDraft>) {
        localDBExecutor.execute {
            recordsDao.saveRecords(ts.map { it.toRecordTable(RecordChange(changeIdCounter.getNext(), false)) })
        }
    }

    fun removeItems(itemsUuids: List<String>) {
        localDBExecutor.execute {
            if (E.env.syncWithServer) {
                recordsDao.locallyDeleteRecords(itemsUuids.map { ItemsIds(it, changeIdCounter.getNext()) })
            } else {
                recordsDao.deleteRecords(itemsUuids)
            }
        }
    }

    fun clear() {
        if (E.env.syncWithServer) {
            pendingClearAll.set(true)
        } else {
            localDBExecutor.execute {
                recordsDao.deleteAll()
            }
        }
    }

    fun combineRecords(recordUuids: List<String>, categoryUuid: String, kind: String, newRecordUuid: String) {
        localDBExecutor.execute {
            recordsDao.combineRecords(
                    recordUuids = recordUuids,
                    categoryUuid = categoryUuid,
                    kind = kind,
                    newRecordUuid = newRecordUuid,
                    changeIds = (0..recordUuids.size).map { changeIdCounter.getNext() }
            )
        }
    }
}