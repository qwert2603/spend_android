package com.qwert2603.spenddemo.spend_draft

import com.qwert2603.spenddemo.model.entity.CreatingSpend
import com.qwert2603.spenddemo.model.repo.SpendDraftRepo
import com.qwert2603.spenddemo.model.repo.SpendKindsRepo
import com.qwert2603.spenddemo.model.repo.SpendsRepo
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class DraftInteractor @Inject constructor(
        private val spendDraftRepo: SpendDraftRepo,
        private val spendsRepo: SpendsRepo,
        private val spendKindsRepo: SpendKindsRepo
) {

    fun getDraft(): Single<CreatingSpend> = spendDraftRepo.getDraft()

    fun saveDraft(creatingSpend: CreatingSpend): Completable = spendDraftRepo.saveDraft(creatingSpend)

    fun createSpend(creatingSpend: CreatingSpend): Completable {
        if (!isCreatable(creatingSpend)) return Completable.error(IllegalArgumentException())
        return Completable
                .fromAction { spendsRepo.addSpend(creatingSpend) }
                .concatWith(spendDraftRepo.removeDraft())
    }

    fun isCreatable(creatingSpend: CreatingSpend) = creatingSpend.kind.isNotBlank() && creatingSpend.value > 0

    fun getSuggestions(inputKind: String): Single<List<String>> = spendKindsRepo.getKindSuggestions(inputKind)

    fun getLastPriceOfKind(kind: String): Single<Int> = spendKindsRepo.getKind(kind)
            .map { it.lastPrice }
            .onErrorReturnItem(0)
}