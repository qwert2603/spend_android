package com.qwert2603.spenddemo.model.repo

import com.qwert2603.spenddemo.model.entity.SpendKind

interface SpendKindsRepo {

    fun getAllKinds(): List<SpendKind>

    fun getKind(kind: String): SpendKind?

    /** [inputKind] if is empty, all kinds are suggested. */
    fun getKindSuggestions(inputKind: String, count: Int = 5): List<String>
}