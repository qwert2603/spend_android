package com.qwert2603.spend.model.local_db.tables

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.qwert2603.spend.model.entity.RecordCategory

@Entity(indices = [
    Index("uuid", unique = true),
    Index("recordTypeId", "name", unique = true)
])
class RecordCategoryTable(
        @PrimaryKey val uuid: String,
        val recordTypeId: Long,
        val name: String
)

fun RecordCategoryTable.toRecordCategory() = RecordCategory(
        uuid = uuid,
        recordTypeId = recordTypeId,
        name = name
)