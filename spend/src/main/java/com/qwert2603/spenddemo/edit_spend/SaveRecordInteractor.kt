package com.qwert2603.spenddemo.edit_spend

import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.RecordDraft
import com.qwert2603.spenddemo.model.repo.RecordsDraftsRepo
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.utils.Const
import com.qwert2603.spenddemo.utils.Wrapper
import io.reactivex.Observable
import javax.inject.Inject

class SaveRecordInteractor @Inject constructor(
        private val recordsRepo: RecordsRepo,
        private val recordsDraftsRepo: RecordsDraftsRepo
) {
    fun getRecordChanges(uuid: String): Observable<Wrapper<Record>> = recordsRepo.getRecord(uuid)

    fun saveRecord(recordDraft: RecordDraft) {
        recordsRepo.saveRecords(listOf(recordDraft))
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
}