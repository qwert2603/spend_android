package com.qwert2603.spend.model.repo

import com.qwert2603.spend.model.entity.RecordCategoryAggregation
import com.qwert2603.spend.model.entity.RecordKindAggregation
import com.qwert2603.spend.utils.Wrapper
import io.reactivex.Observable
import io.reactivex.Single

interface RecordAggregationsRepo {

    fun getRecordCategories(recordTypeId: Long): Observable<List<RecordCategoryAggregation>>

    fun getRecordCategory(recordCategoryUuid: String): Observable<RecordCategoryAggregation>

    fun getRecordCategory(
            recordTypeId: Long,
            recordCategoryName: String
    ): Observable<Wrapper<RecordCategoryAggregation>>

    fun getRecordKinds(
            recordTypeId: Long,
            recordCategoryUuid: String?
    ): Observable<List<RecordKindAggregation>>

    fun getRecordKind(
            recordTypeId: Long,
            recordCategoryUuid: String?,
            kind: String
    ): Observable<Wrapper<RecordKindAggregation>>

    /** @return list of kinds names. */
    fun getKindSuggestions(
            recordTypeId: Long,
            recordCategoryUuid: String?,
            inputKind: String,
            count: Int
    ): Single<List<RecordKindAggregation>>

    fun getCategorySuggestions(
            recordTypeId: Long,
            inputCategoryName: String,
            count: Int
    ): Single<List<RecordCategoryAggregation>>
}