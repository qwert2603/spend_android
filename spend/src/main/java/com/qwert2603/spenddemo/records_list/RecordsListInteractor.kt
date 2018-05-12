package com.qwert2603.spenddemo.records_list

import com.qwert2603.spenddemo.model.entity.*
import com.qwert2603.spenddemo.model.repo.ProfitsRepo
import com.qwert2603.spenddemo.model.repo.SpendsRepo
import com.qwert2603.spenddemo.model.repo.UserSettingsRepo
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class RecordsListInteractor @Inject constructor(
        private val spendsRepo: SpendsRepo,
        private val userSettingsRepo: UserSettingsRepo,
        private val profitsRepo: ProfitsRepo
) {

    fun addSpend(creatingSpend: CreatingSpend) {
        spendsRepo.addSpend(creatingSpend)
    }

    fun deleteSpend(id: Long) {
        spendsRepo.removeSpend(id)
    }

    fun editSpend(spend: Spend) {
        spendsRepo.editSpend(spend)
    }

    fun deleteAllSpends() {
        spendsRepo.removeAllSpends()
    }

    fun spendsState(): Observable<SpendsState> = spendsRepo.spendsState()

    fun spendCreatedEvents(): Observable<Spend> = spendsRepo.spendCreatedEvents()

    fun getRecordsTextToSend(): Single<String> = Single
            .zip(
                    spendsRepo.getDumpText(),
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

    fun isShowMonthSums() = userSettingsRepo.showMonthSums
    fun setShowMonthSums(show: Boolean) {
        userSettingsRepo.showMonthSums = show
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
    fun editProfit(profit: Profit): Completable = profitsRepo.editProfit(profit)
    fun removeProfit(profitId: Long): Completable = profitsRepo.removeProfit(profitId)
    fun removeAllProfits(): Completable = profitsRepo.removeAllProfits()
}