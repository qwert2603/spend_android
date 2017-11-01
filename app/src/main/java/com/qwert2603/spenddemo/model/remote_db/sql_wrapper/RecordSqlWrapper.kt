package com.qwert2603.spenddemo.model.remote_db.sql_wrapper

import com.qwert2603.spenddemo.model.entity.Record
import java.sql.ResultSet

class RecordSqlWrapper(resultSet: ResultSet) {
    val record = Record(
            resultSet.getLong("id"),
            resultSet.getString("kind"),
            resultSet.getInt("value"),
            resultSet.getDate("date")
    )
}