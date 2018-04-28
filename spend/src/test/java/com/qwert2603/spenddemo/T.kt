package com.qwert2603.spenddemo

import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.model.remote_db.RemoteDBFacade
import com.qwert2603.spenddemo.model.remote_db.RemoteDBImpl
import org.junit.Test
import java.io.BufferedReader
import java.io.FileReader
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*

class T {
    @Test
    fun t() {
        LogUtils.logType = LogUtils.LogType.SOUT

        val bufferedReader = BufferedReader(FileReader("/home/alex/StudioProjects/SpendDemo/spend/src/test/r.txt"))
        var s: String?
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val remoteDBFacade = RemoteDBFacade(RemoteDBImpl(
                "jdbc:postgresql://192.168.1.26:5432/spend",
                "postgres",
                "1234"
        ), "")
        var bb = false
        var date: Date? = null
        while (true) {
            s = bufferedReader.readLine()
            if (s == null) break

            val split = s.split("\t").toTypedArray()

            if (!bb) {
                bb = true
                split[0] = split[0].substring(1)
            }

            if (date == null || split[0].length > 2) {
                date = Date(dateFormat.parse(split[0]).time)
            }

            remoteDBFacade.insertMother(split[1], split[2], split[3].replace(",", ".").toDouble(), date)
        }
    }
}