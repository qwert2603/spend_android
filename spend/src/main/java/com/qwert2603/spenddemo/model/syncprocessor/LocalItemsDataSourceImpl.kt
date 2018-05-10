package com.qwert2603.spenddemo.model.syncprocessor

import com.qwert2603.andrlib.util.mapList
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.tables.toSpend
import com.qwert2603.spenddemo.model.local_db.tables.toSpendTable
import com.qwert2603.syncprocessor.datasource.LocalItemsDataSource
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalItemsDataSourceImpl @Inject constructor(
        private val localDB: LocalDB
) : LocalItemsDataSource<Long, SyncingSpend> {
    override fun getAll(): Single<List<SyncingSpend>> = localDB.spendsDao()
            .getAllSpends()
            .mapList { it.toSpend().toSyncingSpend() }

    override fun changeId(oldId: Long, newItem: SyncingSpend): Completable = Completable.fromAction {
        localDB.spendsDao().updateSpendId(oldId, newItem.toSpend().toSpendTable())
    }

    override fun save(t: SyncingSpend): Completable = Completable.fromAction {
        localDB.spendsDao().editSpend(t.toSpend().toSpendTable())
    }

    override fun remove(id: Long): Completable = Completable.fromAction {
        localDB.spendsDao().removeSpend(id)
    }
}