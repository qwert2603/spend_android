package com.qwert2603.spenddemo.model.repo

import com.qwert2603.spenddemo.model.entity.SpendChange
import io.reactivex.Single

interface SpendChangesRepo {
    fun getAllChanges(): Single<List<SpendChange>>
}