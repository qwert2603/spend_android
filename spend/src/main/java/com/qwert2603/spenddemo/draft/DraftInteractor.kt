package com.qwert2603.spenddemo.draft

import com.qwert2603.spenddemo.model.entity.CreatingRecord
import com.qwert2603.spenddemo.model.repo.DraftRepo
import com.qwert2603.spenddemo.model.repo.KindsRepo
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class DraftInteractor @Inject constructor(
        private val draftRepo: DraftRepo,
        private val recordsRepo: RecordsRepo,
        private val kindsRepo: KindsRepo
) {

    fun getDraft(): Single<CreatingRecord> = draftRepo.getDraft()

    fun saveDraft(creatingRecord: CreatingRecord): Completable = draftRepo.saveDraft(creatingRecord)

    fun createRecord(creatingRecord: CreatingRecord): Completable {
        if (!isCreatable(creatingRecord)) return Completable.error(IllegalArgumentException())
        return Completable
                .fromAction { recordsRepo.addRecord(creatingRecord) }
                .concatWith(draftRepo.removeDraft())
    }

    fun isCreatable(creatingRecord: CreatingRecord) = creatingRecord.kind.isNotBlank() && creatingRecord.value > 0

    fun getSuggestions(inputKind: String): Single<List<String>> = kindsRepo.getKindSuggestions(inputKind)

    fun getLastPriceOfKind(kind: String): Single<Int> = kindsRepo.getKind(kind)
            .map { it.lastPrice }
            .onErrorReturnItem(0)
}