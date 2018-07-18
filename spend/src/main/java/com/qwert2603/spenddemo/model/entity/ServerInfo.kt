package com.qwert2603.spenddemo.model.entity

data class ServerInfo(
        val url: String,
        val user: String,
        val password: String
) {
    companion object {
        val DEFAULT = ServerInfo(
                url = "jdbc:postgresql://192.168.1.26:5432/spend",
                user = "postgres",
                password = "1234"
        )
    }
}