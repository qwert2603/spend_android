package com.qwert2603.spenddemo.model.repo

import com.qwert2603.spenddemo.model.entity.Kind
import io.reactivex.Observable
import io.reactivex.Single

interface KindsRepo {

    fun getAllKinds(): Observable<List<Kind>>

    fun getKind(kind: String): Single<Kind>

    /** [inputKind] if is empty, all kinds are suggested. */
    fun getKindSuggestions(inputKind: String, count: Int = 5): Single<List<String>>
}