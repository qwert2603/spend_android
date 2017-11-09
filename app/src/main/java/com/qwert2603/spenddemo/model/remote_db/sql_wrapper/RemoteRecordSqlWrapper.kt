package com.qwert2603.spenddemo.model.remote_db.sql_wrapper

import com.qwert2603.spenddemo.model.syncprocessor.RemoteRecord
import java.sql.ResultSet

class RemoteRecordSqlWrapper(resultSet: ResultSet) {
    val record = RemoteRecord(
            resultSet.getLong("id"),
            resultSet.getString("kind"),
            resultSet.getInt("value"),
            resultSet.getDate("date"),
            resultSet.getTimestamp("updated").time,
            resultSet.getBoolean("deleted")
    )
}