package com.qwert2603.spenddemo

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.dao.SpendsDao
import com.qwert2603.spenddemo.model.local_db.tables.SpendTable
import com.qwert2603.spenddemo.utils.onlyDate
import com.qwert2603.spenddemo.utils.sumByLong
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class RoomTest {
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
    fun writeUserAndReadInList() {
        val spend = SpendTable(12L, "food", 33, Date().onlyDate())
        spendsDao.addSpend(spend)
        val allSpends = spendsDao.getAllSpendsList()
        Assert.assertEquals(allSpends.size, 1)
        Assert.assertEquals(allSpends.first(), spend)
    }

    @Test
    fun totalSum() {
        val spends = listOf(
                SpendTable(12L, "food", 33, Date().onlyDate()),
                SpendTable(13L, "food", 122, Date().onlyDate()),
                SpendTable(14L, "food", 44, Date().onlyDate())
        )
        spendsDao.addSpends(spends)
        Assert.assertEquals(spendsDao.getTotal(), spends.sumByLong { it.value.toLong() })
    }
}