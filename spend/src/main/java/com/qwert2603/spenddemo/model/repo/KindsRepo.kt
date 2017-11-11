package com.qwert2603.spenddemo.model.repo

import com.qwert2603.spenddemo.model.entity.Kind
import io.reactivex.Observable

interface KindsRepo {
    fun getAllKinds(): Observable<List<Kind>>
}