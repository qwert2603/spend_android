package com.qwert2603.spenddemo.spend_draft

import com.qwert2603.spenddemo.model.entity.RecordDraft
import com.qwert2603.spenddemo.model.repo.RecordKindsRepo
import com.qwert2603.spenddemo.model.repo.RecordsDraftsRepo
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.utils.Const
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class DraftInteractor @Inject constructor(
        private val recordsDraftsRepo: RecordsDraftsRepo,
        private val recordsRepo: RecordsRepo,
        private val recordKindsRepo: RecordKindsRepo
) {

    fun getDraft(): RecordDraft? = recordsDraftsRepo.spendDraft

    fun saveDraft(recordDraft: RecordDraft) {
        recordsDraftsRepo.spendDraft = recordDraft
    }

    fun createRecord(recordDraft: RecordDraft): Completable {
        if (!recordDraft.isValid()) return Completable.error(IllegalArgumentException())
        return Completable
                .fromAction { recordsRepo.saveRecords(listOf(recordDraft)) }
                .doOnComplete { recordsDraftsRepo.spendDraft = null }
    }

    fun getSuggestions(inputKind: String): Single<List<String>> = recordKindsRepo
            .getKindSuggestions(Const.RECORD_TYPE_ID_SPEND, inputKind, 5)

    fun getLastValueOfKind(kind: String): Single<Int> = recordKindsRepo
            .getRecordKind(Const.RECORD_TYPE_ID_SPEND, kind)
            .firstOrError()
            .map { it.t?.lastValue ?: 0 }
}