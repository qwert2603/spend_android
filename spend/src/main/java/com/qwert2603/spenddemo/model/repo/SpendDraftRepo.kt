package com.qwert2603.spenddemo.model.repo

import com.qwert2603.spenddemo.model.entity.CreatingSpend
import io.reactivex.Completable
import io.reactivex.Single

interface SpendDraftRepo {
    fun saveDraft(creatingSpend: CreatingSpend): Completable
    fun getDraft(): Single<CreatingSpend>
    fun removeDraft(): Completable
}