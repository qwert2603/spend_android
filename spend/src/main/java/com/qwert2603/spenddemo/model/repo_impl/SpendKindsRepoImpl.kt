package com.qwert2603.spenddemo.model.repo_impl

import android.arch.lifecycle.LiveData
import com.qwert2603.spenddemo.model.entity.SpendKind
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.tables.toSpendKind
import com.qwert2603.spenddemo.model.repo.SpendKindsRepo
import com.qwert2603.spenddemo.utils.map
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpendKindsRepoImpl @Inject constructor(localDB: LocalDB) : SpendKindsRepo {

    private val kindsSortedByPopularity: LiveData<List<SpendKind>> = localDB.spendsDao()
            .getAllKings()
            .map { it.map { it.toSpendKind() } }

    init {
        // to load them.
        kindsSortedByPopularity.observeForever { }
    }

    override fun getAllKinds(): List<SpendKind> = kindsSortedByPopularity.value!!

    override fun getKind(kind: String): SpendKind? = kindsSortedByPopularity.value!!
            .let { it.singleOrNull { it.kind == kind } }

    override fun getKindSuggestions(inputKind: String, count: Int): Single<List<String>> = kindsSortedByPopularity.value!!
            .map { it.kind }
            .let { spendKinds ->
                spendKinds
                        .filter { it.contains(inputKind, ignoreCase = true) }
                        .sortedBy { it.indexOf(inputKind, ignoreCase = true) }
                        .take(count)
                        .let {
                            if (it.isNotEmpty() || inputKind.length !in 3..5) Single.just(it)
                            else {
                                Single
                                        .fromCallable {
                                            // todo: to separate function.
                                            // consume one-symbol typos.
                                            val result = mutableSetOf<String>()
                                            for (f in 0 until inputKind.length) {
                                                for (ch in ('a'..'z') + ('а'..'я')) {
                                                    val fixedInputKind = inputKind.replaceInPos(f, ch)
                                                    spendKinds
                                                            .filter { it.contains(fixedInputKind, ignoreCase = true) }
                                                            .let { result.addAll(it) }
                                                    if (result.size >= count) return@fromCallable result
                                                }
                                            }
                                            result
                                        }
                                        .map { it.take(count) }
                                        .subscribeOn(Schedulers.computation())
                            }
                        }
            }

    private fun String.replaceInPos(pos: Int, c: Char) = substring(0, pos) + c + substring(pos + 1)
}