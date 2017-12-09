package com.qwert2603.spenddemo

import com.qwert2603.spenddemo.model.entity.CreatingRecord
import com.qwert2603.spenddemo.model.remote_db.RemoteDBFacade
import com.qwert2603.spenddemo.model.remote_db.RemoteDBImpl
import org.junit.Test
import java.io.BufferedReader
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.*

class T2 {
    @Test
    fun t() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val remoteDBFacade = RemoteDBFacade(RemoteDBImpl(
                "jdbc:postgresql://192.168.1.26:5432/spend",
                "postgres",
                "1234"
        ), "spend")

        val bufferedReader = BufferedReader(FileReader("/home/alex/android_projects/SpendDemo/spend/src/test/t.txt"))
        bufferedReader.lines().forEach {
            val split = it.split(" ")

            val creatingRecord = CreatingRecord(
                    kind = it.dropWhile { it != ' ' }.drop(1).dropLastWhile { it != ' ' }.dropLast(1),
                    value = split.last().toInt(),
                    date = dateFormat.parse(split[0]),
                    dateSet = true
            )
//            println(creatingRecord)
            remoteDBFacade.insertRecord(creatingRecord)
        }

    }
}