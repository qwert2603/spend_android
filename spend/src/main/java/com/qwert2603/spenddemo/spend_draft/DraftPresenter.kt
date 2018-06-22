package com.qwert2603.spenddemo.spend_draft

import com.qwert2603.andrlib.base.mvi.BasePresenter
import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.model.entity.CreatingSpend
import com.qwert2603.spenddemo.utils.secondOfTwo
import com.qwert2603.spenddemo.utils.setDayFrom
import com.qwert2603.spenddemo.utils.setTimeFrom
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DraftPresenter @Inject constructor(
        private val draftInteractor: DraftInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : BasePresenter<DraftView, DraftViewState>(uiSchedulerProvider) {

    override val initialState = DraftViewState(
            creatingSpend = CreatingSpend(
                    kind = "",
                    value = 0,
                    date = null
            ),
            createEnable = false,
            showTime = false
    )

    private val loadDraft: Observable<CreatingSpend> = intent { it.viewCreated() }
            .switchMapSingle { draftInteractor.getDraft() }
            .share()

    private val clearDraft = PublishSubject.create<Any>()

    private val onDateSelectedIntent = intent { it.onDateSelected() }.share()
    private val onTimeSelectedIntent = intent { it.onTimeSelected() }.share()
    private val kingChangesIntent = intent { it.kingChanges() }.share()
    private val onKindSelectedIntent = intent { it.onKindSelected() }.share()
    private val onKindSuggestionSelectedIntent = intent { it.onKindSuggestionSelected() }.share()

    private val draftChanges: Observable<CreatingSpend> = Observable
            .merge(
                    loadDraft
                            .map { draft ->
                                { _: CreatingSpend -> draft }
                            },
                    Observable
                            .merge(listOf(
                                    onDateSelectedIntent
                                            .map { date ->
                                                { r: CreatingSpend -> r.copy(date = r.getDateNN().setDayFrom(date)) }
                                            },
                                    onTimeSelectedIntent
                                            .map { date ->
                                                { r: CreatingSpend -> r.copy(date = r.getDateNN().setTimeFrom(date)) }
                                            },
                                    kingChangesIntent
                                            .map { kind ->
                                                { r: CreatingSpend -> r.copy(kind = kind) }
                                            },
                                    intent { it.valueChanges() }
                                            .map { value ->
                                                { r: CreatingSpend -> r.copy(value = value) }
                                            },
                                    Observable
                                            .merge(
                                                    onKindSelectedIntent,
                                                    onKindSuggestionSelectedIntent
                                            )
                                            .map { kind ->
                                                Pair(kind, draftInteractor.getLastPriceOfKind(kind))
                                            }
                                            .map { (kind, lastPrice) ->
                                                { r: CreatingSpend -> r.copy(kind = kind, value = lastPrice) }
                                            },
                                    clearDraft
                                            .map {
                                                { _: CreatingSpend -> initialState.creatingSpend }
                                            }
                            ))
                            .delaySubscription(loadDraft)
            )
            .scan(initialState.creatingSpend) { creatingSpend: CreatingSpend, change: (CreatingSpend) -> CreatingSpend ->
                change(creatingSpend)
            }
            .skip(1) // skip initialValue in scan.
            .share()


    override val partialChanges: Observable<PartialChange> = Observable.merge(
            draftChanges
                    .map { DraftPartialChange.DraftChanged(it, draftInteractor.isCreatable(it)) },
            draftInteractor
                    .showTimesChanges()
                    .map { DraftPartialChange.ShowTimes(it) }
    )

    override fun stateReducer(vs: DraftViewState, change: PartialChange): DraftViewState {
        if (change !is DraftPartialChange) throw Exception()
        return when (change) {
            is DraftPartialChange.DraftChanged -> vs.copy(creatingSpend = change.creatingSpend, createEnable = change.createEnable)
            is DraftPartialChange.ShowTimes -> vs.copy(showTime = change.show)
        }
    }

    override fun bindIntents() {
        super.bindIntents()

        draftChanges
                .skip(1) // skip loaded draft.
                .switchMapCompletable { draftInteractor.saveDraft(it) }
                .toObservable<Any>()
                .subscribeToView()

        Observable
                .merge(
                        intent { it.onKindInputClicked() }
                                .withLatestFrom(draftChanges, secondOfTwo())
                                .map { it.kind },
                        kingChangesIntent
                                .debounce(100, TimeUnit.MILLISECONDS)
                )
                .skipUntil(loadDraft)
                .switchMapSingle { kind ->
                    draftInteractor.getSuggestions(kind)
                            .map { if (it.isNotEmpty()) it else listOf("smth") }
                            .doOnSuccess {
                                if (kind !in it) {
                                    viewActions.onNext(DraftViewAction.ShowKindSuggestions(it, kind))
                                } else {
                                    viewActions.onNext(DraftViewAction.HideKindSuggestions)
                                }
                            }
                }
                .subscribeToView()

        intent { it.selectDateClicks() }
                .withLatestFrom(draftChanges, secondOfTwo())
                .doOnNext { viewActions.onNext(DraftViewAction.AskToSelectDate(it.getDateNN().time)) }
                .subscribeToView()

        intent { it.selectTimeClicks() }
                .withLatestFrom(draftChanges, secondOfTwo())
                .doOnNext { viewActions.onNext(DraftViewAction.AskToSelectTime(it.getDateNN().time)) }
                .subscribeToView()

        intent { it.selectKindClicks() }
                .doOnNext { viewActions.onNext(DraftViewAction.AskToSelectKind) }
                .subscribeToView()

        intent { it.saveClicks() }
                .withLatestFrom(draftChanges, secondOfTwo())
                .filter { draftInteractor.isCreatable(it) }
                .switchMapCompletable {
                    draftInteractor.createSpend(it)
                            .doOnComplete { clearDraft.onNext(Any()) }
                            .doOnComplete { viewActions.onNext(DraftViewAction.FocusOnKindInput) }
                }
                .toObservable<Any>()
                .subscribeToView()

        Observable.merge(onDateSelectedIntent, onTimeSelectedIntent)
                .doOnNext { viewActions.onNext(DraftViewAction.FocusOnKindInput) }
                .subscribeToView()

        Observable
                .merge(
                        onKindSelectedIntent,
                        onKindSuggestionSelectedIntent
                )
                .doOnNext { viewActions.onNext(DraftViewAction.FocusOnValueInput) }
                .subscribeToView()
    }
}