package com.qwert2603.spenddemo.model.repo_impl

import android.arch.lifecycle.LiveData
import com.qwert2603.spenddemo.model.entity.SpendKind
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.tables.toSpendKind
import com.qwert2603.spenddemo.model.repo.SpendKindsRepo
import com.qwert2603.spenddemo.utils.map
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

    override fun getKindSuggestions(inputKind: String, count: Int): List<String> = kindsSortedByPopularity.value
            .let {
                // todo: consume one-symbol typos.
                it!!
                        .map { it.kind }
                        .filter { it.contains(inputKind, ignoreCase = true) }
                        .sortedBy { it.indexOf(inputKind, ignoreCase = true) }
                        .take(count)
            }
}