package com.qwert2603.spenddemo.draft

import com.qwert2603.andrlib.util.mapList
import com.qwert2603.spenddemo.model.entity.CreatingRecord
import com.qwert2603.spenddemo.model.entity.Kind
import com.qwert2603.spenddemo.model.repo.DraftRepo
import com.qwert2603.spenddemo.model.repo.KindsRepo
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.utils.onlyDate
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.*
import javax.inject.Inject

class DraftInteractor @Inject constructor(
        private val draftRepo: DraftRepo,
        private val recordsRepo: RecordsRepo,
        private val kindsRepo: KindsRepo
) {
    companion object {
        private val EMPTY_CREATING_RECORD = CreatingRecord("", 0, Date().onlyDate(), false)
    }

    fun getDraftB(): Single<CreatingRecord> = draftRepo.getDraft()

    fun saveDraftB(creatingRecord: CreatingRecord): Completable = draftRepo.saveDraft(creatingRecord)

    fun createRecordB(creatingRecord: CreatingRecord): Completable = Completable
            .fromAction { recordsRepo.addRecord(creatingRecord) }
            .concatWith(draftRepo.removeDraft())

    fun isCreatable(creatingRecord: CreatingRecord) = creatingRecord.kind.isNotBlank() && creatingRecord.value > 0

    fun getInitialSuggestionsB(): Single<List<String>> = kindsRepo.getKindSuggestions("")

    fun getSuggestionsB(inputKind: String): Single<List<String>> = kindsRepo.getKindSuggestions(inputKind)

    fun getLastPriceOfKind(kind: String): Single<Int> = kindsRepo.getKind(kind).map { it.lastPrice }






    private val draftChanges = BehaviorSubject.create<CreatingRecord>()
    private val kindSelected = PublishSubject.create<Any>()

    private var draft = EMPTY_CREATING_RECORD
    private val changeDraftLock = Any()

    private val allKinds: BehaviorSubject<List<Kind>> = kindsRepo.getAllKinds().subscribeWith(BehaviorSubject.create())

    private fun changeDraft(change: (CreatingRecord) -> CreatingRecord) {
        synchronized(changeDraftLock) {
            draft = change(draft)
            draftChanges.onNext(draft)
        }
    }

    init {
        draftRepo.getDraft()
                .subscribe { draft ->
                    changeDraft { _ ->
                        draft.copy(date = if (draft.dateSet) draft.date else Date().onlyDate())
                    }
                }
        draftChanges
                .skip(1) // skip loaded from draftRepo.getDraft()
                .switchMap { draftRepo.saveDraft(it).toObservable<Any>() }
                .subscribe()
    }

    fun getDraft(): Observable<CreatingRecord> = draftChanges
    fun kindSelected(): Observable<Any> = kindSelected

    fun onKindChanged(kind: String, fromSuggestion: Boolean) {
        if (fromSuggestion) {
            changeDraft {
                it.copy(
                        kind = kind,
                        value = allKinds.value?.find { it.kind == kind }?.lastPrice ?: it.value
                )
            }
            kindSelected.onNext(Any())
        } else {
            changeDraft { it.copy(kind = kind) }
        }
    }

    fun onValueChanged(value: Int) {
        changeDraft { it.copy(value = value) }
    }

    fun onDateChanged(date: Date) {
        changeDraft { it.copy(date = date, dateSet = true) }
    }

    fun createRecord() {
        val draft = draft
        if (!isValid(draft)) return
        changeDraft { EMPTY_CREATING_RECORD }
        recordsRepo.addRecord(draft)
    }

    fun isValid(creatingRecord: CreatingRecord) = creatingRecord.kind.isNotBlank() && creatingRecord.value > 0

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
}