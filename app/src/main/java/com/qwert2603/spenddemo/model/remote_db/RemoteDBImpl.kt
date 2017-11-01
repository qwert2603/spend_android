package com.qwert2603.spenddemo.model.remote_db

import com.qwert2603.spenddemo.utils.LogUtils
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

class RemoteDBImpl(
        private val url: String,
        private val user: String,
        private val password: String
) : RemoteDB {
    override fun <T> query(sql: String, mapper: (resultSet: ResultSet) -> T, args: List<Any>): List<T> {
        LogUtils.d("RemoteDBImpl", "query calling $sql $args")
        return DriverManager
                .getConnection(url, user, password)
                .prepareStatement(sql)
                .also { it.setArgs(args) }
                .executeQuery()
                .let {
                    val list = mutableListOf<T>()
                    while (it.next()) {
                        list += mapper(it)
                    }
                    list
                }
                .also { LogUtils.d("RemoteDBImpl", "query $sql $args $it") }
    }

    override fun execute(sql: String, args: List<Any>) {
        LogUtils.d("RemoteDBImpl", "execute calling $sql $args")
        DriverManager
                .getConnection(url, user, password)
                .prepareStatement(sql)
                .also { it.setArgs(args) }
                .execute()
        LogUtils.d("RemoteDBImpl", "execute $sql $args")
    }

    private fun PreparedStatement.setArgs(args: List<Any>) {
        args.forEachIndexed { i, any ->
            val index = i + 1
            when (any) {
                is Number -> setLong(index, any.toLong())
                is String -> setString(index, any)
                is java.sql.Date -> setDate(index, any)
                else -> LogUtils.e("RemoteDBImpl unknown arg ${any.javaClass} $any")
            }
        }
    }
}