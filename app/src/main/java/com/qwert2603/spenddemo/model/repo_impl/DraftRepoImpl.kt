package com.qwert2603.spenddemo.model.repo_impl

import com.qwert2603.spenddemo.draft.DraftInteractor
import com.qwert2603.spenddemo.model.entity.CreatingRecord
import com.qwert2603.spenddemo.model.repo.DraftRepo
import com.qwert2603.spenddemo.utils.LogUtils
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DraftRepoImpl @Inject constructor() : DraftRepo {

    @Volatile private var draft: CreatingRecord = DraftInteractor.EmptyCreatingRecord
        set(value) {
            LogUtils.d("DraftRepoImpl ${hashCode()} draft = $value")
            field = value
        }

    private val draftChanges: PublishSubject<CreatingRecord> = PublishSubject.create()

    override fun saveDraft(creatingRecord: CreatingRecord): Completable = Completable
            .fromAction { draft = creatingRecord }

    override fun getDraft(): Observable<CreatingRecord> = draftChanges
            .startWith(draft)

    override fun removeDraft(): Completable = Completable.fromAction { draft = DraftInteractor.EmptyCreatingRecord }

    override fun onDateSelected(date: Date) {
        draft = draft.copy(date = date)
        draftChanges.onNext(draft)
    }

    override fun onKindSelected(kind: String) {
        draft = draft.copy(kind = kind)
        draftChanges.onNext(draft)
    }
}