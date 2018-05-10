package com.qwert2603.spenddemo.model.repo

import com.qwert2603.spenddemo.model.entity.Change
import io.reactivex.Completable
import io.reactivex.Single

interface ChangesRepo {
    fun getAllChanges(): Single<List<Change>>
    fun saveChange(change: Change): Completable
    fun removeChange(spendId: Long): Completable
}