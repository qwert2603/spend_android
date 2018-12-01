package com.qwert2603.spenddemo.model.sync_processor

import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.env.E
import com.qwert2603.spenddemo.model.entity.RecordChange
import com.qwert2603.spenddemo.model.entity.RecordDraft
import com.qwert2603.spenddemo.model.local_db.dao.RecordsDao
import com.qwert2603.spenddemo.model.local_db.entity.ItemsIds
import com.qwert2603.spenddemo.model.rest.ApiHelper
import com.qwert2603.spenddemo.utils.Const
import com.qwert2603.spenddemo.utils.executeAndWait
import io.reactivex.subjects.PublishSubject
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

    // todo: use it.
    val lastSyncMillis = PublishSubject.create<Long>()

    fun start() {
        if (!E.env.syncWithServer) return
        Executors.newSingleThreadExecutor().execute {
            while (true) {
                try {
                    Thread.yield()
                    Thread.sleep(26)

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

                        val (updated, deleted) = locallyChangedItems.partition { it.change!!.changeKindId == Const.CHANGE_KIND_UPSERT }
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
                        localDBExecutor.executeAndWait {
                            recordsDao.saveChangesFromServer(updatesFromRemote.toChangesFromServer())
                            lastChangeStorage.lastChangeInfo = updatesFromRemote.lastChangeInfo
                        }
                    }

                    lastSyncMillis.onNext(System.currentTimeMillis())
                } catch (t: Throwable) {
                    LogUtils.e(TAG, "remoteDBExecutor.execute", t)
                    Thread.sleep(1000)
                }
            }
        }
    }

    fun saveItems(ts: List<RecordDraft>) {
        localDBExecutor.execute {
            recordsDao.saveRecords(ts.map { it.toRecordTable(RecordChange(changeIdCounter.getNext(), Const.CHANGE_KIND_UPSERT)) })
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
}