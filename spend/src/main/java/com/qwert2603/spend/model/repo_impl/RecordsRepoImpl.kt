package com.qwert2603.spend.model.repo_impl

import android.content.Context
import com.google.gson.Gson
import com.qwert2603.andrlib.schedulers.ModelSchedulersProvider
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spend.model.entity.*
import com.qwert2603.spend.model.local_db.dao.RecordsDao
import com.qwert2603.spend.model.repo.RecordsRepo
import com.qwert2603.spend.model.sync_processor.IsShowingToUserHolder
import com.qwert2603.spend.model.sync_processor.SyncProcessor
import com.qwert2603.spend.utils.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.io.PrintWriter
import java.util.*
import java.util.concurrent.TimeUnit

class RecordsRepoImpl(
        private val recordsDao: RecordsDao,
        private val syncProcessor: SyncProcessor,
        private val appContext: Context,
        private val modelSchedulersProvider: ModelSchedulersProvider,
        isShowingToUserHolder: IsShowingToUserHolder
) : RecordsRepo {

    private val recordCreatedLocallyEvents = PublishSubject.create<String>()

    private val recordEditedLocallyEvents = PublishSubject.create<String>()

    private val recordCombinedLocallyEvents = PublishSubject.create<String>()

    init {
        isShowingToUserHolder.isShowingToUser.subscribe {
            LogUtils.d("RecordsRepoImpl isShowingToUser $it")

            if (it) {
                syncProcessor.start()
            } else {
                syncProcessor.stop()
            }
        }
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

    override fun getSumLastDays(recordTypeId: Long, days: Days, recordsFilters: RecordsFilters?): Observable<Long> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -days.days + 1)
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
                                        && recordsFilters?.check(record) != false
                            }
                            .sumByLong { it.value.toLong() }
                }
                .distinctUntilChanged()
    }

    override fun getSumLastMinutes(recordTypeId: Long, minutes: Minutes, recordsFilters: RecordsFilters?): Observable<Long> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, -minutes.minutes + 1)
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
                                        && (record.date > startDate || (record.date == startDate && (record.time
                                        ?: STime(0)) >= startTime))
                                        && recordsFilters?.check(record) != false
                            }
                            .sumByLong { it.value.toLong() }
                }
                .distinctUntilChanged()
    }

    override fun getDumpFile(): Single<File> = Single
            .fromCallable { recordsDao.getDump() }
            .map { dump ->
                val filename = "spend_dump.json"
                val dir = File(appContext.filesDir, "dumps")
                dir.mkdirs()
                val file = File(dir, filename)
                PrintWriter(file).use { it.write(Gson().toJson(dump)) }
                file
            }
            .subscribeOn(modelSchedulersProvider.computation)

    override fun getNotDeletedRecordsHash(): Observable<String> = recordsDao
            .recordsList
            .buffer(1L, TimeUnit.SECONDS)
            .mapNotNull { it.lastOrNull() }
            .startWith(recordsDao.recordsList.take(1))
            .map { records ->
                val currentTimeMillis = System.currentTimeMillis()
                records
                        .calculateNotDeletedRecordsHash()
                        .also { LogUtils.d("timing_ getNotDeletedRecordsHash() ${System.currentTimeMillis() - currentTimeMillis} ms") }
            }
            .subscribeOn(modelSchedulersProvider.computation)

    override fun getRecordCreatedLocallyEvents(): Observable<String> = recordCreatedLocallyEvents.hide()

    override fun getRecordEditedLocallyEvents(): Observable<String> = recordEditedLocallyEvents.hide()

    override fun getRecordCombinedLocallyEvents(): Observable<String> = recordCombinedLocallyEvents.hide()

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

    override fun getSyncState(): Observable<SyncState> = syncProcessor.syncState.hide()

    override fun combineRecords(recordUuids: List<String>, categoryUuid: String, kind: String) {
        LogUtils.d { "RecordsRepoImpl combineRecords $recordUuids $categoryUuid $kind" }

        val newRecordUuid = UUID.randomUUID().toString()

        recordCombinedLocallyEvents.onNext(newRecordUuid)

        syncProcessor.combineRecords(
                recordUuids = recordUuids,
                categoryUuid = categoryUuid,
                kind = kind,
                newRecordUuid = newRecordUuid
        )
    }

    override fun changeRecords(recordsUuids: List<String>, changedDate: SDate?, changedTime: Wrapper<STime>?) {
        LogUtils.d { "RecordsRepoImpl changeRecords $recordsUuids $changedDate $changedTime" }

        syncProcessor.changeRecords(recordsUuids, changedDate, changedTime)
    }

    companion object {

        private fun List<Record>.calculateNotDeletedRecordsHash(): String = this
                .filter { !it.isDeleted() }
                .sortedBy { it.uuid }
                .map { it.toNotDeletedRecord() }
                .toString()
                .sha256()
    }
}