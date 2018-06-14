package com.qwert2603.spenddemo.model.repo

import com.qwert2603.spenddemo.model.entity.SpendChange
import io.reactivex.Completable
import io.reactivex.Single

interface SpendChangesRepo {
    fun getAllChanges(): Single<List<SpendChange>>
    fun saveChange(spendChange: SpendChange): Completable
    fun removeChange(spendId: Long): Completable
}