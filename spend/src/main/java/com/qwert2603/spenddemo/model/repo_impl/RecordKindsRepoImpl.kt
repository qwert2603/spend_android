package com.qwert2603.spenddemo.model.repo_impl

import com.qwert2603.andrlib.schedulers.ModelSchedulersProvider
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.RecordKind
import com.qwert2603.spenddemo.model.local_db.dao.RecordsDao
import com.qwert2603.spenddemo.model.repo.RecordKindsRepo
import com.qwert2603.spenddemo.utils.Const
import com.qwert2603.spenddemo.utils.Wrapper
import com.qwert2603.spenddemo.utils.wrap
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordKindsRepoImpl @Inject constructor(
        recordsDao: RecordsDao,
        private val modelSchedulersProvider: ModelSchedulersProvider
) : RecordKindsRepo {

    private val recordsKindsLists = BehaviorSubject.create<Map<Long, List<RecordKind>>>()

    init {
        recordsDao.recordsList
                .observeOn(modelSchedulersProvider.computation)
                .map { it.toRecordKindsList() }
                .subscribe(
                        { recordsKindsLists.onNext(it) },
                        { LogUtils.e("RecordKindsRepoImpl recordsKindsLists error!", it) }
                ).also { }
    }

    override fun getRecordKinds(recordKindId: Long): Observable<List<RecordKind>> = recordsKindsLists.map { it[recordKindId] }

    override fun getRecordKind(recordKindId: Long, kind: String): Observable<Wrapper<RecordKind>> = recordsKindsLists
            .map { it[recordKindId]!!.find { recordKind -> recordKind.kind == kind }.wrap() }

    override fun getKindSuggestions(recordKindId: Long, inputKind: String, count: Int): Single<List<String>> = recordsKindsLists
            .firstOrError()
            .map { it[recordKindId]!!.map { it.kind } }
            .map { spendKinds ->
                spendKinds
                        .filter { it.contains(inputKind, ignoreCase = true) }
                        .sortedBy { it.indexOf(inputKind, ignoreCase = true) }
                        .take(count)
                        .let {
                            if (it.isNotEmpty() || inputKind.length !in 3..5) it
                            else {
                                // consume one-symbol typos.
                                spendKinds.findWithTypo(inputKind, count).take(count)
                            }
                        }
            }
            .subscribeOn(modelSchedulersProvider.computation)

    companion object {

        private fun String.replaceInPos(pos: Int, c: Char) = substring(0, pos) + c + substring(pos + 1)

        private fun List<String>.findWithTypo(search: String, limit: Int): List<String> {
            val result = mutableSetOf<String>()
            for (f in 0 until search.length) {
                for (ch in ('a'..'z') + ('а'..'я')) {
                    val fixedInputKind = search.replaceInPos(f, ch)
                    this
                            .filter { it.contains(fixedInputKind, ignoreCase = true) }
                            .let { result.addAll(it) }
                    if (result.size >= limit) return result.take(limit)
                }
            }
            return result.take(limit)
        }

        private val Record.timeNN: Int get() = time ?: -1

        private fun List<Record>.toRecordKindsList(): Map<Long, List<RecordKind>> {
            val b = System.currentTimeMillis()

            val result = hashMapOf<Long, List<RecordKind>>()
            val byType = this
                    .filter { it.change?.changeKindId != Const.CHANGE_KIND_DELETE }
                    .groupBy { it.recordTypeId }
            for (recordTypeId in listOf(Const.RECORD_TYPE_ID_SPEND, Const.RECORD_TYPE_ID_PROFIT)) {
                val counts = hashMapOf<String, Int>()
                val lasts = hashMapOf<String, Record>()

                byType.getOrElse(recordTypeId) { emptyList() }
                        .forEach { record ->
                            counts[record.kind] = (counts[record.kind] ?: 0) + 1
                            val prevLast = lasts[record.kind]
                            lasts[record.kind] =
                                    if (prevLast != null) {
                                        maxOf(
                                                prevLast,
                                                record,
                                                Comparator { r1, r2 ->
                                                    if (r1.date != r2.date) return@Comparator r1.date.compareTo(r2.date)
                                                    if (r1.timeNN != r2.timeNN) return@Comparator r1.timeNN.compareTo(r2.timeNN)
                                                    return@Comparator r1.uuid.compareTo(r2.uuid)
                                                }
                                        )
                                    } else {
                                        record
                                    }
                        }

                result[recordTypeId] = lasts.values
                        .map {
                            RecordKind(
                                    recordTypeId = recordTypeId,
                                    kind = it.kind,
                                    lastDate = it.date,
                                    lastTime = it.time,
                                    lastValue = it.value,
                                    recordsCount = counts[it.kind]!!
                            )
                        }
                        .sortedByDescending { it.recordsCount } // todo: sort by lastDate DESC and so on.
            }

            LogUtils.d("timing_ RecordKindsRepoImpl toRecordKindsList ${System.currentTimeMillis() - b} ms")

            return result
        }
    }
}