package com.qwert2603.spenddemo.model.remote_db.sql_wrapper

import com.qwert2603.spenddemo.model.local_db.tables.SpendTable
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.*

class RemoteSpendSqlWrapper(resultSet: ResultSet) {
    val spend = RemoteSpend(
            resultSet.getLong("id"),
            resultSet.getString("kind"),
            resultSet.getInt("value"),
            resultSet.getTimestamp("date"),
            resultSet.getTimestamp("updated"),
            resultSet.getBoolean("deleted")
    )
}

data class RemoteSpend(
        val id: Long,
        val kind: String,
        val value: Int,
        val date: Date,
        val updated: Timestamp,
        val deleted: Boolean
)

fun RemoteSpend.toSpendTable() = SpendTable(
        id = id,
        kind = kind,
        value = value,
        date = date,
        change = null
)