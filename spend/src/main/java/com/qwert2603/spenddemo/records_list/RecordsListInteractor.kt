package com.qwert2603.spenddemo.records_list

import com.qwert2603.spenddemo.model.entity.CreatingProfit
import com.qwert2603.spenddemo.model.entity.Profit
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.RecordsState
import com.qwert2603.spenddemo.model.repo.ProfitsRepo
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.model.repo.UserSettingsRepo
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class RecordsListInteractor @Inject constructor(
        private val recordsRepo: RecordsRepo,
        private val userSettingsRepo: UserSettingsRepo,
        private val profitsRepo: ProfitsRepo
) {

    fun deleteRecord(id: Long) {
        recordsRepo.removeRecord(id)
    }

    fun editRecord(record: Record) {
        recordsRepo.editRecord(record)
    }

    fun recordsState(): Observable<RecordsState> = recordsRepo.recordsState()

    fun recordCreatedEvents(): Observable<Record> = recordsRepo.recordCreatedEvents()

    fun getRecordsTextToSend(): Single<String> = Single
            .zip(
                    recordsRepo.getDumpText(),
                    profitsRepo.getDumpText(),
                    BiFunction { spends, profits -> "SPENDS:\n$spends\n\nPROFITS:\n$profits" }
            )

    fun isShowIds() = userSettingsRepo.showIds
    fun setShowIds(show: Boolean) {
        userSettingsRepo.showIds = show
    }

    fun isShowChangeKinds() = userSettingsRepo.showChangeKinds
    fun setShowChangeKinds(show: Boolean) {
        userSettingsRepo.showChangeKinds = show
    }

    fun isShowDateSums() = userSettingsRepo.showDateSums
    fun setShowDateSums(show: Boolean) {
        userSettingsRepo.showDateSums = show
    }

    fun isShowSpends() = userSettingsRepo.showSpends
    fun setShowSpends(show: Boolean) {
        userSettingsRepo.showSpends = show
    }

    fun isShowProfits() = userSettingsRepo.showProfits
    fun setShowProfits(show: Boolean) {
        userSettingsRepo.showProfits = show
    }

    fun getAllProfits(): Single<List<Profit>> = profitsRepo.getAllProfits()
    fun addProfit(creatingProfit: CreatingProfit): Single<Long> = profitsRepo.addProfit(creatingProfit)
    fun removeProfit(profitId: Long): Completable = profitsRepo.removeProfit(profitId)
}