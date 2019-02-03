package com.qwert2603.spend.model.local_db.entity

import com.qwert2603.spend.model.local_db.tables.RecordCategoryTable
import com.qwert2603.spend.model.local_db.tables.RecordTable

data class ChangesFromServer(
        val updatedCategories: List<RecordCategoryTable>,
        val updatedRecords: List<RecordTable>, // all changes == null
        val deletedRecordsUuid: List<String>
)