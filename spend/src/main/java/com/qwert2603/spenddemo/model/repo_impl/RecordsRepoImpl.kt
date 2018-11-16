package com.qwert2603.spenddemo.model.repo_impl

import android.content.Context
import com.google.gson.Gson
import com.qwert2603.andrlib.util.mapList
import com.qwert2603.spenddemo.di.LocalDBExecutor
import com.qwert2603.spenddemo.di.RemoteDBExecutor
import com.qwert2603.spenddemo.model.entity.LastUpdateInfo
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.RecordDraft
import com.qwert2603.spenddemo.model.entity.toRecordServer
import com.qwert2603.spenddemo.model.local_db.dao.RecordsDao
import com.qwert2603.spenddemo.model.local_db.tables.RecordTable
import com.qwert2603.spenddemo.model.local_db.tables.toRecord
import com.qwert2603.spenddemo.model.local_db.tables.toRecordServer
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.model.rest.Rest
import com.qwert2603.spenddemo.model.rest.entity.RecordServer
import com.qwert2603.spenddemo.model.rest.entity.SaveRecordsParam
import com.qwert2603.spenddemo.model.rest.toRecordTable
import com.qwert2603.spenddemo.model.rest.toUpdatesFromRemote
import com.qwert2603.spenddemo.model.sync_processor.*
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.ExecutorService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordsRepoImpl @Inject constructor(
        private val recordsDao: RecordsDao,
        rest: Rest,
        appContext: Context,
        @RemoteDBExecutor remoteDBExecutor: ExecutorService,
        @LocalDBExecutor localDBExecutor: ExecutorService
) : RecordsRepo {

    private val prefs = appContext.getSharedPreferences("records.prefs", Context.MODE_PRIVATE)

    private val syncProcessor = SyncProcessor(
            remoteDBExecutor = remoteDBExecutor,
            localDBExecutor = localDBExecutor,
            lastUpdateStorage = PrefsLastUpdateStorage(prefs, Gson()),
            remoteDataSource = object : RemoteDataSource<RecordServer> {
                override fun getUpdates(lastUpdateInfo: LastUpdateInfo?, count: Int): UpdatesFromRemote<RecordServer> = rest
                        .getRecordsUpdates(lastUpdateInfo?.lastUpdated, lastUpdateInfo?.lastUuid, count)
                        .toUpdatesFromRemote()

                override fun saveChanges(updated: List<RecordServer>, deletedUuids: List<String>) = rest
                        .saveRecords(SaveRecordsParam(updated, deletedUuids))
            },
            localDataSource = object : LocalDataSource<RecordTable, RecordServer> {
                override fun saveItems(ts: List<RecordTable>) = recordsDao.saveRecords(ts)
                override fun locallyDeleteItems(itemsIds: List<ItemsIds>) = recordsDao.locallyDeleteRecords(itemsIds)
                override fun getLocallyChangedItems(count: Int): List<RecordTable> = recordsDao.getLocallyChangedRecords(count)
                override fun clearLocalChange(itemsIds: List<ItemsIds>) = recordsDao.clearLocalChanges(itemsIds)
                override fun saveChangesFromRemote(updatesFromRemote: UpdatesFromRemote<RecordServer>) = recordsDao.saveChangesFromServer(
                        updatesFromRemote.updatedItems,
                        updatesFromRemote.deletedItemsUuid
                )

                override fun deleteItems(uuids: List<String>) = recordsDao.deleteRecords(uuids)
                override fun deleteAll() = recordsDao.deleteAllRecords()
            },
            changeIdCounter = PrefsCounter(prefs, "last_change_id"),
            l2t = { this.toRecordServer() },
            t2l = { this.toRecordTable(it) }
    )

    private val recordCreatedLocallyEvents = PublishSubject.create<String>()

    init {
//     todo:   syncProcessor.start()
    }

    override fun getRecordsList(): Observable<List<Record>> = recordsDao.recordsList

    override fun getRecord(uuid: String): Observable<Wrapper<Record>> = recordsDao
            .getRecord(uuid)
            .distinctUntilChanged()
            .toObservable()
            .map { it.firstOrNull()?.toRecord().wrap() }

    override fun getSumLastDays(recordTypeId: Long, days: Int): Observable<Long> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -days + 1)
        return recordsDao
                .getSumDays(recordTypeId, calendar.toDateInt())
                .toObservable()
                .startWith(0)
    }

    override fun getSumLastMinutes(recordTypeId: Long, minutes: Int): Observable<Long> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, -minutes + 1)
        return recordsDao
                .getSum(recordTypeId, calendar.toDateInt(), calendar.toTimeInt())
                .toObservable()
                .startWith(0)
    }

    override fun getDumpText(): Single<String> = recordsDao.recordsList
            .firstOrError()
            .mapList { "${it.uuid},${it.recordTypeId},${it.date.toDateString()},${it.time?.toTimeString()},${it.kind},${it.value}" }
            .map { it.reduce { acc, s -> "$acc\n$s" } }

    override fun getSyncingRecordsUuids(): Observable<Set<String>> = syncProcessor.syncingRecordsUuids

    override fun getRecordCreatedLocallyEvents(): Observable<String> = recordCreatedLocallyEvents

    override fun getLocalChangesCount(recordTypeIds: List<Long>): Observable<Int> = recordsDao
            .getChangesCount(recordTypeIds)
            .toObservable()

    override fun saveRecords(records: List<RecordDraft>) {
        records.forEach { recordCreatedLocallyEvents.onNext(it.uuid) }
        syncProcessor.saveItems(records.map { it.toRecordServer() })
    }

    override fun removeRecords(recordsUuids: List<String>) {
        syncProcessor.removeItems(recordsUuids)
    }

    override fun removeAllRecords() {
        syncProcessor.clear()
    }
}