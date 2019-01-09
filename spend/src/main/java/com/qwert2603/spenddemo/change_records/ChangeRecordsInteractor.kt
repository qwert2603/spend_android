package com.qwert2603.spenddemo.change_records

import com.qwert2603.spenddemo.model.entity.SDate
import com.qwert2603.spenddemo.model.entity.STime
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.utils.Wrapper
import javax.inject.Inject

class ChangeRecordsInteractor @Inject constructor(
        private val recordsRepo: RecordsRepo
) {
    fun changeRecords(
            recordsUuids: List<String>,
            changedDate: SDate?,
            changedTime: Wrapper<STime>?
    ) = recordsRepo.changeRecords(recordsUuids, changedDate, changedTime)
}