package com.qwert2603.spend.model.local_db.results

import com.qwert2603.spend.model.entity.SDate
import com.qwert2603.spend.model.entity.STime
import com.qwert2603.spend.model.local_db.tables.RecordCategoryTable
import com.qwert2603.spend.model.local_db.tables.RecordTable

data class Dump(
        val sDate: SDate,
        val sTime: STime,
        val recordCategories: List<RecordCategoryTable>,
        val records: List<RecordTable>
)