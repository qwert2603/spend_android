package com.qwert2603.spend.change_records

import com.qwert2603.spend.model.entity.SDate
import com.qwert2603.spend.model.entity.STime
import com.qwert2603.spend.model.repo.RecordsRepo
import com.qwert2603.spend.utils.Wrapper

class ChangeRecordsInteractor(
        private val recordsRepo: RecordsRepo
) {
    fun changeRecords(
            recordsUuids: List<String>,
            changedDate: SDate?,
            changedTime: Wrapper<STime>?
    ) = recordsRepo.changeRecords(recordsUuids, changedDate, changedTime)
}