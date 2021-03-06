package com.qwert2603.spend.records_list

import com.qwert2603.andrlib.base.mvi.BasePresenter
import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.spend.model.entity.*
import com.qwert2603.spend.utils.*
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class RecordsListPresenter(
        private val recordsListInteractor: RecordsListInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : BasePresenter<RecordsListView, RecordsListViewState>(uiSchedulerProvider) {

    override val initialState = RecordsListViewState(
            records = null,
            diff = FastDiffUtils.FastDiffResult.EMPTY,
            showInfo = recordsListInteractor.showInfo.field,
            longSumPeriod = recordsListInteractor.longSumPeriod.field,
            shortSumPeriod = recordsListInteractor.shortSumPeriod.field,
            sumsInfo = SumsInfo.EMPTY,
            sortByValue = false,
            showFilters = false,
            searchQuery = "",
            startDate = null,
            endDate = null,
            recordsChanges = hashMapOf(),
            syncState = SyncState.Syncing,
            _selectedRecordsUuids = hashSetOf(),
            oldRecordsLock = true
    )

    private val showInfoChanges: Observable<ShowInfo> = recordsListInteractor.showInfo.changes.shareAfterViewSubscribed()
    private val longSumPeriodChanges: Observable<Days> = recordsListInteractor.longSumPeriod.changes.shareAfterViewSubscribed()
    private val shortSumPeriodChanges: Observable<Minutes> = recordsListInteractor.shortSumPeriod.changes.shareAfterViewSubscribed()

    private val sortByValueChanges: Observable<Boolean> = intent { it.sortByValueChanges() }.shareAfterViewSubscribed()

    private val recordsFiltersChanges = viewStateObservable
            .map { RecordsFilters(it.searchQuery, it.startDate, it.endDate) }
            .distinctUntilChanged()
            .shareAfterViewSubscribed()

    override val partialChanges: Observable<PartialChange> = Observable.merge(listOf(
            showInfoChanges
                    .map { RecordsListPartialChange.ShowInfoChanged(it) },
            sortByValueChanges
                    .doOnNext { viewActions.onNext(RecordsListViewAction.ScrollToTop) }
                    .map { RecordsListPartialChange.SortByValueChanged(it) },
            intent { it.showFiltersChanges() }
                    .map { RecordsListPartialChange.ShowFiltersChanged(it) },
            intent { it.searchQueryChanges() }
                    .debounce(230, TimeUnit.MILLISECONDS)
                    .map { RecordsListPartialChange.SearchQueryChanged(it) },
            intent { it.startDateSelected() }
                    .map { RecordsListPartialChange.StartDateChanged(it.t) },
            intent { it.endDateSelected() }
                    .map { RecordsListPartialChange.EndDateChanged(it.t) },
            Observable
                    .combineLatest(
                            showInfoChanges,
                            sortByValueChanges.startWith(initialState.sortByValue),
                            viewStateObservable
                                    .map { it.showFilters }
                                    .distinctUntilChanged(),
                            longSumPeriodChanges,
                            shortSumPeriodChanges,
                            recordsFiltersChanges,
                            viewStateObservable
                                    .map { it.selectedRecordsUuids }
                                    .distinctUntilChanged(),
                            makeSeventuple()
                    )
                    .switchMap { seventuple ->
                        RxUtils.minuteChanges()
                                .startWith(Any())
                                .map { seventuple }
                    }
                    .switchMap { (showInfo, sortByValue, showFilters, longSumPeriodDays, shortSumPeriodMinutes, filters, selectedUuids) ->
                        recordsListInteractor.getRecordsList()
                                .map { it.toRecordItemsList(showInfo, sortByValue, showFilters, longSumPeriodDays, shortSumPeriodMinutes, filters, selectedUuids) }
                                .map { it to sortByValue }
                    }
                    .startWith(emptyList<RecordsListItem>() to false)
                    .buffer(2, 1)
                    .map { (prev, current) ->
                        val diffResult = if (prev.second == current.second) {
                            FastDiffUtils.fastCalculateDiff(
                                    oldList = prev.first,
                                    newList = current.first,
                                    id = RecordsListItem::idInList,
                                    compareOrder = if (current.second /* sortByValue */) {
                                        RecordsListItem.COMPARE_ORDER_WITH_VALUE
                                    } else {
                                        RecordsListItem.COMPARE_ORDER
                                    },
                                    isEqual = RecordsListItem.IS_EQUAL
                            )
                        } else {
                            FastDiffUtils.FastDiffResult(
                                    removes = listOf(0 to prev.first.size),
                                    inserts = listOf(0 to current.first.size),
                                    changes = emptyList()
                            )
                        }
                        val recordsChanges = hashMapOf<String, RecordChange>()
                        current.first.forEach {
                            if (it is Record && it.change != null) {
                                recordsChanges[it.uuid] = it.change
                            }
                        }
                        RecordsListPartialChange.RecordsListChanged(
                                list = current.first,
                                diff = diffResult,
                                recordsChanges = recordsChanges
                        )
                    },
            longSumPeriodChanges
                    .map { RecordsListPartialChange.LongSumPeriodChanged(it) },
            shortSumPeriodChanges
                    .map { RecordsListPartialChange.ShortSumPeriodChanged(it) },
            sumsInfoChanges(
                    longSumPeriodChanges,
                    shortSumPeriodChanges,
                    showInfoChanges,
                    recordsFiltersChanges,
                    recordsListInteractor
            ).map { RecordsListPartialChange.SumsInfoChanged(it) },
            recordsListInteractor
                    .getSyncState()
                    .modifyForUi()
                    .map { RecordsListPartialChange.SyncStateChanged(it) },
            intent { it.recordLongClicks() }
                    .filter { !it.isDeleted() }
                    .map { RecordsListPartialChange.ToggleRecordSelection(it.uuid) },
            intent { it.recordClicks() }
                    .filter { !it.isDeleted() }
                    .withLatestFrom(viewStateObservable, makePair())
                    .filter { (record, vs) ->
                        if (!vs.selectMode && record.isChangeable(vs.oldRecordsLock)) {
                            viewActions.onNext(RecordsListViewAction.AskForRecordActions(record.uuid))
                        }
                        vs.selectMode
                    }
                    .map { (record, _) -> RecordsListPartialChange.ToggleRecordSelection(record.uuid) },
            intent { it.cancelSelection() }
                    .map { RecordsListPartialChange.ClearSelection },
            recordsListInteractor
                    .oldRecordsLockStateChanges()
                    .map { RecordsListPartialChange.OldRecordsLockStateChanged(it) }
    ))

    override fun stateReducer(vs: RecordsListViewState, change: PartialChange): RecordsListViewState {
        if (change !is RecordsListPartialChange) throw Exception()
        return when (change) {
            is RecordsListPartialChange.RecordsListChanged -> vs.copy(
                    records = change.list,
                    diff = change.diff,
                    recordsChanges = change.recordsChanges
            )
            is RecordsListPartialChange.ShowInfoChanged -> vs.copy(showInfo = change.showInfo)
            is RecordsListPartialChange.SumsInfoChanged -> vs.copy(sumsInfo = change.sumsInfo)
            is RecordsListPartialChange.SortByValueChanged -> vs.copy(sortByValue = change.sortByValue)
            is RecordsListPartialChange.LongSumPeriodChanged -> vs.copy(longSumPeriod = change.days)
            is RecordsListPartialChange.ShortSumPeriodChanged -> vs.copy(shortSumPeriod = change.minutes)
            is RecordsListPartialChange.ShowFiltersChanged -> {
                if (change.show) {
                    vs.copy(showFilters = true)
                } else {
                    vs.copy(showFilters = false, searchQuery = "", startDate = null, endDate = null)
                }
            }
            is RecordsListPartialChange.SyncStateChanged -> vs.copy(syncState = change.syncState)
            is RecordsListPartialChange.ToggleRecordSelection -> vs.copy(_selectedRecordsUuids = vs.selectedRecordsUuids.toggle(change.recordUuid))
            RecordsListPartialChange.ClearSelection -> vs.copy(_selectedRecordsUuids = hashSetOf())
            is RecordsListPartialChange.SearchQueryChanged -> vs.copy(searchQuery = change.search)
            is RecordsListPartialChange.StartDateChanged -> vs.copy(
                    startDate = change.startDate?.coerceAtMost(vs.endDate ?: SDate.MAX_VALUE)
            )
            is RecordsListPartialChange.EndDateChanged -> vs.copy(
                    endDate = change.endDate?.coerceAtLeast(vs.startDate ?: SDate.MIN_VALUE)
            )
            is RecordsListPartialChange.OldRecordsLockStateChanged -> vs.copy(oldRecordsLock = change.oldRecordsLockState.isLocked)
        }
    }

    override fun bindIntents() {

        Observable
                .merge(listOf(
                        intent { it.showSpendsChanges() }.map { ShowInfoChange.Spends(it) },
                        intent { it.showProfitsChanges() }.map { ShowInfoChange.Profits(it) },
                        intent { it.showSumsChanges() }.map { ShowInfoChange.Sums(it) },
                        intent { it.showChangeKindsChanges() }.map { ShowInfoChange.ChangeKinds(it) },
                        intent { it.showTimesChanges() }.map { ShowInfoChange.Times(it) }
                ))
                .doOnNext { ch ->
                    recordsListInteractor.showInfo.updateField { info ->
                        return@updateField when (ch) {
                            is ShowInfoChange.Spends -> info.copy(showSpends = ch.show)
                            is ShowInfoChange.Profits -> info.copy(showProfits = ch.show)
                            is ShowInfoChange.Sums -> info.copy(showSums = ch.show)
                            is ShowInfoChange.ChangeKinds -> info.copy(showChangeKinds = ch.show)
                            is ShowInfoChange.Times -> info.copy(showTimes = ch.show)
                        }
                    }
                }
                .subscribeToView()

        intent { it.longSumPeriodSelected() }
                .doOnNext { recordsListInteractor.longSumPeriod.field = it }
                .subscribeToView()

        intent { it.shortSumPeriodSelected() }
                .doOnNext { recordsListInteractor.shortSumPeriod.field = it }
                .subscribeToView()

        intent { it.createProfitClicks() }
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToCreateRecord(Const.RECORD_TYPE_ID_PROFIT)) }
                .subscribeToView()

        intent { it.chooseLongSumPeriodClicks() }
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToChooseLongSumPeriod(it.longSumPeriod)) }
                .subscribeToView()

        intent { it.chooseShortSumPeriodClicks() }
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .doOnNext { viewActions.onNext(RecordsListViewAction.AskToChooseShortSumPeriod(it.shortSumPeriod)) }
                .subscribeToView()

        intent { it.addStubRecordsClicks() }
                .flatMapSingle {
                    Single.zip(
                            recordsListInteractor.getRecordCategories(Const.RECORD_TYPE_ID_SPEND),
                            recordsListInteractor.getRecordCategories(Const.RECORD_TYPE_ID_PROFIT),
                            makePair()
                    )
                }
                .doOnNext { (spendCategories, profitCategories) ->
                    val stubSpendKinds = mapOf(
                            "проезд" to listOf("трамвай", "автобус", "троллейбус", "метро"),
                            "еда" to listOf("шоколадка", "столовая", "фрукты", "рыба", "мясо", "овощи", "хлеб"),
                            "развлечения" to listOf("кино", "батут", "кафе", "картинг"),
                            "медицина" to listOf("стоматолог", "анализы", "лор"),
                            "без категории" to listOf("пакет", "банки", "подарки")
                    )

                    val stubProfitKinds = mapOf(
                            "работа" to listOf("зарплата", "аванс", "отпускные"),
                            "институт" to listOf("стипендия"),
                            "без категории" to listOf("зарплата", "фриланс")
                    )

                    val spends = if (spendCategories.isNotEmpty()) {
                        (1..2000).map {
                            val recordCategory = spendCategories.random()
                            RecordDraft(
                                    isNewRecord = true,
                                    uuid = UUID.randomUUID().toString(),
                                    recordTypeId = recordCategory.recordTypeId,
                                    recordCategoryName = recordCategory.name,
                                    recordCategoryUuid = recordCategory.uuid,
                                    date = Calendar.getInstance().also { it.add(Calendar.DAY_OF_MONTH, -Random.nextInt(5000)) }.toSDate(),
                                    time = Calendar.getInstance().also { it.add(Calendar.MINUTE, -Random.nextInt(1440)) }.toSTime().takeIf { Random.nextBoolean() },
                                    kind = stubSpendKinds.getValue(recordCategory.name).random(),
                                    value = Random.nextInt(1, 10000)
                            )
                        }
                    } else {
                        emptyList()
                    }
                    val profits = if (profitCategories.isNotEmpty()) {
                        (1..300).map {
                            val recordCategory = profitCategories.random()
                            RecordDraft(
                                    isNewRecord = true,
                                    uuid = UUID.randomUUID().toString(),
                                    recordTypeId = recordCategory.recordTypeId,
                                    recordCategoryName = recordCategory.name,
                                    recordCategoryUuid = recordCategory.uuid,
                                    date = Calendar.getInstance().also { it.add(Calendar.DAY_OF_MONTH, -Random.nextInt(5000)) }.toSDate(),
                                    time = Calendar.getInstance().also { it.add(Calendar.MINUTE, -Random.nextInt(1440)) }.toSTime().takeIf { Random.nextBoolean() },
                                    kind = stubProfitKinds.getValue(recordCategory.name).random(),
                                    value = Random.nextInt(1, 10000)
                            )
                        }
                    } else {
                        emptyList()
                    }
                    recordsListInteractor.addRecords(spends + profits)
                }
                .subscribeToView()

        intent { it.clearAllClicks() }
                .doOnNext { recordsListInteractor.removeAllRecords() }
                .subscribeToView()

        RxUtils.dateChanges()
                .doOnNext {
                    // we need to rerender all because old view holders with old list items
                    // in RecyclerView will not be redrawn if just to set list with old items.
                    viewActions.onNext(RecordsListViewAction.RerenderAll)
                }
                .subscribeToView()

        recordsListInteractor.getRecordCreatedLocallyEvents()
                .doOnNext { viewActions.onNext(RecordsListViewAction.OnRecordCreatedLocally(it)) }
                .subscribeToView()

        recordsListInteractor.getRecordEditedLocallyEvents()
                .doOnNext { viewActions.onNext(RecordsListViewAction.OnRecordEditedLocally(it)) }
                .subscribeToView()

        recordsListInteractor.getRecordCombinedLocallyEvents()
                .doOnNext { viewActions.onNext(RecordsListViewAction.OnRecordCombinedLocally(it)) }
                .subscribeToView()

        intent { it.combineSelectedClicks() }
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .doOnNext { vs ->
                    // combined records will be marked as deleted (and automatically deselected).
                    vs.createCombineAction()?.also { viewActions.onNext(it) }
                }
                .subscribeToView()

        intent { it.deleteSelectedClicks() }
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .doOnNext { vs ->
                    // records will be marked as deleted (and automatically deselected).
                    viewActions.onNext(RecordsListViewAction.AskToDeleteRecords(vs.selectedRecordsUuids.toList()))
                }
                .subscribeToView()

        intent { it.changeSelectedClicks() }
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .doOnNext { vs ->
                    viewActions.onNext(RecordsListViewAction.AskToChangeRecords(vs.selectedRecordsUuids.toList()))
                }
                .subscribeToView()

        intent { it.selectStartDateClicks() }
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .doOnNext {
                    viewActions.onNext(RecordsListViewAction.AskToSelectStartDate(
                            startDate = it.startDate ?: DateUtils.getNow().first,
                            maxDate = it.endDate
                    ))
                }
                .subscribeToView()

        intent { it.selectEndDateClicks() }
                .withLatestFrom(viewStateObservable, secondOfTwo())
                .doOnNext {
                    viewActions.onNext(RecordsListViewAction.AskToSelectEndDate(
                            endDate = it.endDate ?: DateUtils.getNow().first,
                            minDate = it.startDate
                    ))
                }
                .subscribeToView()

        super.bindIntents()
    }

    private sealed class ShowInfoChange {
        data class Spends(val show: Boolean) : ShowInfoChange()
        data class Profits(val show: Boolean) : ShowInfoChange()
        data class Sums(val show: Boolean) : ShowInfoChange()
        data class ChangeKinds(val show: Boolean) : ShowInfoChange()
        data class Times(val show: Boolean) : ShowInfoChange()
    }
}