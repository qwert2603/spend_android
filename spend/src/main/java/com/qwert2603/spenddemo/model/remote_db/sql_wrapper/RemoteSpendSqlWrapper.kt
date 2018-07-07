package com.qwert2603.spenddemo.model.remote_db.sql_wrapper

import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.spenddemo.model.sync_processor.RemoteItem
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
        override val id: Long,
        val kind: String,
        val value: Int,
        val date: Date,
        override val updated: Timestamp,
        override val deleted: Boolean
) : RemoteItem

fun RemoteSpend.toSpend() = Spend(
        id = id,
        kind = kind,
        value = value,
        date = date
)