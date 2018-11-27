package com.qwert2603.spenddemo.save_spend

import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.RecordDraft
import com.qwert2603.spenddemo.model.repo.RecordKindsRepo
import com.qwert2603.spenddemo.model.repo.RecordsDraftsRepo
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.utils.Const
import com.qwert2603.spenddemo.utils.Wrapper
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class SaveRecordInteractor @Inject constructor(
        private val recordsRepo: RecordsRepo,
        private val recordsDraftsRepo: RecordsDraftsRepo,
        private val recordKindsRepo: RecordKindsRepo
) {
    fun getRecordChanges(uuid: String): Observable<Wrapper<Record>> = recordsRepo.getRecord(uuid)

    fun saveRecord(recordDraft: RecordDraft) {
        recordsRepo.saveRecords(listOf(recordDraft))
        when (recordDraft.recordTypeId) {
            Const.RECORD_TYPE_ID_SPEND -> recordsDraftsRepo.spendDraft = null
            Const.RECORD_TYPE_ID_PROFIT -> recordsDraftsRepo.profitDraft = null
            else -> null!!
        }.also { }
    }

    fun getDraft(recordTypeId: Long): RecordDraft? = when (recordTypeId) {
        Const.RECORD_TYPE_ID_SPEND -> recordsDraftsRepo.spendDraft
        Const.RECORD_TYPE_ID_PROFIT -> recordsDraftsRepo.profitDraft
        else -> null!!
    }

    fun saveDraft(recordTypeId: Long, recordDraft: RecordDraft) {
        when (recordTypeId) {
            Const.RECORD_TYPE_ID_SPEND -> recordsDraftsRepo.spendDraft = recordDraft
            Const.RECORD_TYPE_ID_PROFIT -> recordsDraftsRepo.profitDraft = recordDraft
            else -> null!!
        }.also { }
    }

    fun getSuggestions(recordTypeId: Long, inputKind: String): Single<List<String>> = recordKindsRepo
            .getKindSuggestions(recordTypeId, inputKind, 5)

    fun getLastValueOfKind(recordTypeId: Long, kind: String): Single<Int> = recordKindsRepo
            .getRecordKind(recordTypeId, kind)
            .firstOrError()
            .map { it.t?.lastValue ?: 0 }
}