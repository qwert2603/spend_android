package com.qwert2603.spenddemo.model.repo

import com.qwert2603.spenddemo.model.entity.Kind
import com.qwert2603.spenddemo.model.entity.SourceType
import io.reactivex.Single

interface KindsRepo {
    fun getAllKinds(sourceType: SourceType): Single<List<Kind>>
}