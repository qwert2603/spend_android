package com.qwert2603.spenddemo.model.remote_db

import com.qwert2603.spenddemo.di.qualifiers.RemoteTableName
import com.qwert2603.spenddemo.model.entity.CreatingSpend
import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.spenddemo.model.remote_db.sql_wrapper.IdSqlWrapper
import com.qwert2603.spenddemo.model.remote_db.sql_wrapper.RemoteSpendSqlWrapper
import com.qwert2603.spenddemo.model.syncprocessor.RemoteSpend
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
    fun getAllSpends(lastUpdateMillis: Long = 0): List<RemoteSpend> = remoteDB.query(
            "SELECT * FROM $REMOTE_TABLE_NAME WHERE updated > ? ORDER BY updated DESC",
            { RemoteSpendSqlWrapper(it).spend },
            listOf(Timestamp(lastUpdateMillis))
    )

    fun insertSpend(creatingSpend: CreatingSpend): Long = remoteDB.query(
            "INSERT INTO $REMOTE_TABLE_NAME (kind, value, date) VALUES (?, ?, ?) returning id",
            { IdSqlWrapper(it).id },
            listOf(creatingSpend.kind, creatingSpend.value, creatingSpend.getDateNN().toSqlDate())
    ).single()

    fun updateSpend(spend: Spend) {
        remoteDB.execute(
                "UPDATE $REMOTE_TABLE_NAME SET kind=?, value=?, date=?, updated=NOW() WHERE id=?",
                listOf(spend.kind, spend.value, spend.date.toSqlDate(), spend.id)
        )
    }

    fun deleteSpend(spendId: Long) {
        remoteDB.execute(
                "UPDATE $REMOTE_TABLE_NAME SET deleted=TRUE, updated=NOW() WHERE id = ?",
                listOf(spendId)
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

    fun insertMother(kind: String, subkind: String, value: Double, date: Date, alex: Boolean) {
        remoteDB.execute(
                "INSERT INTO mother (kind, subkind, value, date, alex) VALUES (?, ?, ?, ?, ?)",
                listOf(kind, subkind, value, date, alex)
        )
    }
}