package com.qwert2603.spenddemo.model.repo

import com.qwert2603.spenddemo.model.entity.CreatingRecord
import io.reactivex.Completable
import io.reactivex.Single

interface DraftRepo {
    fun saveDraft(creatingRecord: CreatingRecord): Completable
    fun getDraft(): Single<CreatingRecord>
}