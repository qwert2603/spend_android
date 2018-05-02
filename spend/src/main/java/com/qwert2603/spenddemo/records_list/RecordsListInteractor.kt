package com.qwert2603.spenddemo.records_list

import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.RecordsState
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.model.repo.UserSettingsRepo
import com.qwert2603.spenddemo.utils.Const
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class RecordsListInteractor @Inject constructor(
        private val recordsRepo: RecordsRepo,
        private val userSettingsRepo: UserSettingsRepo
) {

    fun deleteRecord(id: Long) {
        recordsRepo.removeRecord(id)
    }

    fun editRecord(record: Record) {
        recordsRepo.editRecord(record)
    }

    fun recordsState(): Observable<RecordsState> = recordsRepo.recordsState()

    fun recordCreatedEvents(): Observable<Record> = recordsRepo.recordCreatedEvents()

    fun getRecordsTextToSend(): Single<String> = recordsRepo.recordsState()
            .firstOrError()
            .map {
                if (it.records.isEmpty()) return@map ""
                it.records
                        .reversed()
                        .map {
                            listOf(
                                    it.kind,
                                    Const.DATE_FORMAT.format(it.date),
                                    it.value.toString()
                            ).reduce { s1, s2 -> "$s1,$s2" }
                        }
                        .reduce { s1, s2 -> "$s1\n$s2" }
            }

    fun isShowIds() = userSettingsRepo.showIds
    fun setShowIds(show: Boolean) {
        userSettingsRepo.showIds = show
    }

    fun isShowChangeKinds() = userSettingsRepo.showChangeKinds
    fun setShowChangeKinds(show: Boolean) {
        userSettingsRepo.showChangeKinds = show
    }

    fun isShowDateSums() = userSettingsRepo.showChangeDateSums
    fun setShowDateSums(show: Boolean) {
        userSettingsRepo.showChangeDateSums = show
    }
}