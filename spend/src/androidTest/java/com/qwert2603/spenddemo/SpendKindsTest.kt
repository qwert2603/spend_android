package com.qwert2603.spenddemo

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.dao.SpendsDao
import com.qwert2603.spenddemo.model.local_db.tables.SpendKindTable
import com.qwert2603.spenddemo.model.local_db.tables.SpendTable
import com.qwert2603.spenddemo.utils.days
import com.qwert2603.spenddemo.utils.minus
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class SpendKindsTest {
    private lateinit var spendsDao: SpendsDao
    private lateinit var mDb: LocalDB

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getTargetContext()
        mDb = Room.inMemoryDatabaseBuilder(context, LocalDB::class.java).build()
        spendsDao = mDb.spendsDao()
    }

    @After
    fun closeDb() {
        mDb.close()
    }

    @Test
    fun t2() {
        val date1 = Date() - 2.days
        spendsDao.addSpend(SpendTable(1L, "kk", 14, date1))
        Assert.assertEquals(listOf(
                SpendKindTable("kk", date1, 14, 1)
        ), spendsDao.getAllKingsList())

        val date2 = Date()
        spendsDao.addSpend(SpendTable(2L, "kk", 17, date2))
        Assert.assertEquals(listOf(
                SpendKindTable("kk", date2, 17, 2)
        ), spendsDao.getAllKingsList())

        val date3 = Date() - 5.days
        spendsDao.addSpend(SpendTable(3L, "kk", 19, date3))
        Assert.assertEquals(listOf(
                SpendKindTable("kk", date2, 17, 3)
        ), spendsDao.getAllKingsList())

        spendsDao.editSpend(SpendTable(2L, "kk", 26, date2))
        Assert.assertEquals(listOf(
                SpendKindTable("kk", date2, 26, 3)
        ), spendsDao.getAllKingsList())

        spendsDao.editSpend(SpendTable(2L, "kk", 26, date2 - 10.days))
        Assert.assertEquals(listOf(
                SpendKindTable("kk", date1, 14, 3)
        ), spendsDao.getAllKingsList())

        spendsDao.deleteSpend(1L)
        Assert.assertEquals(listOf(
                SpendKindTable("kk", date3, 19, 2)
        ), spendsDao.getAllKingsList())

        val dateQ = Date()
        spendsDao.addSpend(SpendTable(4L, "qq", 37, dateQ))
        Assert.assertEquals(listOf(
                SpendKindTable("kk", date3, 19, 2),
                SpendKindTable("qq", dateQ, 37, 1)
        ), spendsDao.getAllKingsList())

        spendsDao.addSpend(SpendTable(5L, "qq", 12, Date() - 20.days))
        spendsDao.addSpend(SpendTable(6L, "qq", 11, Date() - 20.days))
        Assert.assertEquals(listOf(
                SpendKindTable("qq", dateQ, 37, 3),
                SpendKindTable("kk", date3, 19, 2)
        ), spendsDao.getAllKingsList())

        spendsDao.deleteSpend(2L)
        spendsDao.deleteSpend(3L)
        Assert.assertEquals(listOf(
                SpendKindTable("qq", dateQ, 37, 3)
        ), spendsDao.getAllKingsList())
    }
}