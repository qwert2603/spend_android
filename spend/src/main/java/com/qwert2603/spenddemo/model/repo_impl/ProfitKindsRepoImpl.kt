package com.qwert2603.spenddemo.model.repo_impl

import com.qwert2603.andrlib.schedulers.ModelSchedulersProvider
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.repo.ProfitKindsRepo
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfitKindsRepoImpl @Inject constructor(
        private val localDB: LocalDB,
        private val modelSchedulersProvider: ModelSchedulersProvider
) : ProfitKindsRepo {
    override fun getAllKinds(): Single<List<String>> = localDB.profitsDao()
            .getKinds()
            .subscribeOn(modelSchedulersProvider.io)
}