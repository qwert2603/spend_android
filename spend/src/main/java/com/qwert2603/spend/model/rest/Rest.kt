package com.qwert2603.spend.model.rest

import com.qwert2603.spend.model.rest.entity.GetRecordsUpdatesResult
import com.qwert2603.spend.model.rest.entity.SaveRecordsParam
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface Rest {

    @GET("get_records_updates")
    fun getRecordsUpdates(
            @Query("last_category_change_id") lastCategoryChangeId: Long?,
            @Query("last_record_change_id") lastRecordChangeId: Long?,
            @Query("count") count: Int
    ): Call<GetRecordsUpdatesResult>

    @POST("save_records")
    fun saveRecords(@Body saveRecordsParam: SaveRecordsParam): Call<Any>
}