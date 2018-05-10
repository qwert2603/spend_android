package com.qwert2603.spenddemo.model.local_db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.qwert2603.spenddemo.model.local_db.results.SpendKindResult
import io.reactivex.Flowable

@Dao
interface SpendKindsDao {
    @Query("SELECT kind, value, date FROM SpendTable")
    fun getAllKings(): Flowable<List<SpendKindResult>>
}