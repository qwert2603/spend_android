package com.qwert2603.spenddemo.draft

import com.qwert2603.spenddemo.model.entity.CreatingRecord
import com.qwert2603.spenddemo.model.repo.DraftRepo
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.utils.LogUtils
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import java.util.*
import javax.inject.Inject

class DraftInteractor @Inject constructor(
        private val draftRepo: DraftRepo,
        private val recordsRepo: RecordsRepo
) {
    companion object {
        fun createEmptyCreatingRecord() = CreatingRecord("", 0, Date())
    }

    private val clearEvents = PublishSubject.create<Unit>()

    fun getDraft(): Single<CreatingRecord> = draftRepo.getDraft()
            .doOnEvent { t1, t2 -> LogUtils.d("DraftInteractor draftRepo.getDraft() $t1 $t2") }
            .onErrorReturnItem(createEmptyCreatingRecord())

    fun onDraftChanged(creatingRecord: CreatingRecord): Completable = draftRepo.saveDraft(creatingRecord)

    fun createRecord(creatingRecord: CreatingRecord): Completable {
        if (!isValid(creatingRecord)) return Completable.error(IllegalArgumentException())
        clearEvents.onNext(Unit)
        return draftRepo.removeDraft()
                .concatWith(recordsRepo.addRecord(creatingRecord).toCompletable())
    }

    fun isValid(creatingRecord: CreatingRecord) = creatingRecord.kind.isNotBlank() && creatingRecord.value > 0

    fun clearEvents(): Observable<Unit> = clearEvents
}