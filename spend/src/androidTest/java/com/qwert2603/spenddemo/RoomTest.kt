package com.qwert2603.spenddemo

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.dao.ProfitsDao
import com.qwert2603.spenddemo.model.local_db.dao.SpendsDao
import com.qwert2603.spenddemo.model.local_db.tables.ProfitTable
import com.qwert2603.spenddemo.model.local_db.tables.SpendTable
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
    private lateinit var profitsDao: ProfitsDao
    private lateinit var mDb: LocalDB

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getTargetContext()
        mDb = Room.inMemoryDatabaseBuilder(context, LocalDB::class.java).build()
        spendsDao = mDb.spendsDao()
        profitsDao = mDb.profitsDao()
    }

    @After
    fun closeDb() {
        mDb.close()
    }

    @Test
    fun writeAndRead() {
        val spend = SpendTable(12L, "food", 33, Date(), null)
        spendsDao.saveSpend(spend)
        val allSpends = spendsDao.getAllSpendsList()
        Assert.assertEquals(allSpends.size, 1)
        Assert.assertEquals(allSpends.first(), spend)
    }

    @Test
    fun totalSum() {
        val spends = listOf(
                SpendTable(12L, "food", 33, Date(), null),
                SpendTable(13L, "food", 122, Date(), null),
                SpendTable(14L, "food", 44, Date(), null)
        )
        spendsDao.addSpends(spends)
        Assert.assertEquals(spendsDao.getTotal(), spends.sumByLong { it.value.toLong() })
    }

    @Test
    fun getAllRecords() {
        val spends = listOf(
                SpendTable(12L, "food", 33, Date(), null),
                SpendTable(13L, "food", 122, Date(), null),
                SpendTable(14L, "food", 44, Date(), null)
        )
        val profits = listOf(
                ProfitTable(4L, "salary", 23, Date()),
                ProfitTable(16L, "salary", 23, Date()),
                ProfitTable(17L, "salary", 23, Date())
        )
        spendsDao.addSpends(spends)
        profits.forEach { profitsDao.addProfit(it) }
        Assert.assertEquals(spendsDao.getAllRecordsIds(), (spends.map { it.id } + profits.map { it.id }).sorted())
    }
}