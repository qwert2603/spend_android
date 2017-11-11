package com.qwert2603.spenddemo.model.local_db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.qwert2603.spenddemo.model.local_db.results.KindResult
import io.reactivex.Flowable

@Dao
interface KindsDao {
    @Query("SELECT kind, value, date FROM RecordTable")
    fun getAllKings(): Flowable<List<KindResult>>
}