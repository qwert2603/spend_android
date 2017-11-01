package com.qwert2603.spenddemo.model.remote_db.sql_wrapper

import java.sql.ResultSet

class IdSqlWrapper(resultSet: ResultSet) {
    val id: Long = resultSet.getLong(1)
}