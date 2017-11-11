package com.qwert2603.spenddemo.model.repo_impl

import com.qwert2603.spenddemo.model.entity.Kind
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.repo.KindsRepo
import com.qwert2603.spenddemo.model.schedulers.ModelSchedulersProvider
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KindsRepoImpl @Inject constructor(
        localDB: LocalDB,
        modelSchedulersProvider: ModelSchedulersProvider
) : KindsRepo {

    private val localKinds: BehaviorSubject<List<Kind>> = localDB.kindsDao()
            .getAllKings()
            .toObservable()
            .map {
                it
                        .groupBy { it.kind }
                        .map { it.value }
                        .sortedByDescending { it.size }
                        .map {  it.maxBy { it.date }!! }
                        .map { Kind(it.kind, it.value, it.date) }
            }
            .subscribeOn(modelSchedulersProvider.io)
            .subscribeWith(BehaviorSubject.create())

    override fun getAllKinds(): Observable<List<Kind>> = localKinds
}