package com.qwert2603.spenddemo.model.repo_impl

import android.content.Context
import com.google.gson.Gson
import com.qwert2603.andrlib.schedulers.ModelSchedulersProvider
import com.qwert2603.andrlib.util.mapList
import com.qwert2603.spenddemo.di.LocalDBExecutor
import com.qwert2603.spenddemo.di.RemoteDBExecutor
import com.qwert2603.spenddemo.model.entity.*
import com.qwert2603.spenddemo.model.local_db.dao.RecordsDao
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.model.rest.ApiHelper
import com.qwert2603.spenddemo.model.sync_processor.SyncProcessor
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
        apiHelper: ApiHelper,
        appContext: Context,
        @RemoteDBExecutor remoteDBExecutor: ExecutorService,
        @LocalDBExecutor localDBExecutor: ExecutorService,
        private val modelSchedulersProvider: ModelSchedulersProvider
) : RecordsRepo {

    private val prefs = appContext.getSharedPreferences("records.prefs", Context.MODE_PRIVATE)

    private val syncProcessor = SyncProcessor(
            remoteDBExecutor = remoteDBExecutor,
            localDBExecutor = localDBExecutor,
            lastChangeStorage = PrefsLastChangeStorage(prefs, Gson()),
            apiHelper = apiHelper,
            recordsDao = recordsDao,
            changeIdCounter = PrefsCounter(prefs, "last_change_id")
    )

    private val recordCreatedLocallyEvents = PublishSubject.create<String>()

    private val recordEditedLocallyEvents = PublishSubject.create<String>()

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
        val startDate = calendar.toSDate()

        return recordsDao
                .recordsList
                .observeOn(modelSchedulersProvider.computation)
                .map {
                    it
                            .filter { record ->
                                record.recordCategory.recordTypeId == recordTypeId
                                        && !record.isDeleted()
                                        && record.date >= startDate
                            }
                            .sumByLong { it.value.toLong() }
                }
                .distinctUntilChanged()
    }

    override fun getSumLastMinutes(recordTypeId: Long, minutes: Int): Observable<Long> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, -minutes + 1)
        val startDate = calendar.toSDate()
        val startTime = calendar.toSTime()

        return recordsDao
                .recordsList
                .observeOn(modelSchedulersProvider.computation)
                .map {
                    it
                            .filter { record ->
                                record.recordCategory.recordTypeId == recordTypeId
                                        && !record.isDeleted()
                                        && record.time != null
                                        && (record.date > startDate || (record.date == startDate && record.time >= startTime))
                            }
                            .sumByLong { it.value.toLong() }
                }
                .distinctUntilChanged()
    }

    override fun getDumpText(): Single<String> = recordsDao
            .recordsList
            .firstOrError()
            .mapList { "${it.uuid},${it.recordCategory.uuid},${it.date},${it.time},${it.kind},${it.value}" }
            .map { it.reduce { acc, s -> "$acc\n$s" } }

    override fun getRecordCreatedLocallyEvents(): Observable<String> = recordCreatedLocallyEvents.hide()

    override fun getRecordEditedLocallyEvents(): Observable<String> = recordEditedLocallyEvents.hide()

    override fun getLocalChangesCount(recordTypeIds: List<Long>): Observable<Int> = recordsDao
            .recordsList
            .observeOn(modelSchedulersProvider.computation)
            .map { it.filter { it.change != null }.size }

    override fun saveRecords(records: List<RecordDraft>) {
        records.forEach {
            if (it.isNewRecord) {
                recordCreatedLocallyEvents.onNext(it.uuid)
            } else {
                recordEditedLocallyEvents.onNext(it.uuid)
            }
        }
        syncProcessor.saveItems(records)
    }

    override fun removeRecords(recordsUuids: List<String>) {
        syncProcessor.removeItems(recordsUuids)
    }

    override fun removeAllRecords() {
        syncProcessor.clear()
    }

    override fun getSyncState(): Observable<SyncState> = syncProcessor.syncState
}