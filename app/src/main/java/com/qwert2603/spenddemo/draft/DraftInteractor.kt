package com.qwert2603.spenddemo.draft

import com.qwert2603.spenddemo.model.entity.CreatingRecord
import com.qwert2603.spenddemo.model.repo.DraftRepo
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*
import javax.inject.Inject

class DraftInteractor @Inject constructor(
        private val draftRepo: DraftRepo,
        private val recordsRepo: RecordsRepo
) {
    companion object {
        val EmptyCreatingRecord  = CreatingRecord("", 0, Date())
    }

    private val clearEvents = PublishSubject.create<Unit>()

    fun getDraft(): Observable<CreatingRecord> = draftRepo.getDraft()

    fun onDraftChanged(creatingRecord: CreatingRecord): Completable = draftRepo.saveDraft(creatingRecord)

    fun createRecord(creatingRecord: CreatingRecord): Completable {
        if (!isValid(creatingRecord)) return Completable.error(IllegalArgumentException())
        clearEvents.onNext(Unit)
        recordsRepo.addRecord(creatingRecord)
        return draftRepo.removeDraft()
    }

    fun isValid(creatingRecord: CreatingRecord) = creatingRecord.kind.isNotBlank() && creatingRecord.value > 0

    fun clearEvents(): Observable<Unit> = clearEvents
}