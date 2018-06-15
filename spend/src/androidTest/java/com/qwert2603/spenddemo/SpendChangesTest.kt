package com.qwert2603.spenddemo

import android.arch.persistence.room.Room
import android.database.sqlite.SQLiteConstraintException
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.qwert2603.spenddemo.model.entity.ChangeKind
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.dao.SpendChangesDao
import com.qwert2603.spenddemo.model.local_db.dao.SpendsDao
import com.qwert2603.spenddemo.model.local_db.tables.SpendChangeTable
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
class SpendChangesTest {
    private lateinit var spendsDao: SpendsDao
    private lateinit var spendChangesDao: SpendChangesDao
    private lateinit var mDb: LocalDB

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getTargetContext()
        mDb = Room.inMemoryDatabaseBuilder(context, LocalDB::class.java).build()
        spendsDao = mDb.spendsDao()
        spendChangesDao = mDb.spendChangesDao()
    }

    @After
    fun closeDb() {
        mDb.close()
    }

    @Test
    fun t1() {
        var spend = SpendTable(1, "kind", 14, Date() - 2.days)
        var spendChange = SpendChangeTable(1, ChangeKind.INSERT, 1)
        spendsDao.addSpend(spend)
        spendChangesDao.saveChange(spendChange)
        spend = spend.copy(id = 2)
        spendsDao.changeSpendId(1, 2)
        spendChange = spendChange.copy(spendId = 2)

        Assert.assertEquals(listOf(spend), spendsDao.getAllSpendsList())
        Assert.assertEquals(listOf(spendChange), spendChangesDao.getAllChangesList())
    }

    @Test(expected = SQLiteConstraintException::class)
    fun t2() {
        val spend = SpendTable(1, "kind", 14, Date() - 2.days)
        val spendChange = SpendChangeTable(1, ChangeKind.INSERT, 1)
        spendsDao.addSpend(spend)
        spendChangesDao.saveChange(spendChange)
        spendsDao.deleteSpend(1)
    }
}