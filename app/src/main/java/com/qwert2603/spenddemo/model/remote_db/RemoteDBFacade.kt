package com.qwert2603.spenddemo.model.remote_db

import com.qwert2603.spenddemo.BuildConfig
import com.qwert2603.spenddemo.model.entity.CreatingRecord
import com.qwert2603.spenddemo.model.entity.Kind
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.remote_db.sql_wrapper.IdSqlWrapper
import com.qwert2603.spenddemo.model.remote_db.sql_wrapper.KindSqlWrapper
import com.qwert2603.spenddemo.utils.toSqlDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDBFacade @Inject constructor(private val remoteDB: RemoteDB) {

    fun getAllKinds(): List<Kind> = remoteDB.query(
            "SELECT kind FROM ${BuildConfig.REMOTE_TABLE_NAME} GROUP BY kind ORDER BY count(*) DESC",
            { KindSqlWrapper(it).kind }
    )

    fun insertRecord(creatingRecord: CreatingRecord): Long = remoteDB.query(
            "INSERT INTO ${BuildConfig.REMOTE_TABLE_NAME} (kind, value, date) VALUES (?, ?, ?) returning id",
            { IdSqlWrapper(it).id },
            listOf(creatingRecord.kind, creatingRecord.value, creatingRecord.date.toSqlDate())
    ).single()

    fun updateRecord(record: Record) {
        remoteDB.execute(
                "UPDATE ${BuildConfig.REMOTE_TABLE_NAME} SET kind=?, value=?, date=? WHERE id=?",
                listOf(record.kind, record.value, record.date.toSqlDate(), record.id)
        )
    }

    fun deleteRecord(recordId: Long) {
        remoteDB.execute(
                "DELETE FROM ${BuildConfig.REMOTE_TABLE_NAME} WHERE id = ?",
                listOf(recordId)
        )
    }
}