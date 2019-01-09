package com.qwert2603.spenddemo.change_records

import com.qwert2603.andrlib.base.mvi.BasePresenter
import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.model.entity.days
import com.qwert2603.spenddemo.model.entity.plus
import com.qwert2603.spenddemo.utils.Const
import com.qwert2603.spenddemo.utils.DateUtils
import com.qwert2603.spenddemo.utils.RxUtils
import com.qwert2603.spenddemo.utils.secondOfTwo
import io.reactivex.Observable
import javax.inject.Inject

class ChangeRecordsPresenter @Inject constructor(
        private val recordsUuids: List<String>,
        private val interactor: ChangeRecordsInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : BasePresenter<ChangeRecordsView, ChangeRecordsViewState>(uiSchedulerProvider) {

    override val initialState = ChangeRecordsViewState(null, null)

    override val partialChanges: Observable<PartialChange> = Observable.merge(
            intent { it.changedDateSelected() }
                    .map { ChangeRecordsPartialChange.ChangedDateSelected(it.t) },
            intent { it.changedTimeSelected() }
                    .map { ChangeRecordsPartialChange.ChangedTimeSelected(it.t) }
    )

    override fun stateReducer(vs: ChangeRecordsViewState, change: PartialChange): ChangeRecordsViewState {
        if (change !is ChangeRecordsPartialChange) throw Exception()
        return when (change) {
            is ChangeRecordsPartialChange.ChangedDateSelected -> vs.copy(changedDate = change.date)
            is ChangeRecordsPartialChange.ChangedTimeSelected -> vs.copy(changedTime = change.time)
        }
    }

    override fun bindIntents() {

        intent { it.askToSelectDateClicks() }
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .doOnNext {
                    viewActions.onNext(ChangeRecordsViewAction.AskToSelectDate(
                            date = it.changedDate ?: DateUtils.getNow().first,
                            minDate = DateUtils.getNow().first + (-1 * Const.CHANGE_RECORD_PAST.days + 1).days
                    ))
                }
                .subscribeToView()

        intent { it.askToSelectTimeClicks() }
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .doOnNext {
                    viewActions.onNext(ChangeRecordsViewAction.AskToSelectTime(
                            it.changedTime?.t ?: DateUtils.getNow().second
                    ))
                }
                .subscribeToView()

        intent { it.changeClicks() }
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .doOnNext {
                    interactor.changeRecords(
                            recordsUuids = recordsUuids,
                            changedDate = it.changedDate,
                            changedTime = it.changedTime
                    )
                    viewActions.onNext(ChangeRecordsViewAction.Close)
                }
                .subscribeToView()

        RxUtils.dateChanges()
                .doOnNext { viewActions.onNext(ChangeRecordsViewAction.RerenderAll) }
                .subscribeToView()

        super.bindIntents()
    }
}