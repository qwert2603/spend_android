package com.qwert2603.spenddemo.model.repo_impl

import com.qwert2603.andrlib.schedulers.ModelSchedulersProvider
import com.qwert2603.andrlib.util.mapList
import com.qwert2603.spenddemo.model.entity.SpendChange
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.tables.toChange
import com.qwert2603.spenddemo.model.local_db.tables.toChangeTable
import com.qwert2603.spenddemo.model.repo.SpendChangesRepo
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpendChangesRepoImpl @Inject constructor(
        private val localDB: LocalDB,
        private val modelSchedulersProvider: ModelSchedulersProvider
) : SpendChangesRepo {

    override fun getAllChanges(): Single<List<SpendChange>> = localDB.spendChangesDao()
            .getAllChanges()
            .mapList { it.toChange() }
            .subscribeOn(modelSchedulersProvider.io)

    override fun saveChange(spendChange: SpendChange): Completable = Completable
            .fromAction { localDB.spendChangesDao().saveChange(spendChange.toChangeTable()) }
            .subscribeOn(modelSchedulersProvider.io)

    override fun removeChange(spendId: Long): Completable = Completable
            .fromAction { localDB.spendChangesDao().removeChange(spendId) }
            .subscribeOn(modelSchedulersProvider.io)
}