package com.qwert2603.spenddemo.model.remote_db

import com.qwert2603.spenddemo.env.E
import com.qwert2603.spenddemo.model.entity.CreatingSpend
import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.spenddemo.model.remote_db.sql_wrapper.IdSqlWrapper
import com.qwert2603.spenddemo.model.remote_db.sql_wrapper.RemoteSpend
import com.qwert2603.spenddemo.model.remote_db.sql_wrapper.RemoteSpendSqlWrapper
import com.qwert2603.spenddemo.utils.toSqlTimestamp
import java.sql.Date
import java.sql.Timestamp
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDBFacade @Inject constructor(
        private val remoteDB: RemoteDB
) {
    fun getSpends(lastUpdateMillis: Timestamp = Timestamp(0), lastUpdatedId: Long = 0, limit: Int = 20): List<RemoteSpend> = remoteDB.query(
            "SELECT id, kind, value, date, updated, deleted" +
                    " FROM ${E.env.remoteTableName}" +
                    " WHERE updated > ? OR (updated = ? AND id > ?)" +
                    " ORDER BY updated, id" +
                    " LIMIT ?",
            { RemoteSpendSqlWrapper(it).spend },
            listOf(lastUpdateMillis, lastUpdateMillis, lastUpdatedId, limit)
    )

    fun insertSpend(creatingSpend: CreatingSpend): Long = remoteDB.query(
            "INSERT INTO ${E.env.remoteTableName} (kind, value, date) VALUES (?, ?, ?) returning id",
            { IdSqlWrapper(it).id },
            listOf(creatingSpend.kind, creatingSpend.value, creatingSpend.getDateNN().toSqlTimestamp())
    ).single()

    fun updateSpend(spend: Spend) {
        remoteDB.execute(
                "UPDATE ${E.env.remoteTableName} SET kind=?, value=?, date=?, updated=NOW() WHERE id=?",
                listOf(spend.kind, spend.value, spend.date.toSqlTimestamp(), spend.id)
        )
    }

    fun deleteSpend(spendId: Long) {
        remoteDB.execute(
                "UPDATE ${E.env.remoteTableName} SET deleted=TRUE, updated=NOW() WHERE id = ?",
                listOf(spendId)
        )
    }

    fun isDBAvailable(): Boolean = try {
        remoteDB.execute("SELECT id FROM ${E.env.remoteTableName} WHERE id = 1918")
        true
    } catch (e: Exception) {
        false
    }

    fun insertMother(kind: String, subkind: String, value: Double, date: Date, alex: Boolean) {
        remoteDB.execute(
                "INSERT INTO mother (kind, subkind, value, date, alex) VALUES (?, ?, ?, ?, ?)",
                listOf(kind, subkind, value, date, alex)
        )
    }
}