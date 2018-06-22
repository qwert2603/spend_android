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
        // one spend
        val date1 = Date() - 2.days
        spendsDao.saveSpend(SpendTable(1L, "kk", 14, date1, null))
        assertKindsList(
                SpendKindTable("kk", date1, 14, 1)
        )

        // add spend with newer date
        val date2 = Date()
        spendsDao.saveSpend(SpendTable(2L, "kk", 17, date2, null))
        assertKindsList(
                SpendKindTable("kk", date2, 17, 2)
        )

        // add spend with older date
        val date3 = Date() - 5.days
        spendsDao.saveSpend(SpendTable(3L, "kk", 19, date3, null))
        assertKindsList(
                SpendKindTable("kk", date2, 17, 3)
        )

        // change newest spend's value
        spendsDao.saveSpend(SpendTable(2L, "kk", 26, date2, null))
        assertKindsList(
                SpendKindTable("kk", date2, 26, 3)
        )

        // change newest spend's date
        spendsDao.saveSpend(SpendTable(2L, "kk", 26, date2 - 10.days, null))
        assertKindsList(
                SpendKindTable("kk", date1, 14, 3)
        )

        // change newest spend's kind
        spendsDao.saveSpend(SpendTable(1L, "tt", 14, date1, null))
        assertKindsList(
                SpendKindTable("kk", date3, 19, 2),
                SpendKindTable("tt", date1, 14, 1)
        )

        // change kind back
        spendsDao.saveSpend(SpendTable(1L, "kk", 14, date1, null))
        assertKindsList(
                SpendKindTable("kk", date1, 14, 3)
        )

        // delete newest spend
        spendsDao.deleteSpend(1L)
        assertKindsList(
                SpendKindTable("kk", date3, 19, 2)
        )

        // add spend with another kind
        val dateQ = Date()
        spendsDao.saveSpend(SpendTable(4L, "qq", 37, dateQ, null))
        assertKindsList(
                SpendKindTable("kk", date3, 19, 2),
                SpendKindTable("qq", dateQ, 37, 1)
        )

        // add two more spends of another kind.
        // so another kind becomes above in list
        spendsDao.saveSpend(SpendTable(5L, "qq", 12, Date() - 20.days, null))
        spendsDao.saveSpend(SpendTable(6L, "qq", 11, Date() - 20.days, null))
        assertKindsList(
                SpendKindTable("qq", dateQ, 37, 3),
                SpendKindTable("kk", date3, 19, 2)
        )

        // delete all spends of kind "kk"
        spendsDao.deleteSpend(2L)
        spendsDao.deleteSpend(3L)
        assertKindsList(
                SpendKindTable("qq", dateQ, 37, 3)
        )
    }

    private fun assertKindsList(vararg expected: SpendKindTable) {
        Assert.assertEquals(expected.toList(), spendsDao.getAllKingsList())
    }
}