package com.qwert2603.spenddemo.draft

import com.qwert2603.spenddemo.model.entity.CreatingRecord
import com.qwert2603.spenddemo.model.entity.Kind
import com.qwert2603.spenddemo.model.repo.DraftRepo
import com.qwert2603.spenddemo.model.repo.KindsRepo
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.utils.mapList
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import java.util.*
import javax.inject.Inject

class DraftInteractor @Inject constructor(
        private val draftRepo: DraftRepo,
        private val recordsRepo: RecordsRepo,
        private val kindsRepo: KindsRepo
) {
    companion object {
        val EmptyCreatingRecord = CreatingRecord("", 0, Date())
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

    fun getMatchingKinds(inputKind: String): Single<List<String>> {
        if (inputKind.isBlank()) return Single.just(emptyList())
        return kindsRepo
                .getAllKinds()
                .firstOrError()
                .mapList { it.kind }
                .map {
                    val input = inputKind.toLowerCase()
                    it
                            .filter { input in it.toLowerCase() }
                            .sortedBy { it.toLowerCase().indexOf(input) }
                }
                .map { if (it.isNotEmpty()) it else listOf("don't know") }
    }

    fun kindSelected(): Observable<Kind> = draftRepo.kindSelected
            .withLatestFrom(kindsRepo.getAllKinds(), BiFunction { selectedKind: String, kinds: List<Kind> ->
                kinds.find { it.kind == selectedKind } ?: Kind(selectedKind, 0, Date(0))
            })

    fun getAllKinds(): Observable<List<Kind>> = kindsRepo.getAllKinds()
}