package com.qwert2603.spenddemo.edit_spend

import com.qwert2603.andrlib.base.mvi.BasePresenter
import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.andrlib.model.IdentifiableLong
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class EditSpendPresenter @Inject constructor(
        spendId: Long,
        private val editSpendInteractor: EditSpendInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : BasePresenter<EditSpendView, EditSpendViewState>(uiSchedulerProvider) {

    override val initialState = EditSpendViewState(
            id = IdentifiableLong.NO_ID,
            kind = "",
            date = Date(0),
            time = null,
            value = 0,
            serverKind = null,
            serverDate = null,
            serverTime = null,
            serverValue = null,
            justChangedOnServer = false
    )

    private val serverSpendChanges: Observable<Wrapper<Spend>> = intent { it.viewCreated() }
            .switchMap { editSpendInteractor.getSpendChanges(spendId) }
            .shareReplayLast()

    override val partialChanges: Observable<PartialChange> = Observable.merge(listOf(
            serverSpendChanges
                    .filter { it.t != null }
                    .take(1)
                    .map { EditSpendPartialChange.SpendLoaded(it.t!!) },
            serverSpendChanges
                    .mapNotNull { it.t }
                    .shareReplayLast()
                    .let { spendChanges ->
                        fun <T> serverPartialChanges(
                                mapper: (Spend) -> T,
                                partialChangesCreator: (T) -> PartialChange
                        ): Observable<PartialChange> = spendChanges
                                .map(mapper)
                                .distinctUntilChanged()
                                .skip(1)
                                .map(partialChangesCreator)

                        Observable.merge(
                                serverPartialChanges({ it.kind }, { EditSpendPartialChange.KindChangeOnServer(it) }),
                                serverPartialChanges({ it.value }, { EditSpendPartialChange.ValueChangeOnServer(it) }),
                                serverPartialChanges({ it.date }, { EditSpendPartialChange.DateChangeOnServer(it) }),
                                serverPartialChanges({ it.time.wrap() }, { EditSpendPartialChange.TimeChangeOnServer(it.t) })
                        )
                    },
            serverSpendChanges
                    .mapNotNull { it.t }
                    .distinctUntilChanged()
                    .skip(1)
                    .switchMap { _ ->
                        Observable.interval(0, 700, TimeUnit.MILLISECONDS)
                                .take(2)
                                .map { it == 0L }
                    }
                    .map { EditSpendPartialChange.SpendJustChangedOnServer(it) },
            intent { it.onServerKindResolved() }
                    .map { EditSpendPartialChange.KindServerResolved(it) },
            intent { it.onServerDateResolved() }
                    .map { EditSpendPartialChange.DateServerResolved(it) },
            intent { it.onServerTimeResolved() }
                    .map { EditSpendPartialChange.TimeServerResolved(it) },
            intent { it.onServerValueResolved() }
                    .map { EditSpendPartialChange.ValueServerResolved(it) },
            intent { it.kindChanges() }
                    .map { EditSpendPartialChange.KindChanged(it) },
            intent { it.valueChanges() }
                    .map { EditSpendPartialChange.ValueChanged(it) },
            intent { it.onKindSelected() }
                    .doOnNext { viewActions.onNext(EditSpendViewAction.FocusOnValueInput) }
                    .map { EditSpendPartialChange.KindSelected(it) },
            intent { it.onDateSelected() }
                    .doOnNext { viewActions.onNext(EditSpendViewAction.FocusOnKindInput) }
                    .map { EditSpendPartialChange.DateSelected(it) },
            intent { it.onTimeSelected() }
                    .doOnNext { viewActions.onNext(EditSpendViewAction.FocusOnKindInput) }
                    .map { EditSpendPartialChange.TimeSelected(it.t) },
            Observable.interval(300, TimeUnit.MILLISECONDS)
                    .map { Date().onlyDate() }
                    .distinctUntilChanged()
                    .skip(1)
                    .map { EditSpendPartialChange.CurrentDateChanged }
    ))

    override fun stateReducer(vs: EditSpendViewState, change: PartialChange): EditSpendViewState {
        if (change !is EditSpendPartialChange) null!!
        return when (change) {
            is EditSpendPartialChange.SpendLoaded -> vs.copy(
                    id = change.spend.id,
                    kind = change.spend.kind,
                    value = change.spend.value,
                    date = change.spend.date,
                    time = change.spend.time
            )
            is EditSpendPartialChange.KindChanged -> vs.copy(kind = change.kind)
            is EditSpendPartialChange.ValueChanged -> vs.copy(value = change.value)
            is EditSpendPartialChange.KindSelected -> vs.copy(kind = change.kind)
            is EditSpendPartialChange.DateSelected -> vs.copy(date = change.date)
            is EditSpendPartialChange.TimeSelected -> vs.copy(time = change.time)
            is EditSpendPartialChange.KindChangeOnServer -> vs.copy(serverKind = change.kind)
            is EditSpendPartialChange.ValueChangeOnServer -> vs.copy(serverValue = change.value)
            is EditSpendPartialChange.DateChangeOnServer -> vs.copy(serverDate = change.date)
            is EditSpendPartialChange.TimeChangeOnServer -> vs.copy(serverTime = change.time.wrap())
            is EditSpendPartialChange.SpendJustChangedOnServer -> vs.copy(justChangedOnServer = change.justChanged)
            is EditSpendPartialChange.KindServerResolved -> vs.copy(
                    kind = if (change.acceptFromServer) vs.serverKind ?: vs.kind else vs.kind,
                    serverKind = null
            )
            is EditSpendPartialChange.ValueServerResolved -> vs.copy(
                    value = if (change.acceptFromServer) vs.serverValue ?: vs.value else vs.value,
                    serverValue = null
            )
            is EditSpendPartialChange.DateServerResolved -> vs.copy(
                    date = if (change.acceptFromServer) vs.serverDate ?: vs.date else vs.date,
                    serverDate = null
            )
            is EditSpendPartialChange.TimeServerResolved -> vs.copy(
                    time = if (change.acceptFromServer) vs.serverTime?.t ?: vs.time else vs.time,
                    serverTime = null
            )
            EditSpendPartialChange.CurrentDateChanged -> vs
        }
    }

    override fun bindIntents() {
        super.bindIntents()

        intent { it.selectKindClicks() }
                .doOnNext { viewActions.onNext(EditSpendViewAction.AskToSelectKind) }
                .subscribeToView()

        intent { it.selectDateClicks() }
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .doOnNext { viewActions.onNext(EditSpendViewAction.AskToSelectDate(it.date.time)) }
                .subscribeToView()

        intent { it.selectTimeClicks() }
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .doOnNext {
                    viewActions.onNext(EditSpendViewAction.AskToSelectTime(
                            (it.time ?: Date().onlyTime()).time
                    ))
                }
                .subscribeToView()

        serverSpendChanges
                .filter { it.t == null }
                .take(1)
                .doOnNext { viewActions.onNext(EditSpendViewAction.EditingSpendDeletedOnServer) }
                .subscribeToView()

        intent { it.saveClicks() }
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .doOnNext { viewActions.onNext(EditSpendViewAction.SendResult(it.getSpend())) }
                .subscribeToView()
    }
}