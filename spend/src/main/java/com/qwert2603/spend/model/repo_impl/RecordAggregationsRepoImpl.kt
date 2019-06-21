package com.qwert2603.spend.model.repo_impl

import androidx.annotation.VisibleForTesting
import com.qwert2603.andrlib.schedulers.ModelSchedulersProvider
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spend.model.entity.Record
import com.qwert2603.spend.model.entity.RecordCategory
import com.qwert2603.spend.model.entity.RecordCategoryAggregation
import com.qwert2603.spend.model.entity.RecordKindAggregation
import com.qwert2603.spend.model.local_db.dao.RecordsDao
import com.qwert2603.spend.model.repo.RecordAggregationsRepo
import com.qwert2603.spend.utils.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject

class RecordAggregationsRepoImpl(
        recordsDao: RecordsDao,
        private val modelSchedulersProvider: ModelSchedulersProvider
) : RecordAggregationsRepo {

    data class AggregationResult(
            val recordsCategoriesList: Map<Long, List<RecordCategoryAggregation>>,
            val recordsKindsLists: Map<Long, Map<String?, List<RecordKindAggregation>>>
    )

    /** key is recordTypeId. */
    private val recordsCategoriesList: BehaviorSubject<Map<Long, List<RecordCategoryAggregation>>> = BehaviorSubject.create()

    /**
     * {recordTypeId ; {recordCategoryUuid? ; recordKinds}}.
     * recordCategoryUuid == null means all recordKinds for given recordTypeId.
     */
    private val recordsKindsLists: BehaviorSubject<Map<Long, Map<String?, List<RecordKindAggregation>>>> = BehaviorSubject.create()

    init {
        Observable
                .combineLatest(
                        recordsDao.recordsList,
                        recordsDao.recordCategoriesList,
                        makePair()
                )
                .observeOn(modelSchedulersProvider.computation)
                .map { aggregate(it.first, it.second) }
                .subscribe(
                        {
                            recordsCategoriesList.onNext(it.recordsCategoriesList)
                            recordsKindsLists.onNext(it.recordsKindsLists)
                        },
                        { LogUtils.e("RecordAggregationsRepoImpl recordsKindsLists error!", it) }
                ).also { }
    }

    override fun getRecordCategories(recordTypeId: Long): Observable<List<RecordCategoryAggregation>> = recordsCategoriesList
            .map { it[recordTypeId] }

    override fun getRecordCategory(recordCategoryUuid: String): Observable<RecordCategoryAggregation> = recordsCategoriesList
            .map { it.values.flatten().single { category -> category.recordCategory.uuid == recordCategoryUuid } }
            .distinctUntilChanged()

    override fun getRecordCategory(recordTypeId: Long, recordCategoryName: String): Observable<Wrapper<RecordCategoryAggregation>> = recordsCategoriesList
            .map { it[recordTypeId]!!.find { category -> category.recordCategory.name == recordCategoryName }.wrap() }
            .distinctUntilChanged()

    override fun getRecordKinds(recordTypeId: Long, recordCategoryUuid: String?): Observable<List<RecordKindAggregation>> = recordsKindsLists
            .map { it[recordTypeId]!![recordCategoryUuid] }

    override fun getRecordKind(recordTypeId: Long, recordCategoryUuid: String?, kind: String): Observable<Wrapper<RecordKindAggregation>> = recordsKindsLists
            .map { it[recordTypeId]!![recordCategoryUuid]!!.find { recordKind -> recordKind.kind == kind }.wrap() }
            .distinctUntilChanged()

    override fun getKindSuggestions(recordTypeId: Long, recordCategoryUuid: String?, inputKind: String, count: Int): Single<List<RecordKindAggregation>> = recordsKindsLists
            .firstOrError()
            .map { it[recordTypeId]!![recordCategoryUuid] }
            .map { it.findSuggestions(inputKind, count) { it.kind } }
            .subscribeOn(modelSchedulersProvider.computation)

    override fun getCategorySuggestions(recordTypeId: Long, inputCategoryName: String, count: Int): Single<List<RecordCategoryAggregation>> = recordsCategoriesList
            .firstOrError()
            .map { it[recordTypeId]!! }
            .map { it.findSuggestions(inputCategoryName, count) { it.recordCategory.name } }
            .subscribeOn(modelSchedulersProvider.computation)

    companion object {

        private fun <T> List<T>.findSuggestions(
                query: String,
                count: Int,
                extractor: (T) -> String
        ): List<T> = this@findSuggestions
                .filter { extractor(it).contains(query, ignoreCase = true) }
                .sortedBy { extractor(it).indexOf(query, ignoreCase = true) }
                .take(count)
                .let { list ->
                    if (list.isNotEmpty() || query.length !in 3..5) list
                    else {
                        // consume one-symbol typos.
                        this@findSuggestions
                                .findWithTypo(query, count, extractor)
                                .take(count)
                    }
                }

        private fun String.replaceInPos(pos: Int, c: Char) = substring(0, pos) + c + substring(pos + 1)

        private fun <T> List<T>.findWithTypo(
                search: String,
                limit: Int,
                extractor: (T) -> String
        ): List<T> {
            val result = mutableSetOf<T>()
            for (f in 0 until search.length) {
                for (ch in ('a'..'z') + ('а'..'я')) {
                    val fixedInputKind = search.replaceInPos(f, ch)
                    for (r in this.filter { extractor(it).contains(fixedInputKind, ignoreCase = true) }) {
                        result.add(r)
                        if (result.size >= limit) return result.toList()
                    }
                }
            }
            return result.toList()
        }

        private val Record.timeNN: Int get() = time?.time ?: -1

        private val kindsComparator = Comparator<RecordKindAggregation> { k1, k2 ->
            return@Comparator when {
                k1.recordsCount != k2.recordsCount -> k1.recordsCount.compareTo(k2.recordsCount).unaryMinus()
                k1.lastRecord.date != k2.lastRecord.date -> k1.lastRecord.date.compareTo(k2.lastRecord.date).unaryMinus()
                k1.lastRecord.timeNN != k2.lastRecord.timeNN -> k1.lastRecord.timeNN.compareTo(k2.lastRecord.timeNN).unaryMinus()
                k1.lastRecord.recordCategory.name != k2.lastRecord.recordCategory.name -> k1.lastRecord.recordCategory.name.compareTo(k2.lastRecord.recordCategory.name)
                else -> k1.kind.compareTo(k2.kind)
            }
        }

        private val RecordCategoryAggregation.dateNN: Int get() = lastRecord?.date?.date ?: -1
        private val RecordCategoryAggregation.timeNN: Int get() = lastRecord?.timeNN ?: -1

        private val categoriesComparator = Comparator<RecordCategoryAggregation> { c1, c2 ->
            return@Comparator when {
                c1.recordsCount != c2.recordsCount -> c1.recordsCount.compareTo(c2.recordsCount).unaryMinus()
                c1.dateNN != c2.dateNN -> c1.dateNN.compareTo(c2.dateNN).unaryMinus()
                c1.timeNN != c2.timeNN -> c1.timeNN.compareTo(c2.timeNN).unaryMinus()
                else -> c1.recordCategory.name.compareTo(c2.recordCategory.name)
            }
        }

        @VisibleForTesting
        fun aggregate(
                records: List<Record>,
                categories: List<RecordCategory>
        ): AggregationResult {
            val b = System.currentTimeMillis()

            val recordsCategoriesList: HashMap<Long, List<RecordCategoryAggregation>> = hashMapOf()
            val recordsKindsLists: HashMap<Long, HashMap<String?, List<RecordKindAggregation>>> = hashMapOf()

            val categoriesByType = categories.groupBy { it.recordTypeId }
            val recordsByCategory = records
                    .filter { !it.isDeleted() }
                    .groupBy { it.recordCategory.uuid }

            for (recordTypeId in Const.RECORD_TYPE_IDS) {
                recordsKindsLists[recordTypeId] = hashMapOf()

                categoriesByType.getOrElse(recordTypeId) { emptyList() }
                        .forEach { category ->

                            val counts = hashMapOf<String, Int>()
                            val totalValues = hashMapOf<String, Long>()
                            val lasts = hashMapOf<String, Record>()

                            recordsByCategory.getOrElse(category.uuid) { emptyList() }
                                    .forEach { record ->
                                        counts[record.kind] = (counts[record.kind] ?: 0) + 1
                                        totalValues[record.kind] = (totalValues[record.kind]
                                                ?: 0) + record.value
                                        val prevLast = lasts[record.kind]
                                        lasts[record.kind] =
                                                if (prevLast != null) {
                                                    maxOf(
                                                            prevLast,
                                                            record,
                                                            Comparator { r1, r2 ->
                                                                return@Comparator when {
                                                                    r1.date != r2.date -> r1.date.compareTo(r2.date)
                                                                    r1.timeNN != r2.timeNN -> r1.timeNN.compareTo(r2.timeNN)
                                                                    else -> r1.uuid.compareTo(r2.uuid)
                                                                }
                                                            }
                                                    )
                                                } else {
                                                    record
                                                }
                                    }

                            recordsKindsLists[recordTypeId]!![category.uuid] = lasts
                                    .map { (kind, lastRecord) ->
                                        RecordKindAggregation(
                                                recordTypeId = recordTypeId,
                                                recordCategory = category,
                                                kind = kind,
                                                lastRecord = lastRecord,
                                                recordsCount = counts[kind]!!,
                                                totalValue = totalValues[kind]!!
                                        )
                                    }
                                    .sortedWith(kindsComparator)
                        }

                recordsCategoriesList[recordTypeId] = categoriesByType.getOrElse(recordTypeId) { emptyList() }
                        .map { category ->
                            val kinds = recordsKindsLists[recordTypeId]!![category.uuid]!!
                            RecordCategoryAggregation(
                                    recordTypeId = recordTypeId,
                                    recordCategory = category,
                                    lastRecord = kinds.map { it.lastRecord }.maxBy { it.dateTime() },
                                    recordsCount = kinds.sumBy { it.recordsCount },
                                    totalValue = kinds.sumByLong { it.totalValue }
                            )
                        }
                        .sortedWith(categoriesComparator)

                recordsKindsLists[recordTypeId]!![null] = recordsKindsLists[recordTypeId]!!
                        .map { (_, kinds) -> kinds }
                        .flatten()
                        .sortedWith(kindsComparator)
            }

            LogUtils.d("timing_ RecordAggregationsRepoImpl aggregate ${System.currentTimeMillis() - b} ms")

            return AggregationResult(recordsCategoriesList, recordsKindsLists)
        }
    }
}