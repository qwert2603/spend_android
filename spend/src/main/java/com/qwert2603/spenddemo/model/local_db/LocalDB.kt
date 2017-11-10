package com.qwert2603.spenddemo.model.local_db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import com.qwert2603.spenddemo.model.local_db.converters.ChangeKindConverter
import com.qwert2603.spenddemo.model.local_db.converters.DateConverter
import com.qwert2603.spenddemo.model.local_db.dao.ChangesDao
import com.qwert2603.spenddemo.model.local_db.dao.KindsDao
import com.qwert2603.spenddemo.model.local_db.dao.RecordsDao
import com.qwert2603.spenddemo.model.local_db.tables.ChangeTable
import com.qwert2603.spenddemo.model.local_db.tables.RecordTable

@Database(version = 1, exportSchema = true, entities = arrayOf(RecordTable::class, ChangeTable::class))
@TypeConverters(DateConverter::class, ChangeKindConverter::class)
abstract class LocalDB : RoomDatabase() {
    abstract fun recordsDao(): RecordsDao
    abstract fun kindsDao(): KindsDao
    abstract fun changesDao(): ChangesDao
}