package com.qwert2603.spenddemo.model.sync_processor

import com.qwert2603.spenddemo.model.entity.RecordChange
import com.qwert2603.spenddemo.model.entity.RecordDraft
import com.qwert2603.spenddemo.model.local_db.entity.ChangesFromServer
import com.qwert2603.spenddemo.model.local_db.tables.RecordCategoryTable
import com.qwert2603.spenddemo.model.local_db.tables.RecordTable
import com.qwert2603.spenddemo.model.rest.entity.GetRecordsUpdatesResult
import com.qwert2603.spenddemo.model.rest.entity.RecordCategoryServer
import com.qwert2603.spenddemo.model.rest.entity.RecordServer

fun RecordDraft.toRecordTable(recordChange: RecordChange?): RecordTable {
    val (sDate, sTime) = this.dateTime()
    return RecordTable(
            uuid = uuid,
            recordCategoryUuid = recordCategoryUuid!!,
            date = sDate.date,
            time = sTime?.time,
            kind = kind,
            value = value,
            change = recordChange
    )
}

fun RecordTable.toRecordServer() = RecordServer(
        uuid = uuid,
        recordCategoryUuid = recordCategoryUuid,
        date = date,
        time = time,
        kind = kind,
        value = value
)

fun GetRecordsUpdatesResult.toChangesFromServer() = ChangesFromServer(
        updatedCategories = updatedCategories.map { it.toRecordCategoryTable() },
        updatedRecords = updatedRecords.map { it.toRecordTable(null) },
        deletedRecordsUuid = deletedRecordsUuid
)

private fun RecordServer.toRecordTable(recordChange: RecordChange?) = RecordTable(
        uuid = uuid,
        recordCategoryUuid = recordCategoryUuid,
        date = date,
        time = time,
        kind = kind,
        value = value,
        change = recordChange
)

private fun RecordCategoryServer.toRecordCategoryTable() = RecordCategoryTable(
        uuid = uuid,
        recordTypeId = recordTypeId,
        name = name
)