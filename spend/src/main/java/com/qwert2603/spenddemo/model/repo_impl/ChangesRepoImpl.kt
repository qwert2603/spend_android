package com.qwert2603.spenddemo.model.repo_impl

import com.qwert2603.andrlib.schedulers.ModelSchedulersProvider
import com.qwert2603.andrlib.util.mapList
import com.qwert2603.spenddemo.model.entity.Change
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.tables.toChange
import com.qwert2603.spenddemo.model.local_db.tables.toChangeTable
import com.qwert2603.spenddemo.model.repo.ChangesRepo
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChangesRepoImpl @Inject constructor(
        private val localDB: LocalDB,
        private val modelSchedulersProvider: ModelSchedulersProvider
) : ChangesRepo {

    override fun getAllChanges(): Single<List<Change>> = localDB.changesDao()
            .getAllChanges()
            .mapList { it.toChange() }
            .subscribeOn(modelSchedulersProvider.io)

    override fun saveChange(change: Change): Completable = Completable
            .fromAction { localDB.changesDao().saveChange(change.toChangeTable()) }
            .subscribeOn(modelSchedulersProvider.io)

    override fun removeChange(spendId: Long): Completable = Completable
            .fromAction { localDB.changesDao().removeChange(spendId) }
            .subscribeOn(modelSchedulersProvider.io)
}