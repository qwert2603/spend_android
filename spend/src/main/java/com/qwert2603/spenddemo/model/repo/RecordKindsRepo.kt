package com.qwert2603.spenddemo.model.repo

import com.qwert2603.spenddemo.model.entity.RecordKind
import com.qwert2603.spenddemo.utils.Wrapper
import io.reactivex.Observable
import io.reactivex.Single

interface RecordKindsRepo {

    fun getRecordKinds(recordKindId: Long): Observable<List<RecordKind>>

    fun getRecordKind(recordKindId: Long, kind: String): Observable<Wrapper<RecordKind>>

    fun getKindSuggestions(recordKindId: Long, inputKind: String, count: Int): Single<List<String>>
}