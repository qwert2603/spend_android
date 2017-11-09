package com.qwert2603.spenddemo.draft

import com.qwert2603.spenddemo.base_mvi.BasePresenter
import com.qwert2603.spenddemo.model.entity.CreatingRecord
import com.qwert2603.spenddemo.model.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.utils.LogUtils
import com.qwert2603.spenddemo.utils.switchToUiIfNotYet
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class DraftPresenter @Inject constructor(
        private val draftInteractor: DraftInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : BasePresenter<DraftView, DraftViewState>(uiSchedulerProvider) {
    companion object {
        val INITIAL_MODEL = DraftViewState(DraftInteractor.EmptyCreatingRecord, false)
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

        val draftChanges = Observable
                .merge(listOf(
                        draftLoaded,
                        kindIntent,
                        valueIntent,
                        dateIntent,
                        clearEvents
                ))
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

        draftChanges
                .switchMap { draftInteractor.onDraftChanged(it).toObservable<Unit>() }
                .subscribeToView()

        intent { it.selectDateClicks() }
                .withLatestFrom(draftChanges, BiFunction { _: Any, creatingRecord: CreatingRecord -> creatingRecord })
                .doOnNext { viewActions.onNext(DraftViewAction.AskToSelectDate(it.date.time)) }
                .subscribeToView()

        intent { it.selectKindClicks() }
                .doOnNext { viewActions.onNext(DraftViewAction.AskToSelectKind()) }
                .subscribeToView()

        intent { it.saveClicks() }
                .withLatestFrom(draftChanges, BiFunction { _: Any, r: CreatingRecord -> r })
                .flatMapCompletable {
                    draftInteractor.createRecord(it)
                            .onErrorComplete {
                                LogUtils.e("createRecord error", it)
                                true
                            }
                }
                .toObservable<Unit>()
                .subscribeToView()

        val observable = draftChanges.map { DraftViewState(it, draftInteractor.isValid(it)) }
        subscribeViewState(observable.switchToUiIfNotYet(uiSchedulerProvider), DraftView::render)
    }
}