package com.qwert2603.spenddemo.model.repo_impl

import android.content.Context
import com.google.gson.Gson
import com.qwert2603.andrlib.schedulers.ModelSchedulersProvider
import com.qwert2603.andrlib.util.mapList
import com.qwert2603.spenddemo.di.LocalDBExecutor
import com.qwert2603.spenddemo.di.RemoteDBExecutor
import com.qwert2603.spenddemo.model.entity.LastUpdateInfo
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.RecordDraft
import com.qwert2603.spenddemo.model.entity.toRecordServer
import com.qwert2603.spenddemo.model.local_db.dao.RecordsDao
import com.qwert2603.spenddemo.model.local_db.tables.RecordTable
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
        @LocalDBExecutor localDBExecutor: ExecutorService,
        private val modelSchedulersProvider: ModelSchedulersProvider
) : RecordsRepo {

    private val prefs = appContext.getSharedPreferences("records.prefs", Context.MODE_PRIVATE)

    private val syncProcessor = SyncProcessor(
            remoteDBExecutor = remoteDBExecutor,
            localDBExecutor = localDBExecutor,
            lastUpdateStorage = PrefsLastUpdateStorage(prefs, Gson()),
            remoteDataSource = object : RemoteDataSource<RecordServer> {
                override fun getUpdates(lastUpdateInfo: LastUpdateInfo?, count: Int): UpdatesFromRemote<RecordServer> = rest
                        .getRecordsUpdates(lastUpdateInfo?.lastUpdated, lastUpdateInfo?.lastUuid, count)
                        .execute()
                        .body()!!
                        .toUpdatesFromRemote()

                override fun saveChanges(updated: List<RecordServer>, deletedUuids: List<String>) = rest
                        .saveRecords(SaveRecordsParam(updated, deletedUuids))
                        .execute()
                        .let { Unit }
            },
            localDataSource = object : LocalDataSource<RecordTable, RecordServer> {
                override fun saveItems(ts: List<RecordTable>) = recordsDao.saveRecords(ts)
                override fun locallyDeleteItems(itemsIds: List<ItemsIds>) = recordsDao.locallyDeleteRecords(itemsIds)
                override fun getLocallyChangedItems(count: Int): List<RecordTable> = recordsDao.getLocallyChangedRecords(count)
                override fun saveChangesFromRemote(updatesFromRemote: UpdatesFromRemote<RecordServer>) = recordsDao.saveChangesFromServer(
                        updatedRecords = updatesFromRemote.updatedItems,
                        deletedRecordsUuid = updatesFromRemote.deletedItemsUuid
                )

                override fun onChangesSentToServer(editedRecords: List<ItemsIds>, deletedUuids: List<String>) = recordsDao.onChangesSentToServer(
                        editedRecords = editedRecords,
                        deletedUuids = deletedUuids
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
        syncProcessor.start()
    }

    override fun getRecordsList(): Observable<List<Record>> = recordsDao
            .recordsList
            .observeOn(modelSchedulersProvider.computation)

    override fun getRecord(uuid: String): Observable<Wrapper<Record>> = recordsDao
            .recordsList
            .observeOn(modelSchedulersProvider.computation)
            .map { records ->
                records
                        .singleOrNull { it.uuid == uuid }
                        .wrap()
            }
            .distinctUntilChanged()

    override fun getSumLastDays(recordTypeId: Long, days: Int): Observable<Long> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -days + 1)
        val startDate = calendar.toDateInt()

        return recordsDao
                .recordsList
                .observeOn(modelSchedulersProvider.computation)
                .map {
                    it
                            .filter {
                                it.recordTypeId == recordTypeId
                                        && it.change?.changeKindId != Const.CHANGE_KIND_DELETE
                                        && it.date >= startDate
                            }
                            .sumByLong { it.value.toLong() }
                }
    }

    override fun getSumLastMinutes(recordTypeId: Long, minutes: Int): Observable<Long> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, -minutes + 1)
        val startDate = calendar.toDateInt()
        val startTime = calendar.toTimeInt()

        return recordsDao
                .recordsList
                .observeOn(modelSchedulersProvider.computation)
                .map {
                    it
                            .filter { record ->
                                record.recordTypeId == recordTypeId
                                        && record.change?.changeKindId != Const.CHANGE_KIND_DELETE
                                        && record.time != null
                                        && (record.date > startDate || (record.date == startDate && record.time >= startTime))
                            }
                            .sumByLong { it.value.toLong() }
                }
    }

    override fun getDumpText(): Single<String> = recordsDao.recordsList
            .firstOrError()
            .mapList { "${it.uuid},${it.recordTypeId},${it.date.toDateString()},${it.time?.toTimeString()},${it.kind},${it.value}" }
            .map { it.reduce { acc, s -> "$acc\n$s" } }

    override fun getRecordCreatedLocallyEvents(): Observable<String> = recordCreatedLocallyEvents

    override fun getLocalChangesCount(recordTypeIds: List<Long>): Observable<Int> = recordsDao
            .recordsList
            .observeOn(modelSchedulersProvider.computation)
            .map { it.filter { it.change != null }.size }

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