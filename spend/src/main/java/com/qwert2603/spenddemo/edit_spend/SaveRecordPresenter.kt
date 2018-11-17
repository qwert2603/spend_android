package com.qwert2603.spenddemo.edit_spend

import com.qwert2603.andrlib.base.mvi.BasePresenter
import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.model.entity.RecordDraft
import com.qwert2603.spenddemo.model.entity.toRecordDraft
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SaveRecordPresenter @Inject constructor(
        private val saveRecordKey: SaveRecordKey,
        private val saveRecordInteractor: SaveRecordInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : BasePresenter<SaveRecordView, SaveRecordViewState>(uiSchedulerProvider) {

    override val initialState by lazy {
        val editingRecord by lazy {
            val uuid = (saveRecordKey as SaveRecordKey.EditRecord).uuid
            saveRecordInteractor
                    .getRecordChanges(uuid)
                    .blockingFirst()
                    .t!!
                    .toRecordDraft()
        }

        SaveRecordViewState(
                isNewRecord = saveRecordKey is SaveRecordKey.NewRecord,
                recordDraft = when (saveRecordKey) {
                    is SaveRecordKey.EditRecord -> editingRecord
                    is SaveRecordKey.NewRecord -> saveRecordInteractor.getDraft(saveRecordKey.recordTypeId)
                            ?: RecordDraft.new(saveRecordKey.recordTypeId)
                },
                serverKind = null,
                serverDate = null,
                serverTime = null,
                serverValue = null,
                justChangedOnServer = false,
                existingRecord = when (saveRecordKey) {
                    is SaveRecordKey.EditRecord -> editingRecord
                    is SaveRecordKey.NewRecord -> null
                }
        )
    }

    private val serverRecordChanges: Observable<Wrapper<Record>> = intent { it.viewCreated() }
            .switchMap {
                when (saveRecordKey) {
                    is SaveRecordKey.EditRecord -> saveRecordInteractor.getRecordChanges(saveRecordKey.uuid)
                    is SaveRecordKey.NewRecord -> Observable.never()
                }
            }
            .shareAfterViewSubscribed()

    override val partialChanges: Observable<PartialChange> = Observable.merge(listOf(
            serverRecordChanges
                    .mapNotNull { it.t }
                    .shareReplayLast()
                    .let { recordChanges ->
                        fun <T> serverPartialChanges(
                                mapper: (Record) -> T,
                                partialChangesCreator: (T) -> PartialChange
                        ): Observable<PartialChange> = recordChanges
                                .map(mapper)
                                .distinctUntilChanged()
                                .skip(1)
                                .map(partialChangesCreator)

                        Observable.merge(
                                serverPartialChanges({ it.kind }, { SaveRecordPartialChange.KindChangeOnServer(it) }),
                                serverPartialChanges({ it.value }, { SaveRecordPartialChange.ValueChangeOnServer(it) }),
                                serverPartialChanges({ it.date }, { SaveRecordPartialChange.DateChangeOnServer(it) }),
                                serverPartialChanges({ it.time.wrap() }, { SaveRecordPartialChange.TimeChangeOnServer(it.t) })
                        )
                    },
            serverRecordChanges
                    .mapNotNull { it.t }
                    .distinctUntilChanged()
                    .skip(1)
                    .switchMap {
                        Observable.interval(0, 700, TimeUnit.MILLISECONDS)
                                .take(2)
                                .map { it == 0L }
                    }
                    .map { SaveRecordPartialChange.RecordJustChangedOnServer(it) },
            serverRecordChanges
                    .skip(1)
                    .mapNotNull { it.t }
                    .map { SaveRecordPartialChange.ExistingRecordChanged(it.toRecordDraft()) },
            intent { it.onServerKindResolved() }
                    .map { SaveRecordPartialChange.KindServerResolved(it) },
            intent { it.onServerDateResolved() }
                    .map { SaveRecordPartialChange.DateServerResolved(it) },
            intent { it.onServerTimeResolved() }
                    .map { SaveRecordPartialChange.TimeServerResolved(it) },
            intent { it.onServerValueResolved() }
                    .map { SaveRecordPartialChange.ValueServerResolved(it) },
            intent { it.kindChanges() }
                    .map { SaveRecordPartialChange.KindChanged(it) },
            intent { it.valueChanges() }
                    .map { SaveRecordPartialChange.ValueChanged(it) },
            intent { it.onKindSelected() }
                    .doOnNext { viewActions.onNext(SaveRecordViewAction.FocusOnValueInput) }
                    .map { SaveRecordPartialChange.KindSelected(it) },
            intent { it.onDateSelected() }
                    .doOnNext { viewActions.onNext(SaveRecordViewAction.FocusOnKindInput) }
                    .map { SaveRecordPartialChange.DateSelected(it.t) },
            intent { it.onTimeSelected() }
                    .doOnNext { viewActions.onNext(SaveRecordViewAction.FocusOnKindInput) }
                    .map { SaveRecordPartialChange.TimeSelected(it.t) }
    ))

    override fun stateReducer(vs: SaveRecordViewState, change: PartialChange): SaveRecordViewState {
        if (change !is SaveRecordPartialChange) null!!
        return when (change) {
            is SaveRecordPartialChange.KindChanged -> vs.copy(recordDraft = vs.recordDraft.copy(kind = change.kind))
            is SaveRecordPartialChange.ValueChanged -> vs.copy(recordDraft = vs.recordDraft.copy(value = change.value))
            is SaveRecordPartialChange.KindSelected -> vs.copy(recordDraft = vs.recordDraft.copy(kind = change.kind))
            is SaveRecordPartialChange.DateSelected -> vs.copy(recordDraft = vs.recordDraft.copy(date = change.date))
            is SaveRecordPartialChange.TimeSelected -> vs.copy(recordDraft = vs.recordDraft.copy(time = change.time))
            is SaveRecordPartialChange.KindChangeOnServer -> vs.copy(serverKind = change.kind)
            is SaveRecordPartialChange.ValueChangeOnServer -> vs.copy(serverValue = change.value)
            is SaveRecordPartialChange.DateChangeOnServer -> vs.copy(serverDate = change.date)
            is SaveRecordPartialChange.TimeChangeOnServer -> vs.copy(serverTime = change.time.wrap())
            is SaveRecordPartialChange.RecordJustChangedOnServer -> vs.copy(justChangedOnServer = change.justChanged)
            is SaveRecordPartialChange.ExistingRecordChanged -> vs.copy(existingRecord = change.existingRecord)
            is SaveRecordPartialChange.KindServerResolved -> vs.copy(
                    recordDraft = if (change.acceptFromServer && vs.serverKind != null) vs.recordDraft.copy(kind = vs.serverKind) else vs.recordDraft,
                    serverKind = null
            )
            is SaveRecordPartialChange.ValueServerResolved -> vs.copy(
                    recordDraft = if (change.acceptFromServer && vs.serverValue != null) vs.recordDraft.copy(value = vs.serverValue) else vs.recordDraft,
                    serverValue = null
            )
            is SaveRecordPartialChange.DateServerResolved -> vs.copy(
                    recordDraft = if (change.acceptFromServer && vs.serverDate != null) vs.recordDraft.copy(date = vs.serverDate) else vs.recordDraft,
                    serverDate = null
            )
            is SaveRecordPartialChange.TimeServerResolved -> vs.copy(
                    recordDraft = if (change.acceptFromServer && vs.serverTime != null) vs.recordDraft.copy(time = vs.serverTime.t) else vs.recordDraft,
                    serverTime = null
            )
        }
    }

    override fun bindIntents() {
        if (saveRecordKey is SaveRecordKey.NewRecord) {
            viewStateObservable
                    .doOnNext { saveRecordInteractor.saveDraft(saveRecordKey.recordTypeId, it.recordDraft) }
                    .subscribeToView()
        }

        intent { it.selectKindClicks() }
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .doOnNext { viewActions.onNext(SaveRecordViewAction.AskToSelectKind(it.recordDraft.recordTypeId)) }
                .subscribeToView()

        intent { it.selectDateClicks() }
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .doOnNext {
                    viewActions.onNext(SaveRecordViewAction.AskToSelectDate(
                            it.recordDraft.date ?: DateUtils.getNow().first
                    ))
                }
                .subscribeToView()

        intent { it.selectTimeClicks() }
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .doOnNext {
                    viewActions.onNext(SaveRecordViewAction.AskToSelectTime(
                            it.recordDraft.time ?: DateUtils.getNow().second
                    ))
                }
                .subscribeToView()

        serverRecordChanges
                .buffer(2, 1)
                .mapNotNull { (prev, current) ->
                    if (prev.t != null && current.t == null) {
                        prev.t.recordTypeId
                    } else {
                        null
                    }
                }
                .take(1)
                .doOnNext { viewActions.onNext(SaveRecordViewAction.EditingRecordDeletedOnServer(it)) }
                .subscribeToView()

        intent { it.saveClicks() }
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .doOnNext {
                    saveRecordInteractor.saveRecord(it.recordDraft)
                    viewActions.onNext(SaveRecordViewAction.Close)
                }
                .subscribeToView()

        RxUtils.dateChanges()
                .doOnNext { viewActions.onNext(SaveRecordViewAction.RerenderAll) }
                .subscribeToView()

        super.bindIntents()
    }
}