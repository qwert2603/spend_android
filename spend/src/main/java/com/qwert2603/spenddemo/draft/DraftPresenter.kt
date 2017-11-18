package com.qwert2603.spenddemo.draft

import com.qwert2603.spenddemo.base_mvi.BasePresenter
import com.qwert2603.spenddemo.model.entity.CreatingRecord
import com.qwert2603.spenddemo.model.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.utils.switchToUiIfNotYet
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DraftPresenter @Inject constructor(
        private val draftInteractor: DraftInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : BasePresenter<DraftView, DraftViewState>(uiSchedulerProvider) {

    override fun bindIntents() {

        val kindIntent = intent { it.kingChanges() }.share()
        val draftChanges = draftInteractor.getDraft()
                .delaySubscription(intent { it.viewCreated() })
                .share()

        kindIntent
                .doOnNext { draftInteractor.onKindChanged(it, false) }
                .subscribeToView()
        intent { it.suggestionSelected() }
                .doOnNext { draftInteractor.onKindChanged(it, true) }
                .subscribeToView()
        intent { it.valueChanges() }
                .doOnNext { draftInteractor.onValueChanged(it) }
                .subscribeToView()
        intent { it.dateChanges() }
                .doOnNext { draftInteractor.onDateChanged(it) }
                .subscribeToView()

        kindIntent
                .debounce(100, TimeUnit.MILLISECONDS)
                .switchMap { search ->
                    draftInteractor.getMatchingKinds(search)
                            .doOnSuccess {
                                if (search !in it) {
                                    viewActions.onNext(DraftViewAction.ShowKindSuggestions(it, search))
                                } else {
                                    viewActions.onNext(DraftViewAction.HideKindSuggestions())
                                }
                            }
                            .toObservable()
                }
                .subscribeToView()

        intent { it.selectDateClicks() }
                .withLatestFrom(draftChanges, BiFunction { _: Any, creatingRecord: CreatingRecord -> creatingRecord })
                .doOnNext { viewActions.onNext(DraftViewAction.AskToSelectDate(it.date.time)) }
                .subscribeToView()

        intent { it.selectKindClicks() }
                .doOnNext { viewActions.onNext(DraftViewAction.AskToSelectKind()) }
                .subscribeToView()

        intent { it.saveClicks() }
                .doOnNext { draftInteractor.createRecord() }
                .doOnNext { viewActions.onNext(DraftViewAction.FocusOnKindInput()) }
                .subscribeToView()

        draftInteractor.focusOnValue()
                .doOnNext { viewActions.onNext(DraftViewAction.FocusOnValueInput()) }
                .subscribeToView()

        val observable = draftChanges.map { DraftViewState(it, draftInteractor.isValid(it)) }
        subscribeViewState(observable.switchToUiIfNotYet(uiSchedulerProvider), DraftView::render)
    }
}