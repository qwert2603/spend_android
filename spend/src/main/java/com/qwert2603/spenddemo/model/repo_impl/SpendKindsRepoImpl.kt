package com.qwert2603.spenddemo.model.repo_impl

import android.arch.lifecycle.LiveData
import com.qwert2603.andrlib.schedulers.ModelSchedulersProvider
import com.qwert2603.spenddemo.model.entity.SpendKind
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.tables.toSpendKind
import com.qwert2603.spenddemo.model.repo.SpendKindsRepo
import com.qwert2603.spenddemo.utils.map
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpendKindsRepoImpl @Inject constructor(
        localDB: LocalDB,
        private val modelSchedulersProvider: ModelSchedulersProvider
) : SpendKindsRepo {

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
                                // consume one-symbol typos.
                                Single
                                        .fromCallable { spendKinds.findWithTypo(inputKind, count) }
                                        .map { it.take(count) }
                            }
                        }
                        .subscribeOn(modelSchedulersProvider.computation)
            }

    companion object {

        private fun String.replaceInPos(pos: Int, c: Char) = substring(0, pos) + c + substring(pos + 1)

        private fun List<String>.findWithTypo(search: String, limit: Int): List<String> {
            val result = mutableSetOf<String>()
            for (f in 0 until search.length) {
                for (ch in ('a'..'z') + ('а'..'я')) {
                    val fixedInputKind = search.replaceInPos(f, ch)
                    this
                            .filter { it.contains(fixedInputKind, ignoreCase = true) }
                            .let { result.addAll(it) }
                    if (result.size >= limit) return result.take(limit)
                }
            }
            return result.take(limit)
        }

    }
}