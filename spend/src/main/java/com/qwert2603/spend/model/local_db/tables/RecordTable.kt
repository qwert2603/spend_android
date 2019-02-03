package com.qwert2603.spend.model.local_db.tables

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.qwert2603.spend.model.entity.RecordChange

@Entity(
        indices = [
            Index("uuid", unique = true),
            Index("date"),
            Index("time"),
            Index("recordCategoryUuid"),
            Index("kind"),
            Index("change_id", unique = true),
            Index("change_isDelete")
        ]
)
data class RecordTable(
        @PrimaryKey val uuid: String,
        val recordCategoryUuid: String,
        val date: Int,
        val time: Int?,
        val kind: String,
        val value: Int,
        @Embedded(prefix = "change_") val change: RecordChange?
)