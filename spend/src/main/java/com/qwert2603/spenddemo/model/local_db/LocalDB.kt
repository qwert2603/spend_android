package com.qwert2603.spenddemo.model.local_db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import com.qwert2603.spenddemo.model.local_db.converters.ChangeKindConverter
import com.qwert2603.spenddemo.model.local_db.converters.DateConverter
import com.qwert2603.spenddemo.model.local_db.dao.ChangesDao
import com.qwert2603.spenddemo.model.local_db.dao.ProfitsDao
import com.qwert2603.spenddemo.model.local_db.dao.SpendKindsDao
import com.qwert2603.spenddemo.model.local_db.dao.SpendsDao
import com.qwert2603.spenddemo.model.local_db.tables.ChangeTable
import com.qwert2603.spenddemo.model.local_db.tables.ProfitTable
import com.qwert2603.spenddemo.model.local_db.tables.SpendTable

@Database(version = 4, exportSchema = true, entities = [SpendTable::class, ChangeTable::class, ProfitTable::class])
@TypeConverters(DateConverter::class, ChangeKindConverter::class)
abstract class LocalDB : RoomDatabase() {
    abstract fun spendsDao(): SpendsDao
    abstract fun kindsDao(): SpendKindsDao
    abstract fun changesDao(): ChangesDao
    abstract fun profitsDao(): ProfitsDao
}