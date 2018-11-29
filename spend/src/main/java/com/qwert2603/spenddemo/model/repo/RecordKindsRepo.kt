package com.qwert2603.spenddemo.model.repo

import com.qwert2603.spenddemo.model.entity.RecordCategoryAggregation
import com.qwert2603.spenddemo.model.entity.RecordKind
import com.qwert2603.spenddemo.utils.Wrapper
import io.reactivex.Observable
import io.reactivex.Single

// todo: RecordAggregationsRepo
interface RecordKindsRepo {

    fun getRecordCategories(recordTypeId: Long): Observable<List<RecordCategoryAggregation>>

    fun getRecordCategory(recordCategoryUuid: String): Observable<RecordCategoryAggregation>

    fun getRecordCategory(
            recordTypeId: Long,
            recordCategoryName: String
    ): Observable<Wrapper<RecordCategoryAggregation>>

    fun getRecordKinds(
            recordTypeId: Long,
            recordCategoryUuid: String?
    ): Observable<List<RecordKind>>

    fun getRecordKind(
            recordTypeId: Long,
            recordCategoryUuid: String?,
            kind: String
    ): Observable<Wrapper<RecordKind>>

    /** @return list of kinds names. */
    fun getKindSuggestions(
            recordTypeId: Long,
            recordCategoryUuid: String?,
            inputKind: String,
            count: Int
    ): Single<List<RecordKind>>

    fun getCategorySuggestions(
            recordTypeId: Long,
            inputCategoryName: String,
            count: Int
    ): Single<List<RecordCategoryAggregation>>
}