package com.qwert2603.spenddemo.model.remote_db

import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.model.entity.ServerInfo
import io.reactivex.Observable
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class RemoteDBImpl(serverInfoChanges: Observable<ServerInfo>) : RemoteDB {

    companion object {
        var IMITATE_DELAY = false
        private const val IMITATED_SERVER_DELAY = 3000L
    }

    private lateinit var serverInfo: ServerInfo

    private val changeConnectionLock = Any()

    private var connection: Connection? = null
        set(value) {
            synchronized(changeConnectionLock) {
                field = value
                preparedStatements.clear()
            }
        }

    private val preparedStatements = ConcurrentHashMap<String, PreparedStatement>()

    init {
        Class.forName(org.postgresql.Driver::class.java.name)
        serverInfoChanges.subscribe {
            serverInfo = it
            connection = null
        }
    }

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

    @Synchronized
    private fun getPreparedStatement(sql: String): PreparedStatement {
        val connection = connection
                ?: DriverManager
                        .getConnection(serverInfo.url, serverInfo.user, serverInfo.password)
                        .also { connection = it }

        return preparedStatements[sql]
                ?: connection.prepareStatement(sql)
                        .also { preparedStatements[sql] = it }
    }

    private fun <T> sendRequest(uuid: UUID, request: () -> T): T {
        try {
            if (IMITATE_DELAY) Thread.sleep(IMITATED_SERVER_DELAY)
            return request()
        } catch (e: Exception) {
            LogUtils.d("RemoteDBImpl", "$uuid <<-- error ${e.message}")
            connection = null
            throw e
        }
    }

    private fun PreparedStatement.setArgs(args: List<Any>) {
        args.forEachIndexed { i, any ->
            val index = i + 1
            when (any) {
                is NullSqlArg -> setNull(index, any.type)
                is Int -> setInt(index, any)
                is Long -> setLong(index, any)
                is Double -> setDouble(index, any)
                is String -> setString(index, any)
                is java.sql.Date -> setDate(index, any)
                is java.sql.Time -> setTime(index, any)
                is java.sql.Timestamp -> setTimestamp(index, any)
                is Boolean -> setBoolean(index, any)
                else -> LogUtils.e("RemoteDBImpl unknown arg ${any.javaClass} $any")
            }
        }
    }
}