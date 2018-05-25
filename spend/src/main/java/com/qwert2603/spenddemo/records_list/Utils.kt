package com.qwert2603.spenddemo.records_list

import com.qwert2603.spenddemo.model.entity.CreatingProfit
import com.qwert2603.spenddemo.model.entity.CreatingSpend
import com.qwert2603.spenddemo.records_list.entity.*
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import java.util.*
import java.util.concurrent.TimeUnit

fun List<SpendUI>.sumSpends() = sumByLong { it.value.toLong() }
fun List<ProfitUI>.sumProfits() = sumByLong { it.value.toLong() }

fun makeRecordsList(
        spendsList: List<SpendUI>,
        profitsList: List<ProfitUI>,
        showDateSums: Boolean,
        showMonthSums: Boolean,
        showProfits: Boolean,
        showSpends: Boolean
): List<RecordsListItem> {
    val spendsByDate = spendsList.groupBy { it.date.onlyDate() }
    val profitsByDate = profitsList.groupBy { it.date.onlyDate() }
    val datesByMonth = (spendsByDate.keys union profitsByDate.keys).groupBy { it.onlyMonth() }
    val showSpendSum = showSpends || !showProfits
    val showProfitSum = showProfits || !showSpends
    return datesByMonth.keys
            .sortedDescending()
            .map { month ->
                datesByMonth[month]!!
                        .sortedDescending()
                        .map { date ->
                            (profitsByDate[date] plusNN spendsByDate[date])
                                    .plus(DateSumUI(
                                            date = date,
                                            showSpends = showSpendSum,
                                            showProfits = showProfitSum,
                                            spends = spendsByDate[date]?.sumSpends() ?: 0,
                                            profits = profitsByDate[date]?.sumProfits() ?: 0
                                    ))
                        }
                        .flatten()
                        .let {
                            it + MonthSumUI(
                                    date = month,
                                    showSpends = showSpendSum,
                                    showProfits = showProfitSum,
                                    spends = it.mapNotNull { it as? SpendUI }.sumSpends(),
                                    profits = it.mapNotNull { it as? ProfitUI }.sumProfits()
                            )
                        }
            }
            .flatten()
            .plus(createTotalsItem(spendsList, profitsList, showProfitSum, showSpendSum))
            .filter {
                when (it) {
                    is SpendUI -> showSpends
                    is ProfitUI -> showProfits
                    is DateSumUI -> showDateSums && when {
                        showSpends == showProfits -> true
                        showSpends -> spendsByDate[it.date] != null
                        else -> profitsByDate[it.date] != null
                    }
                    is MonthSumUI -> showMonthSums && when {
                        showSpends == showProfits -> true
                        showSpends -> datesByMonth[it.date]!!.any { it in spendsByDate }
                        else -> datesByMonth[it.date]!!.any { it in profitsByDate }
                    }
                    is TotalsUI -> true
                    else -> null!!
                }
            }
}

fun createTotalsItem(
        spendsList: List<SpendUI>,
        profitsList: List<ProfitUI>,
        showProfits: Boolean,
        showSpends: Boolean
): TotalsUI {
    val spendsSum = spendsList.sumSpends()
    val profitsSum = profitsList.sumProfits()
    return TotalsUI(
            showProfits = showProfits,
            showSpends = showSpends,
            spendsCount = spendsList.size,
            spendsSum = spendsSum,
            profitsCount = profitsList.size,
            profitsSum = profitsSum,
            totalBalance = profitsSum - spendsSum
    )
}

fun addStubSpends(recordsListInteractor: RecordsListInteractor, count: Int): Observable<Long> {
    val stubSpendKinds = listOf("трамвай", "столовая", "шоколадка", "автобус")
    val random = Random()
    return Observable.interval(100, TimeUnit.MILLISECONDS)
            .take(count.toLong())
            .doOnNext {
                recordsListInteractor.addSpend(CreatingSpend(
                        kind = stubSpendKinds[random.nextInt(stubSpendKinds.size)],
                        value = random.nextInt(1000) + 1,
                        date = Date() - (random.nextInt(21)).days
                ))
            }
}

fun addStubProfits(recordsListInteractor: RecordsListInteractor, count: Int): Observable<Long> {
    val stubProfitKinds = listOf("стипендия", "зарплата", "аванс", "доход")
    val random = Random()
    return Observable.range(0, count)
            .concatMapSingle {
                recordsListInteractor.addProfit(CreatingProfit(
                        kind = stubProfitKinds[random.nextInt(stubProfitKinds.size)],
                        value = random.nextInt(10000) + 1,
                        date = Date() - (random.nextInt(21)).days
                ))
            }
}