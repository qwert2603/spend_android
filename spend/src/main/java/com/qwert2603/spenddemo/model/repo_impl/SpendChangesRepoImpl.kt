package com.qwert2603.spenddemo.model.repo_impl

import com.qwert2603.andrlib.schedulers.ModelSchedulersProvider
import com.qwert2603.spenddemo.model.entity.SpendChange
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.repo.SpendChangesRepo
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpendChangesRepoImpl @Inject constructor(
        private val localDB: LocalDB,
        private val modelSchedulersProvider: ModelSchedulersProvider
) : SpendChangesRepo {

    override fun getAllChanges(): Single<List<SpendChange>> = localDB.spendsDao()
            .getAllLocallyChangedSpends()
            .subscribeOn(modelSchedulersProvider.io)
}