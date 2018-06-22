package com.qwert2603.spenddemo.model.local_db.converters

import android.arch.persistence.room.TypeConverter
import com.qwert2603.spenddemo.model.entity.ChangeKind

class ChangeKindConverter {
    @TypeConverter
    fun toChangeKind(index: Int?) = index?.let { ChangeKind.values()[it] }

    @TypeConverter
    fun toIndex(changeKind: ChangeKind?) = changeKind?.ordinal
}