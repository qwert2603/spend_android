package com.qwert2603.spenddemo.spend_draft

import com.qwert2603.spenddemo.model.entity.CreatingSpend
import com.qwert2603.spenddemo.model.repo.SpendDraftRepo
import com.qwert2603.spenddemo.model.repo.SpendKindsRepo
import com.qwert2603.spenddemo.model.repo.SpendsRepo
import com.qwert2603.spenddemo.model.repo.UserSettingsRepo
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class DraftInteractor @Inject constructor(
        private val spendDraftRepo: SpendDraftRepo,
        private val spendsRepo: SpendsRepo,
        private val spendKindsRepo: SpendKindsRepo,
        private val userSettingsRepo: UserSettingsRepo
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

    fun getSuggestions(inputKind: String): List<String> = spendKindsRepo.getKindSuggestions(inputKind)

    fun getLastPriceOfKind(kind: String): Int = spendKindsRepo.getKind(kind)?.lastPrice ?: 0

    fun showTimesChanges(): Observable<Boolean> = userSettingsRepo.showTimesChanges()
}