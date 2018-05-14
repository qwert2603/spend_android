package com.qwert2603.spenddemo.spend_draft

import com.qwert2603.andrlib.base.mvi.BasePresenter
import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.model.entity.CreatingSpend
import com.qwert2603.spenddemo.utils.secondOfTwo
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
            createEnable = false
    )

    private val loadDraft: Observable<CreatingSpend> = intent { it.viewCreated() }
            .switchMapSingle { draftInteractor.getDraft() }
            .share()

    private val clearDraft = PublishSubject.create<Any>()

    private val onDateSelectedIntent = intent { it.onDateSelected() }.share()
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
                                                { r: CreatingSpend -> r.copy(date = date) }
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
                                            .switchMapSingle { kind ->
                                                draftInteractor.getLastPriceOfKind(kind).map { Pair(kind, it) }
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
            .scan(initialState.creatingSpend, { creatingSpend: CreatingSpend, change: (CreatingSpend) -> CreatingSpend ->
                change(creatingSpend)
            })
            .skip(1) // skip initialValue in scan.
            .share()


    private data class DraftChanged(val draftViewState: DraftViewState) : PartialChange

    override val partialChanges: Observable<PartialChange> = draftChanges
            .map { DraftViewState(it, draftInteractor.isCreatable(it)) }
            .map { DraftChanged(it) }

    override fun stateReducer(vs: DraftViewState, change: PartialChange) = (change as DraftChanged).draftViewState

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

        onDateSelectedIntent
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