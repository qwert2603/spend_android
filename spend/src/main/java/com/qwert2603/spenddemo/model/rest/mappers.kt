package com.qwert2603.spenddemo.model.rest

import com.qwert2603.spenddemo.model.entity.LastUpdateInfo
import com.qwert2603.spenddemo.model.entity.RecordChange
import com.qwert2603.spenddemo.model.local_db.tables.RecordTable
import com.qwert2603.spenddemo.model.rest.entity.GetRecordsUpdatesResult
import com.qwert2603.spenddemo.model.rest.entity.LastUpdateInfoServer
import com.qwert2603.spenddemo.model.rest.entity.RecordServer
import com.qwert2603.spenddemo.model.sync_processor.UpdatesFromRemote

fun RecordServer.toRecordTable(recordChange: RecordChange?) = RecordTable(
        uuid = uuid,
        recordTypeId = recordTypeId,
        date = date,
        time = time,
        kind = kind,
        value = value,
        change = recordChange
)

fun LastUpdateInfoServer.toLastUpdateInfo() = LastUpdateInfo(
        lastUpdated = lastUpdated,
        lastUuid = lastUuid
)

fun GetRecordsUpdatesResult.toUpdatesFromRemote() = UpdatesFromRemote(
        updatedItems = updatedRecords,
        deletedItemsUuid = deletedRecordsUuid,
        lastUpdateInfo = lastUpdateInfo.toLastUpdateInfo()
)