package com.qwert2603.spend.model.sync_processor

import android.content.Context
import com.google.gson.Gson
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spend.SpendDemoApplication
import com.qwert2603.spend.di.LocalDBExecutor
import com.qwert2603.spend.di.RemoteDBExecutor
import com.qwert2603.spend.env.E
import com.qwert2603.spend.model.entity.*
import com.qwert2603.spend.model.local_db.dao.RecordsDao
import com.qwert2603.spend.model.local_db.entity.ItemsIds
import com.qwert2603.spend.model.rest.ApiHelper
import com.qwert2603.spend.utils.PrefsCounter
import com.qwert2603.spend.utils.PrefsLastChangeStorage
import com.qwert2603.spend.utils.Wrapper
import com.qwert2603.spend.utils.executeAndWait
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncProcessor @Inject constructor(
        appContext: Context,
        @RemoteDBExecutor private val remoteDBExecutor: ExecutorService,
        @LocalDBExecutor private val localDBExecutor: ExecutorService,
        private val apiHelper: ApiHelper,
        private val recordsDao: RecordsDao
) {

    companion object {
        private const val TAG = "SyncProcessor"
    }

    private val prefs = appContext.getSharedPreferences("records.prefs", Context.MODE_PRIVATE)
    private val lastChangeStorage = PrefsLastChangeStorage(prefs, Gson())
    private val changeIdCounter = PrefsCounter(prefs, "last_change_id")

    private val pendingClearAll = AtomicBoolean(false)
    private val pendingOneSync = AtomicBoolean(false)
    private val running = AtomicBoolean(false)

    val syncState: BehaviorSubject<SyncState> = BehaviorSubject.createDefault(SyncState.Syncing)

    init {
        Executors.newSingleThreadExecutor().execute {
            while (true) {

                try {
                    Thread.yield()
                    Thread.sleep(42)

                    LogUtils.d(TAG) { "while (true) ${pendingOneSync.get()} ${running.get()}" }
                    if (!pendingOneSync.getAndSet(false) && !running.get()) continue

                    if (pendingClearAll.compareAndSet(true, false)) {
                        localDBExecutor.executeAndWait {
                            recordsDao.deleteAll()
                            lastChangeStorage.lastChangeInfo = null
                        }
                    }

                    var updatedRecordsCount = 0

                    while (true) {
                        val locallyChangedItems = localDBExecutor.executeAndWait {
                            recordsDao.getLocallyChangedRecords(50)
                        }
                        if (locallyChangedItems.isEmpty()) break

                        syncState.onNext(SyncState.Syncing)
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
                        updatedRecordsCount += locallyChangedItems.size
                    }

                    while (true) {
                        val updatesFromRemote = remoteDBExecutor.executeAndWait {
                            apiHelper.getUpdates(lastChangeStorage.lastChangeInfo, 50)
                        }
                        if (updatesFromRemote.isEmpty()) break

                        syncState.onNext(SyncState.Syncing)
                        localDBExecutor.executeAndWait {
                            recordsDao.saveChangesFromServer(updatesFromRemote.toChangesFromServer())
                            lastChangeStorage.lastChangeInfo = updatesFromRemote.lastChangeInfo
                        }
                        updatedRecordsCount += updatesFromRemote.updatedRecords.size + updatesFromRemote.deletedRecordsUuid.size
                    }

                    syncState.onNext(SyncState.Synced(updatedRecordsCount))
                } catch (t: Throwable) {
                    syncState.onNext(SyncState.Error(t))
                    LogUtils.e(
                            tag = TAG,
                            msg = "remoteDBExecutor.execute",
                            t = if (t is ExecutionException) {
                                t.cause
                            } else {
                                t
                            }
                    )
                    SpendDemoApplication.debugHolder.logLine { "SyncProcessor error ${t.message}" }
                    Thread.sleep(1000)
                }
            }
        }
    }

    fun start() {
        LogUtils.d(TAG, "start")
        SpendDemoApplication.debugHolder.logLine { "SyncProcessor start" }
        if (!E.env.syncWithServer) return
        running.set(true)
    }

    fun stop() {
        LogUtils.d(TAG, "stop")
        SpendDemoApplication.debugHolder.logLine { "SyncProcessor stop" }
        running.set(false)
    }

    fun makeOneSync() {
        LogUtils.d(TAG, "makeOneSync")
        SpendDemoApplication.debugHolder.logLine { "SyncProcessor makeOneSync" }
        pendingOneSync.set(true)
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

    fun changeRecords(
            recordsUuids: List<String>,
            changedDate: SDate?,
            changedTime: Wrapper<STime>?
    ) {
        localDBExecutor.execute {
            recordsDao.changeRecords(
                    recordsIds = recordsUuids.map { ItemsIds(it, changeIdCounter.getNext()) },
                    changedDate = changedDate,
                    changedTime = changedTime
            )
        }
    }
}