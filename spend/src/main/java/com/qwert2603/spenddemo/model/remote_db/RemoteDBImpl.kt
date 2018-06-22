package com.qwert2603.spenddemo.model.remote_db

import com.qwert2603.andrlib.util.LogUtils
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.*

class RemoteDBImpl(
        private val url: String,//todo: from user's settings.
        private val user: String,
        private val password: String
) : RemoteDB {

    companion object {
        var IMITATE_DELAY = false
        private const val IMITATED_SERVER_DELAY = 3000L
    }

    init {
        Class.forName(org.postgresql.Driver::class.java.name)
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
//        return DriverManager.getConnection(url, user, password).prepareStatement(sql)

        val connection = connection ?: DriverManager.getConnection(url, user, password)
        DriverManager.setLoginTimeout(3)// todo: check
        val preparedStatement = preparedStatements[sql] ?: connection.prepareStatement(sql)
        preparedStatement.queryTimeout = 4 // todo: check
        this.connection = connection
        preparedStatements[sql] = preparedStatement
        return preparedStatement
    }

    private fun <T> sendRequest(uuid: UUID, request: () -> T): T {
        try {
            if (IMITATE_DELAY) Thread.sleep(IMITATED_SERVER_DELAY)
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
                is Int -> setInt(index, any)
                is Long -> setLong(index, any)
                is String -> setString(index, any)
                is java.sql.Date -> setDate(index, any)
                is java.sql.Timestamp -> setTimestamp(index, any)
                is Boolean -> setBoolean(index, any)
                else -> LogUtils.e("RemoteDBImpl unknown arg ${any.javaClass} $any")
            }
        }
    }
}