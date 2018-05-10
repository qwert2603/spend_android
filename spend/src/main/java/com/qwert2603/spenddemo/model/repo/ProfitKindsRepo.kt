package com.qwert2603.spenddemo.model.repo

import io.reactivex.Single

interface ProfitKindsRepo {
    fun getAllKinds(): Single<List<String>>
}