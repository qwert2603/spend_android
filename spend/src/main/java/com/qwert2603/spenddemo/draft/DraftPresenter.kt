package com.qwert2603.spenddemo.draft

import com.qwert2603.andrlib.base.mvi.BasePresenter
import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.model.entity.CreatingRecord
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DraftPresenter @Inject constructor(
        private val draftInteractor: DraftInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : BasePresenter<DraftView, DraftViewState>(uiSchedulerProvider) {

    override val initialState = DraftViewState(CreatingRecord("", 0, Date(), false), false)

    private val kindIntent = intent { it.kingChanges() }.share()
    private val draftChanges = draftInteractor.getDraft()
            .delaySubscription(intent { it.viewCreated() })
            .share()

    private data class DraftChanged(val draftViewState: DraftViewState) : PartialChange

    override val partialChanges: Observable<PartialChange> = draftChanges
            .map { DraftViewState(it, draftInteractor.isValid(it)) }
            .map { DraftChanged(it) }

    override fun stateReducer(vs: DraftViewState, change: PartialChange) = (change as DraftChanged).draftViewState

    override fun bindIntents() {
        super.bindIntents()

        kindIntent
                .doOnNext { draftInteractor.onKindChanged(it, false) }
                .subscribeToView()
        intent { it.suggestionSelected() }
                .doOnNext { draftInteractor.onKindChanged(it, true) }
                .subscribeToView()
        intent { it.valueChanges() }
                .doOnNext { draftInteractor.onValueChanged(it) }
                .subscribeToView()

        kindIntent
                .debounce(100, TimeUnit.MILLISECONDS)
                .switchMapSingle { search ->
                    draftInteractor.getMatchingKinds(search)
                            .doOnSuccess {
                                if (search !in it) {
                                    viewActions.onNext(DraftViewAction.ShowKindSuggestions(it, search))
                                } else {
                                    viewActions.onNext(DraftViewAction.HideKindSuggestions)
                                }
                            }
                }
                .subscribeToView()

        intent { it.selectDateClicks() }
                .withLatestFrom(draftChanges, BiFunction { _: Any, creatingRecord: CreatingRecord -> creatingRecord })
                .doOnNext { viewActions.onNext(DraftViewAction.AskToSelectDate(it.date.time)) }
                .subscribeToView()

        intent { it.selectKindClicks() }
                .doOnNext { viewActions.onNext(DraftViewAction.AskToSelectKind) }
                .subscribeToView()

        intent { it.saveClicks() }
                .withLatestFrom(draftChanges, BiFunction { _: Any, creatingRecord: CreatingRecord -> creatingRecord })
                .filter { draftInteractor.isValid(it) }
                .doOnNext { draftInteractor.createRecord() }
                .doOnNext { viewActions.onNext(DraftViewAction.FocusOnKindInput) }
                .subscribeToView()

        draftInteractor.kindSelected()
                .doOnNext { viewActions.onNext(DraftViewAction.FocusOnValueInput) }
                .subscribeToView()
    }
}