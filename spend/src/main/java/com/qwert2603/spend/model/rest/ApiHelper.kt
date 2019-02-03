package com.qwert2603.spend.model.rest

import com.qwert2603.spend.model.rest.entity.GetRecordsUpdatesResult
import com.qwert2603.spend.model.rest.entity.LastChangeInfo
import com.qwert2603.spend.model.rest.entity.RecordServer
import com.qwert2603.spend.model.rest.entity.SaveRecordsParam
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiHelper @Inject constructor(
        private val rest: Rest
) {
    fun getUpdates(lastChangeInfo: LastChangeInfo?, count: Int): GetRecordsUpdatesResult = rest
            .getRecordsUpdates(
                    lastCategoryChangeId = lastChangeInfo?.lastCategoryChangeId,
                    lastRecordChangeId = lastChangeInfo?.lastRecordChangeId,
                    count = count
            )
            .execute()
            .let {
                if (!it.isSuccessful) throw HttpException(it)
                it.body()!!
            }

    fun saveChanges(updated: List<RecordServer>, deletedUuids: List<String>) = rest
            .saveRecords(SaveRecordsParam(updated, deletedUuids))
            .execute()
            .let {
                if (!it.isSuccessful) throw HttpException(it)
                Unit
            }
}