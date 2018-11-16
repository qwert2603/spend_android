package com.qwert2603.spenddemo.spend_draft

import com.qwert2603.andrlib.base.mvi.BasePresenter
import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.model.entity.RecordDraft
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DraftPresenter @Inject constructor(
        private val draftInteractor: DraftInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : BasePresenter<DraftView, DraftViewState>(uiSchedulerProvider) {

    override val initialState = DraftViewState(
            draftInteractor.getDraft() ?: RecordDraft.new(Const.RECORD_TYPE_ID_SPEND)
    )

    private val clearDraft = PublishSubject.create<Any>()

    private val onDateSelectedIntent = intent { it.onDateSelected() }.shareAfterViewSubscribed()
    private val onTimeSelectedIntent = intent { it.onTimeSelected() }.shareAfterViewSubscribed()
    private val kingChangesIntent = intent { it.kingChanges() }.shareAfterViewSubscribed()
    private val onKindSelectedIntent = intent { it.onKindSelected() }.shareAfterViewSubscribed()
    private val onKindSuggestionSelectedIntent = intent { it.onKindSuggestionSelected() }.shareAfterViewSubscribed()

    override val partialChanges: Observable<PartialChange> = Observable.merge(listOf(
            onDateSelectedIntent
                    .map { DraftPartialChange.DateSelected(it.t) },
            onTimeSelectedIntent
                    .map { DraftPartialChange.TimeSelected(it.t) },
            kingChangesIntent
                    .map { DraftPartialChange.KindChanged(it) },
            intent { it.valueChanges() }
                    .map { DraftPartialChange.ValueChanged(it) },
            Observable
                    .merge(
                            onKindSelectedIntent,
                            onKindSuggestionSelectedIntent
                    )
                    .switchMapSingle { kind ->
                        draftInteractor.getLastValueOfKind(kind)
                                .map { Pair(kind, it) }
                    }
                    .map { (kind, lastValue) ->
                        DraftPartialChange.KindSelected(kind, lastValue)
                    },
            clearDraft
                    .map { DraftPartialChange.DraftCleared }
    ))


    override fun stateReducer(vs: DraftViewState, change: PartialChange): DraftViewState {
        if (change !is DraftPartialChange) throw Exception()
        return when (change) {
            is DraftPartialChange.DateSelected -> {
                val (nowDate, nowTime) = DateUtils.getNow()
                if (vs.recordDraft.date == null && change.date == nowDate) {
                    vs.copy(recordDraft = vs.recordDraft.copy(
                            date = change.date,
                            time = nowTime
                    ))
                } else {
                    vs.copy(recordDraft = vs.recordDraft.copy(
                            date = change.date,
                            time = if (change.date != null) vs.recordDraft.time else null
                    ))
                }
            }
            is DraftPartialChange.TimeSelected -> vs.copy(recordDraft = vs.recordDraft.copy(time = change.time.takeIf { vs.recordDraft.date != null }))
            is DraftPartialChange.KindChanged -> vs.copy(recordDraft = vs.recordDraft.copy(kind = change.kind))
            is DraftPartialChange.KindSelected -> vs.copy(recordDraft = vs.recordDraft.copy(kind = change.kind, value = change.lastValue))
            is DraftPartialChange.ValueChanged -> vs.copy(recordDraft = vs.recordDraft.copy(value = change.value))
            DraftPartialChange.DraftCleared -> vs.copy(recordDraft = RecordDraft.new(Const.RECORD_TYPE_ID_SPEND))
        }
    }

    override fun bindIntents() {
        super.bindIntents()

        viewStateObservable
                .skip(1) // skip initial value.
                .doOnNext { draftInteractor.saveDraft(it.recordDraft) }
                .subscribeToView()

        Observable
                .merge(
                        intent { it.onKindInputClicked() }
                                .withLatestFrom(viewStateObservable, secondOfTwo())
                                .mapNotNull { it.recordDraft }
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
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .mapNotNull { it.recordDraft }
                .doOnNext {
                    viewActions.onNext(DraftViewAction.AskToSelectDate(
                            it.date ?: DateUtils.getNow().first
                    ))
                }
                .subscribeToView()

        intent { it.selectTimeClicks() }
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .mapNotNull { it.recordDraft }
                .doOnNext {
                    viewActions.onNext(DraftViewAction.AskToSelectTime(
                            it.time ?: DateUtils.getNow().second
                    ))
                }
                .subscribeToView()

        intent { it.selectKindClicks() }
                .doOnNext { viewActions.onNext(DraftViewAction.AskToSelectKind) }
                .subscribeToView()

        intent { it.saveClicks() }
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .mapNotNull { it.recordDraft }
                .filter { it.isValid() }
                .switchMapCompletable {
                    draftInteractor.createRecord(it)
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

        RxUtils.dateChanges()
                .doOnNext { viewActions.onNext(DraftViewAction.RerenderAll) }
                .subscribeToView()
    }
}