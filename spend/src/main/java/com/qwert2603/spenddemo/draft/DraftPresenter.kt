package com.qwert2603.spenddemo.draft

import com.qwert2603.andrlib.base.mvi.BasePresenter
import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.model.entity.CreatingRecord
import com.qwert2603.spenddemo.utils.secondOfTwo
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DraftPresenter @Inject constructor(
        private val draftInteractor: DraftInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : BasePresenter<DraftView, DraftViewState>(uiSchedulerProvider) {

    override val initialState = DraftViewState(
            creatingRecord = CreatingRecord(
                    kind = "",
                    value = 0,
                    date = Date(),
                    dateSet = false
            ),
            createEnable = false
    )

    private val loadDraft: Observable<CreatingRecord> = intent { it.viewCreated() }
            .switchMapSingle { draftInteractor.getDraft() }
            .share()

    private val clearDraft = PublishSubject.create<Any>()

    private val onDateSelectedIntent = intent { it.onDateSelected() }.share()
    private val kingChangesIntent = intent { it.kingChanges() }.share()
    private val onKindSelectedIntent = intent { it.onKindSelected() }.share()
    private val onKindSuggestionSelectedIntent = intent { it.onKindSuggestionSelected() }.share()

    private val draftChanges: Observable<CreatingRecord> = Observable
            .merge(
                    loadDraft
                            .map { draft ->
                                { _: CreatingRecord -> draft }
                            },
                    Observable
                            .merge(listOf(
                                    onDateSelectedIntent
                                            .map { date ->
                                                { r: CreatingRecord -> r.copy(date = date, dateSet = true) }
                                            },
                                    kingChangesIntent
                                            .map { kind ->
                                                { r: CreatingRecord -> r.copy(kind = kind) }
                                            },
                                    intent { it.valueChanges() }
                                            .map { value ->
                                                { r: CreatingRecord -> r.copy(value = value) }
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
                                                { r: CreatingRecord -> r.copy(kind = kind, value = lastPrice) }
                                            },
                                    clearDraft
                                            .map {
                                                { _: CreatingRecord -> initialState.creatingRecord }
                                            }
                            ))
                            .delaySubscription(loadDraft)
            )
            .scan(initialState.creatingRecord, { creatingRecord: CreatingRecord, change: (CreatingRecord) -> CreatingRecord ->
                change(creatingRecord)
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
                        intent { it.onKindInputFocused() }
                                .withLatestFrom(draftChanges, secondOfTwo())
                                .map { it.kind },
                        kingChangesIntent
                                .debounce(100, TimeUnit.MILLISECONDS)
                )
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
                .doOnNext { viewActions.onNext(DraftViewAction.AskToSelectDate(it.date.time)) }
                .subscribeToView()

        intent { it.selectKindClicks() }
                .doOnNext { viewActions.onNext(DraftViewAction.AskToSelectKind) }
                .subscribeToView()

        intent { it.saveClicks() }
                .withLatestFrom(draftChanges, secondOfTwo())
                .filter { draftInteractor.isCreatable(it) }
                .switchMapCompletable {
                    draftInteractor.createRecord(it)
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