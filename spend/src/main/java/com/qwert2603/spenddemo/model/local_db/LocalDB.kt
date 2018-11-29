package com.qwert2603.spenddemo.model.local_db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.qwert2603.spenddemo.model.local_db.dao.RecordsDao
import com.qwert2603.spenddemo.model.local_db.tables.RecordCategoryTable
import com.qwert2603.spenddemo.model.local_db.tables.RecordTable

@Database(version = 12, exportSchema = true, entities = [RecordTable::class, RecordCategoryTable::class])
abstract class LocalDB : RoomDatabase() {
    abstract fun recordsDao(): RecordsDao
}