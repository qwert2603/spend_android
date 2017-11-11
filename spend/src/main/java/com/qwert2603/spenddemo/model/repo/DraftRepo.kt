package com.qwert2603.spenddemo.model.repo

import com.qwert2603.spenddemo.model.entity.CreatingRecord
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*

interface DraftRepo {
    fun saveDraft(creatingRecord: CreatingRecord): Completable
    fun getDraft(): Observable<CreatingRecord>
    fun removeDraft(): Completable

    val dateSelected: PublishSubject<Date>
    val kindSelected: PublishSubject<String>
}