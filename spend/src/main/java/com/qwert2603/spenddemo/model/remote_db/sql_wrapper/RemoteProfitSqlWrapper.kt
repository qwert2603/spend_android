package com.qwert2603.spenddemo.model.remote_db.sql_wrapper

import com.qwert2603.spenddemo.model.entity.Profit
import com.qwert2603.spenddemo.model.sync_processor.RemoteItem
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.*

class RemoteProfitSqlWrapper(resultSet: ResultSet) {
    val profit = RemoteProfit(
            id = resultSet.getLong("id"),
            kind = resultSet.getString("kind"),
            value = resultSet.getInt("value"),
            date = resultSet.getTimestamp("date"),
            updated = resultSet.getTimestamp("updated"),
            deleted = resultSet.getBoolean("deleted")
    )
}

data class RemoteProfit(
        override val id: Long,
        val kind: String,
        val value: Int,
        val date: Date,
        override val updated: Timestamp,
        override val deleted: Boolean
) : RemoteItem

fun RemoteProfit.toProfit() = Profit(
        id = id,
        kind = kind,
        value = value,
        date = date
)