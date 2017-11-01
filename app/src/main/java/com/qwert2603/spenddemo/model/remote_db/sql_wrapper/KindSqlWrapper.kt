package com.qwert2603.spenddemo.model.remote_db.sql_wrapper

import com.qwert2603.spenddemo.model.entity.Kind
import java.sql.ResultSet

class KindSqlWrapper(resultSet: ResultSet) {
    val kind = Kind(resultSet.getString("kind"))
}