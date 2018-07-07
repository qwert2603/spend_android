package com.qwert2603.spenddemo.model.remote_db

import com.qwert2603.spenddemo.env.E
import com.qwert2603.spenddemo.model.entity.Profit
import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.spenddemo.model.remote_db.sql_wrapper.*
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
                    " FROM ${E.env.remoteTableNameSpends}" +
                    " WHERE updated > ? OR (updated = ? AND id > ?)" +
                    " ORDER BY updated, id" +
                    " LIMIT ?",
            { RemoteSpendSqlWrapper(it).spend },
            listOf(lastUpdateMillis, lastUpdateMillis, lastUpdatedId, limit)
    )

    // spend.id is ignored.
    fun insertSpend(spend: Spend): Long = remoteDB.query(
            "INSERT INTO ${E.env.remoteTableNameSpends} (kind, value, date) VALUES (?, ?, ?) returning id",
            { IdSqlWrapper(it).id },
            listOf(spend.kind, spend.value, spend.date.toSqlTimestamp())
    ).single()

    fun updateSpend(spend: Spend) {
        remoteDB.execute(
                "UPDATE ${E.env.remoteTableNameSpends} SET kind=?, value=?, date=?, updated=NOW() WHERE id=?",
                listOf(spend.kind, spend.value, spend.date.toSqlTimestamp(), spend.id)
        )
    }

    fun deleteSpend(spendId: Long) {
        remoteDB.execute(
                "UPDATE ${E.env.remoteTableNameSpends} SET deleted=TRUE, updated=NOW() WHERE id = ?",
                listOf(spendId)
        )
    }

    fun getProfits(lastUpdateMillis: Timestamp = Timestamp(0), lastUpdatedId: Long = 0, limit: Int = 20): List<RemoteProfit> = remoteDB.query(
            "SELECT id, kind, value, date, updated, deleted" +
                    " FROM ${E.env.remoteTableNameProfits}" +
                    " WHERE updated > ? OR (updated = ? AND id > ?)" +
                    " ORDER BY updated, id" +
                    " LIMIT ?",
            { RemoteProfitSqlWrapper(it).profit },
            listOf(lastUpdateMillis, lastUpdateMillis, lastUpdatedId, limit)
    )

    // profit.id is ignored.
    fun insertProfit(profit: Profit): Long = remoteDB.query(
            "INSERT INTO ${E.env.remoteTableNameProfits} (kind, value, date) VALUES (?, ?, ?) returning id",
            { IdSqlWrapper(it).id },
            listOf(profit.kind, profit.value, profit.date.toSqlTimestamp())
    ).single()

    fun updateProfit(profit: Profit) {
        remoteDB.execute(
                "UPDATE ${E.env.remoteTableNameProfits} SET kind=?, value=?, date=?, updated=NOW() WHERE id=?",
                listOf(profit.kind, profit.value, profit.date.toSqlTimestamp(), profit.id)
        )
    }

    fun deleteProfit(profitId: Long) {
        remoteDB.execute(
                "UPDATE ${E.env.remoteTableNameProfits} SET deleted=TRUE, updated=NOW() WHERE id = ?",
                listOf(profitId)
        )
    }


    fun insertMother(kind: String, subkind: String, value: Double, date: Date, alex: Boolean) {
        remoteDB.execute(
                "INSERT INTO mother (kind, subkind, value, date, alex) VALUES (?, ?, ?, ?, ?)",
                listOf(kind, subkind, value, date, alex)
        )
    }
}