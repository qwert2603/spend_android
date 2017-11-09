package com.qwert2603.spenddemo.model.remote_db

import com.qwert2603.spenddemo.utils.LogUtils
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.*

class RemoteDBImpl(
        private val url: String,
        private val user: String,
        private val password: String
) : RemoteDB {
    init {
        try {
            Class.forName(org.postgresql.Driver::class.java.name)
        } catch (e: ClassNotFoundException) {
            LogUtils.e("postgresql", e)
        }
    }

    private var connection: Connection? = null
    private val preparedStatements = mutableMapOf<String, PreparedStatement>()

    override fun <T> query(sql: String, mapper: (resultSet: ResultSet) -> T, args: List<Any>): List<T> {
        val uuid = UUID.randomUUID()
        return sendRequest(uuid) {
            LogUtils.d("RemoteDBImpl", "$uuid -->> $sql $args")
            getPreparedStatement(sql)
                    .also { it.setArgs(args) }
                    .executeQuery()
                    .let {
                        val list = mutableListOf<T>()
                        while (it.next()) list += mapper(it)
                        list
                    }
                    .also { LogUtils.d("RemoteDBImpl", "$uuid <<-- $sql $args $it") }
        }
    }

    override fun execute(sql: String, args: List<Any>) {
        val uuid = UUID.randomUUID()
        sendRequest(uuid) {
            LogUtils.d("RemoteDBImpl", "$uuid -->> $sql $args")
            getPreparedStatement(sql)
                    .also { it.setArgs(args) }
                    .execute()
            LogUtils.d("RemoteDBImpl", "$uuid <<-- $sql $args")
        }
    }

    private fun getPreparedStatement(sql: String): PreparedStatement {
        val connection = connection ?: DriverManager.getConnection(url, user, password)
        val preparedStatement = preparedStatements[sql] ?: connection.prepareStatement(sql)
        preparedStatement.queryTimeout = 4 // todo: check
        this.connection = connection
        preparedStatements.put(sql, preparedStatement)
        return preparedStatement
    }

    private fun <T> sendRequest(uuid: UUID, request: () -> T): T {
        try {
            return request()
        } catch (e: Exception) {
            LogUtils.d("RemoteDBImpl", "$uuid <<-- error ${e.message}")
            connection = null
            preparedStatements.clear()
            throw e
        }
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