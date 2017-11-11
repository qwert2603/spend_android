package com.qwert2603.spenddemo.model.syncprocessor

import com.qwert2603.spenddemo.model.entity.toChangeKind
import com.qwert2603.spenddemo.model.entity.toSyncChangeKind
import com.qwert2603.spenddemo.model.local_db.LocalDB
import com.qwert2603.spenddemo.model.local_db.tables.ChangeTable
import com.qwert2603.spenddemo.utils.mapList
import com.qwert2603.syncprocessor.datasource.LocalChangesDataSource
import com.qwert2603.syncprocessor.entity.Change
import io.reactivex.Completable
import io.reactivex.Single

class LocalChangesDataSourceImpl(
        private val localDB: LocalDB
) : LocalChangesDataSource<Long> {
    override fun getAll(): Single<List<Change<Long>>> = localDB.changesDao()
            .getAllChanges()
            .mapList { Change(it.recordId, it.changeKind.toSyncChangeKind()) }

    override fun save(change: Change<Long>): Completable = Completable.fromAction {
        localDB.changesDao().saveChange(ChangeTable(change.changeKind.toChangeKind(), change.itemId))
    }

    override fun remove(itemId: Long): Completable = Completable.fromAction {
        localDB.changesDao().removeChange(itemId)
    }
}