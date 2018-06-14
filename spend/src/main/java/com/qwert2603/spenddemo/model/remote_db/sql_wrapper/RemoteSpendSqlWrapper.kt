package com.qwert2603.spenddemo.model.remote_db.sql_wrapper

import java.sql.ResultSet
import java.util.*

class RemoteSpendSqlWrapper(resultSet: ResultSet) {
    val spend = RemoteSpend(
            resultSet.getLong("id"),
            resultSet.getString("kind"),
            resultSet.getInt("value"),
            resultSet.getDate("date"),
            resultSet.getTimestamp("updated").time,
            resultSet.getBoolean("deleted")
    )
}

data class RemoteSpend(
        val id: Long,
        val kind: String,
        val value: Int,
        val date: Date,
        val updated: Long,
        val deleted: Boolean
)