package com.qwert2603.spenddemo.draft

import com.qwert2603.spenddemo.base_mvi.BasePresenter
import com.qwert2603.spenddemo.model.entity.CreatingRecord
import com.qwert2603.spenddemo.model.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.utils.LogUtils
import com.qwert2603.spenddemo.utils.switchToUiIfNotYet
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.util.*
import javax.inject.Inject

class DraftPresenter @Inject constructor(
        private val draftInteractor: DraftInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : BasePresenter<DraftView, DraftViewState>(uiSchedulerProvider) {
    companion object {
        val INITIAL_MODEL = DraftViewState("", 0, Date(0), false)
    }

    override fun bindIntents() {

        val draftLoaded = intent { it.viewCreated() }
                .flatMap { draftInteractor.getDraft() }
                .map { DraftPartialChange.DraftLoaded(it) }
                .share()

        val kindIntent = intent { it.kingChanges() }
                .map { DraftPartialChange.KindChanged(it) }
                .skipUntil(draftLoaded)
        val valueIntent = intent { it.valueChanges() }
                .map { DraftPartialChange.ValueChanged(it) }
                .skipUntil(draftLoaded)
        val dateIntent = intent { it.dateChanges() }
                .map { DraftPartialChange.DateChanged(it) }
                .skipUntil(draftLoaded)

        val clearEvents = draftInteractor.clearEvents()
                .map { DraftPartialChange.DraftCleared() }
                .doOnNext { viewActions.onNext(DraftViewAction.FocusOnKindInput()) }

        val draftIntents = Observable
                .merge(listOf(
                        draftLoaded,
                        kindIntent,
                        valueIntent,
                        dateIntent,
                        clearEvents
                ))
                .share()

        val draftChanges = draftIntents
                .scan(DraftInteractor.EmptyCreatingRecord, { creatingRecord, change ->
                    LogUtils.d("DraftPresenter draftChanges $change")
                    when (change) {
                        is DraftPartialChange.DraftLoaded -> change.creatingRecord
                        is DraftPartialChange.KindChanged -> creatingRecord.copy(kind = change.kind)
                        is DraftPartialChange.ValueChanged -> creatingRecord.copy(value = change.value)
                        is DraftPartialChange.DateChanged -> creatingRecord.copy(date = change.date)
                        is DraftPartialChange.DraftCleared -> DraftInteractor.EmptyCreatingRecord
                        else -> null!!
                    }
                })
                .skip(1)
                .share()

        intent { it.selectDateClicks() }
                .withLatestFrom(draftChanges, BiFunction { _: Any, creatingRecord: CreatingRecord -> creatingRecord })
                .doOnNext { viewActions.onNext(DraftViewAction.AskToSelectDate(it.date.time)) }
                .subscribeToView()

        intent { it.selectKindClicks() }
                .doOnNext { viewActions.onNext(DraftViewAction.AskToSelectKind()) }
                .subscribeToView()

        val observable = Observable.merge(listOf(
                draftIntents,
                draftChanges
                        .flatMapSingle {
                            draftInteractor.onDraftChanged(it)
                                    .toSingleDefault(draftInteractor.isValid(it))
                                    .map { DraftPartialChange.CreateEnableChanged(it) }
                        },
                intent { it.saveClicks() }
                        .withLatestFrom(draftChanges, BiFunction { _: Any, r: CreatingRecord -> r })
                        .flatMapCompletable {
                            draftInteractor.createRecord(it)
                                    .onErrorComplete {
                                        LogUtils.e("createRecord error", it)
                                        true
                                    }
                        }
                        .toObservable<DraftPartialChange>()
        ))
        subscribeViewState(observable.switchToUiIfNotYet(uiSchedulerProvider).scan(INITIAL_MODEL, this::stateReducer).skip(1), DraftView::render)
    }

    private fun stateReducer(viewState: DraftViewState, change: DraftPartialChange): DraftViewState {
        LogUtils.d("DraftPresenter stateReducer $change")
        return when (change) {
            is DraftPartialChange.DraftLoaded -> viewState.copy(
                    kind = change.creatingRecord.kind,
                    value = change.creatingRecord.value,
                    date = change.creatingRecord.date
            )
            is DraftPartialChange.KindChanged -> viewState.copy(kind = change.kind)
            is DraftPartialChange.ValueChanged -> viewState.copy(value = change.value)
            is DraftPartialChange.DateChanged -> viewState.copy(date = change.date)
            is DraftPartialChange.DraftCleared -> viewState.copy(kind = "", value = 0, date = Date())
            is DraftPartialChange.CreateEnableChanged -> viewState.copy(createEnable = change.canCreated)
        }
    }
}