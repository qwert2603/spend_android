package com.qwert2603.spend.sums

import com.qwert2603.andrlib.base.mvi.BasePresenter
import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spend.model.entity.RecordsListItem
import com.qwert2603.spend.model.entity.SumsShowInfo
import com.qwert2603.spend.model.entity.SyncState
import com.qwert2603.spend.records_list.modifyForUi
import com.qwert2603.spend.utils.FastDiffUtils
import com.qwert2603.spend.utils.RxUtils
import io.reactivex.Observable

class SumsPresenter(
        private val interactor: SumsInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : BasePresenter<SumsView, SumsViewState>(uiSchedulerProvider) {

    override val initialState = SumsViewState(
            sumsShowInfo = interactor.sumsShowInfo.field,
            records = emptyList(),
            diff = FastDiffUtils.FastDiffResult.EMPTY,
            syncState = SyncState.Syncing
    )

    private val sumsShowInfoChanges: Observable<SumsShowInfo> = interactor.sumsShowInfo.changes.shareAfterViewSubscribed()

    override val partialChanges: Observable<PartialChange> = Observable.merge(
            sumsShowInfoChanges
                    .map { SumsPartialChange.ShowInfoChanged(it) },
            sumsShowInfoChanges
                    .switchMap { sumsShowInfo ->
                        RxUtils.dateChanges()
                                .startWith(Any())
                                .map { sumsShowInfo }
                    }
                    .switchMap { sumsShowInfo ->
                        interactor.getRecordsList()
                                .map { it.toSumsList(sumsShowInfo) }
                    }
                    .startWith(initialState.records)
                    .buffer(2, 1)
                    .map { (old, new) ->
                        val diff = FastDiffUtils.fastCalculateDiff(
                                oldList = old,
                                newList = new,
                                id = RecordsListItem::idInList,
                                compareOrder = RecordsListItem.COMPARE_ORDER,
                                isEqual = RecordsListItem.IS_EQUAL
                        )
                        SumsPartialChange.RecordsListChanged(list = new, diff = diff)
                    },
            interactor
                    .getSyncState()
                    .modifyForUi()
                    .map { SumsPartialChange.SyncStateChanged(it) }
    )

    override fun stateReducer(vs: SumsViewState, change: PartialChange): SumsViewState {
        if (change !is SumsPartialChange) throw Exception()
        return when (change) {
            is SumsPartialChange.RecordsListChanged -> vs.copy(
                    records = change.list,
                    diff = change.diff
            )
            is SumsPartialChange.ShowInfoChanged -> vs.copy(sumsShowInfo = change.sumsShowInfo)
            is SumsPartialChange.SyncStateChanged -> vs.copy(syncState = change.syncState)
        }
    }

    override fun bindIntents() {

        Observable
                .merge(listOf(
                        intent { it.showDaySums() }.map { SumsShowInfoChange.Days(it) },
                        intent { it.showMonthSums() }.map { SumsShowInfoChange.Months(it) },
                        intent { it.showYearSums() }.map { SumsShowInfoChange.Years(it) },
                        intent { it.showBalances() }.map { SumsShowInfoChange.Balances(it) }
                ))
                .doOnNext { ch ->
                    interactor.sumsShowInfo.updateField { info ->
                        return@updateField when (ch) {
                            is SumsShowInfoChange.Days -> info.copy(showDaySums = ch.show)
                            is SumsShowInfoChange.Months -> info.copy(showMonthSums = ch.show)
                            is SumsShowInfoChange.Years -> info.copy(showYearSums = ch.show)
                            is SumsShowInfoChange.Balances -> info.copy(showBalances = ch.show)
                        }
                    }

                }
                .subscribeToView()

        intent { it.clearAllClicks() }
                .doOnNext { interactor.removeAllRecords() }
                .subscribeToView()

        RxUtils.dateChanges()
                .doOnNext {
                    // we need to rerender all because old view holders with old list items
                    // in RecyclerView will not be redrawn if just to set list with old items.
                    viewActions.onNext(SumsViewAction.RerenderAll)
                }
                .subscribeToView()

        super.bindIntents()
    }

    private sealed class SumsShowInfoChange {
        data class Days(val show: Boolean) : SumsShowInfoChange()
        data class Months(val show: Boolean) : SumsShowInfoChange()
        data class Years(val show: Boolean) : SumsShowInfoChange()
        data class Balances(val show: Boolean) : SumsShowInfoChange()
    }
}