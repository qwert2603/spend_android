package com.qwert2603.spenddemo.model.local_db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import io.reactivex.Single

@Dao
interface KindsDao {
    @Query("SELECT kind FROM RecordTable GROUP BY kind ORDER BY count(*) DESC")
    fun getAllKings(): Single<List<String>>
}