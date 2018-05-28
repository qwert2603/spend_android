package com.qwert2603.spenddemo.model.remote_db

import com.qwert2603.spenddemo.env.E
import com.qwert2603.spenddemo.model.entity.CreatingSpend
import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.spenddemo.model.remote_db.sql_wrapper.IdSqlWrapper
import com.qwert2603.spenddemo.model.remote_db.sql_wrapper.RemoteSpend
import com.qwert2603.spenddemo.model.remote_db.sql_wrapper.RemoteSpendSqlWrapper
import com.qwert2603.spenddemo.utils.toSqlDate
import java.sql.Date
import java.sql.Timestamp
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDBFacade @Inject constructor(
        private val remoteDB: RemoteDB
) {
    fun getAllSpends(lastUpdateMillis: Long = 0, lastUpdatedId: Long = 0): List<RemoteSpend> = remoteDB.query(
            "SELECT * FROM ${E.env.remoteTableName} WHERE updated >= ? AND id > ? ORDER BY updated DESC",
            { RemoteSpendSqlWrapper(it).spend },
            listOf(Timestamp(lastUpdateMillis), lastUpdatedId)
    )

    fun insertSpend(creatingSpend: CreatingSpend): Long = remoteDB.query(
            "INSERT INTO ${E.env.remoteTableName} (kind, value, date) VALUES (?, ?, ?) returning id",
            { IdSqlWrapper(it).id },
            listOf(creatingSpend.kind, creatingSpend.value, creatingSpend.getDateNN().toSqlDate())
    ).single()

    fun updateSpend(spend: Spend) {
        remoteDB.execute(
                "UPDATE ${E.env.remoteTableName} SET kind=?, value=?, date=?, updated=NOW() WHERE id=?",
                listOf(spend.kind, spend.value, spend.date.toSqlDate(), spend.id)
        )
    }

    fun deleteSpend(spendId: Long) {
        remoteDB.execute(
                "UPDATE ${E.env.remoteTableName} SET deleted=TRUE, updated=NOW() WHERE id = ?",
                listOf(spendId)
        )
    }

    fun isDBAvailable(): Boolean {
        return try {
            remoteDB.execute("SELECT id FROM ${E.env.remoteTableName} WHERE id = 1918")
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