package com.qwert2603.spenddemo.model.repo_impl

import com.qwert2603.andrlib.schedulers.ModelSchedulersProvider
import com.qwert2603.andrlib.util.mapList
import com.qwert2603.spenddemo.model.entity.Kind
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.repo.KindsRepo
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KindsRepoImpl @Inject constructor(
        private val localDB: LocalDB,
        private val modelSchedulersProvider: ModelSchedulersProvider
) : KindsRepo {

    override fun getAllKinds(): Observable<List<Kind>> = getKindsSortedByPopularity()
            .subscribeOn(modelSchedulersProvider.io)

    override fun getKind(kind: String): Single<Kind> = getKindsSortedByPopularity()
            .firstOrError()
            .map { it.single { it.kind == kind } }
            .subscribeOn(modelSchedulersProvider.io)

    override fun getKindSuggestions(inputKind: String, count: Int): Single<List<String>> = getKindsSortedByPopularity()
            .firstOrError()
            .mapList { it.kind }
            .map {
                // todo: consume one-symbol typos.
                it
                        .filter { it.contains(inputKind, ignoreCase = true) }
                        .sortedBy { it.indexOf(inputKind, ignoreCase = true) }
                        .take(count)
            }
            .subscribeOn(modelSchedulersProvider.io)

    // todo: BehaviorSubject<List<Kind>>.
    private fun getKindsSortedByPopularity(): Observable<List<Kind>> = localDB.kindsDao()
            .getAllKings()
            .toObservable()
            .map {
                it
                        .groupBy { it.kind }
                        .map { it.value }
                        .sortedByDescending { it.size }
                        .map { it.maxBy { it.date }!! }
                        .map { Kind(it.kind, it.value, it.date) }
            }
}