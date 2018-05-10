package com.qwert2603.spenddemo.model.remote_db

import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.spenddemo.utils.toUtilDate
import java.sql.ResultSet

class RemoteDBStub : RemoteDB {

    private var lastId = 10000L

    @Suppress("UNCHECKED_CAST")
    override fun <T> query(sql: String, mapper: (resultSet: ResultSet) -> T, args: List<Any>): List<T> {
        if ("INSERT" in sql) return listOf(Spend(
                id = lastId++,
                kind = args[0] as String,
                value = args[1] as Int,
                date = (args[2] as java.sql.Date).toUtilDate()
        ) as T) else throw RuntimeException("stub")
    }

    override fun execute(sql: String, args: List<Any>) {
        // nth.
    }
}