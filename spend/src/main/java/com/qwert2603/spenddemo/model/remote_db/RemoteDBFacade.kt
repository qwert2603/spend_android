package com.qwert2603.spenddemo.model.remote_db

import com.qwert2603.spenddemo.di.qualifiers.RemoteTableName
import com.qwert2603.spenddemo.model.entity.CreatingRecord
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.remote_db.sql_wrapper.IdSqlWrapper
import com.qwert2603.spenddemo.model.remote_db.sql_wrapper.RemoteRecordSqlWrapper
import com.qwert2603.spenddemo.model.syncprocessor.RemoteRecord
import com.qwert2603.spenddemo.utils.toSqlDate
import java.sql.Date
import java.sql.Timestamp
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDBFacade @Inject constructor(
        private val remoteDB: RemoteDB,
        @RemoteTableName private val REMOTE_TABLE_NAME: String
) {
    fun getAllRecords(lastUpdateMillis: Long = 0): List<RemoteRecord> = remoteDB.query(
            "SELECT * FROM $REMOTE_TABLE_NAME WHERE updated > ? ORDER BY updated DESC",
            { RemoteRecordSqlWrapper(it).record },
            listOf(Timestamp(lastUpdateMillis))
    )

    fun insertRecord(creatingRecord: CreatingRecord): Long = remoteDB.query(
            "INSERT INTO $REMOTE_TABLE_NAME (kind, value, date) VALUES (?, ?, ?) returning id",
            { IdSqlWrapper(it).id },
            listOf(creatingRecord.kind, creatingRecord.value, creatingRecord.date.toSqlDate())
    ).single()

    fun updateRecord(record: Record) {
        remoteDB.execute(
                "UPDATE $REMOTE_TABLE_NAME SET kind=?, value=?, date=?, updated=NOW() WHERE id=?",
                listOf(record.kind, record.value, record.date.toSqlDate(), record.id)
        )
    }

    fun deleteRecord(recordId: Long) {
        remoteDB.execute(
                "UPDATE $REMOTE_TABLE_NAME SET deleted=TRUE, updated=NOW() WHERE id = ?",
                listOf(recordId)
        )
    }

    fun isDBAvailable(): Boolean {
        return try {
            remoteDB.execute("SELECT id FROM $REMOTE_TABLE_NAME WHERE id = 1918")
            true
        } catch (e: Exception) {
            false
        }
    }

    fun insertMother(kind: String, subkind: String, value: Double, date: Date) {
        remoteDB.execute(
                "INSERT INTO mother (kind, subkind, value, date) VALUES (?, ?, ?, ?)",
                listOf(kind, subkind, value, date)
        )
    }
}