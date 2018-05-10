package com.qwert2603.spenddemo.model.remote_db.sql_wrapper

import com.qwert2603.spenddemo.model.syncprocessor.RemoteSpend
import java.sql.ResultSet

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