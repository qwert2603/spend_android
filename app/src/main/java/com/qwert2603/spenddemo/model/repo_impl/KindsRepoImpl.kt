package com.qwert2603.spenddemo.model.repo_impl

import com.qwert2603.spenddemo.model.entity.Kind
import com.qwert2603.spenddemo.model.entity.SourceType
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.remote_db.RemoteDBFacade
import com.qwert2603.spenddemo.model.repo.KindsRepo
import com.qwert2603.spenddemo.model.schedulers.ModelSchedulersProvider
import com.qwert2603.spenddemo.utils.mapList
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KindsRepoImpl @Inject constructor(
        private val localDB: LocalDB,
        private val remoteDBFacade: RemoteDBFacade,
        private val modelSchedulersProvider: ModelSchedulersProvider
) : KindsRepo {
    override fun getAllKinds(sourceType: SourceType): Single<List<Kind>> = when (sourceType) {
        SourceType.LOCAL -> localDB.kindsDao()
                .getAllKings()
                .mapList { Kind(it) }
        SourceType.REMOTE -> Single.fromCallable { remoteDBFacade.getAllKinds() }
    }.subscribeOn(modelSchedulersProvider.io)
}