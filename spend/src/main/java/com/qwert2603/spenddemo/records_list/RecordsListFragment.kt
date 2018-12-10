package com.qwert2603.spenddemo.records_list

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.view.animation.AnimationUtils
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView
import com.qwert2603.andrlib.base.mvi.BaseFragment
import com.qwert2603.andrlib.base.mvi.ViewAction
import com.qwert2603.andrlib.util.Const
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.dialogs.*
import com.qwert2603.spenddemo.env.E
import com.qwert2603.spenddemo.model.entity.*
import com.qwert2603.spenddemo.navigation.KeyboardManager
import com.qwert2603.spenddemo.records_list.vh.DaySumViewHolder
import com.qwert2603.spenddemo.save_record.SaveRecordDialogFragmentBuilder
import com.qwert2603.spenddemo.save_record.SaveRecordKey
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_records_list.*
import kotlinx.android.synthetic.main.toolbar_default.*
import java.util.concurrent.TimeUnit

class RecordsListFragment : BaseFragment<RecordsListViewState, RecordsListView, RecordsListPresenter>(), RecordsListView {

    companion object {
        private const val REQUEST_CHOOSE_LONG_SUM_PERIOD = 6
        private const val REQUEST_CHOOSE_SHORT_SUM_PERIOD = 7
    }

    override fun createPresenter() = DIHolder.diManager.presentersCreatorComponent
            .recordsListPresenterCreatorComponent()
            .build()
            .createRecordsListPresenter()

    private var layoutAnimationShown by BundleBoolean("layoutAnimationShown", { arguments!! }, false)

    private val adapter: RecordsListAdapter get() = records_RecyclerView.adapter as RecordsListAdapter
    private val itemAnimator: RecordsListAnimator get() = records_RecyclerView.itemAnimator as RecordsListAnimator

    private val menuHolder = MenuHolder()

    private val longSumPeriodDaysSelected = PublishSubject.create<Int>()
    private val shortSumPeriodMinutesSelected = PublishSubject.create<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments = arguments ?: Bundle()
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_records_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        records_RecyclerView.adapter = RecordsListAdapter()
        records_RecyclerView.recycledViewPool.setMaxRecycledViews(RecordsListAdapter.VIEW_TYPE_RECORD, 30)
        records_RecyclerView.recycledViewPool.setMaxRecycledViews(RecordsListAdapter.VIEW_TYPE_DATE_SUM, 20)
        records_RecyclerView.addItemDecoration(ConditionDividerDecoration(requireContext()) { rv, vh, _ ->
            vh.adapterPosition > 0 && rv.findViewHolderForAdapterPosition(vh.adapterPosition - 1) is DaySumViewHolder
        })
        records_RecyclerView.itemAnimator = RecordsListAnimator(createSpendViewImpl)

        createSpendViewImpl.dialogShower = object : DialogAwareView.DialogShower {
            override fun showDialog(dialogFragment: DialogFragment, requestCode: Int) {
                dialogFragment
                        .makeShow(requestCode)
            }
        }

        Observable
                .combineLatest(
                        RxRecyclerView.scrollEvents(records_RecyclerView)
                                .switchMap {
                                    Observable.interval(0, 1, TimeUnit.SECONDS)
                                            .take(2)
                                            .map { it == 0L }
                                }
                                .distinctUntilChanged()
                                .observeOn(AndroidSchedulers.mainThread()),
                        RxRecyclerView.scrollEvents(records_RecyclerView)
                                .map {
                                    val lastVisiblePosition = (records_RecyclerView.layoutManager as LinearLayoutManager)
                                            .findLastVisibleItemPosition()
                                    val lastIsTotal = lastVisiblePosition != RecyclerView.NO_POSITION
                                            && currentViewState.records[lastVisiblePosition] is Totals
                                    return@map !lastIsTotal
                                },
                        Boolean::and.toRxBiFunction()
                )
                .subscribe { showFromScroll ->
                    val showFloatingDate = currentViewState.showInfo.showFloatingDate()
                    val records = currentViewState.records
                    floatingDate_TextView.setVisible(showFromScroll && showFloatingDate && records.any { it is DaySum })
                }
                .disposeOnDestroyView()
        RxRecyclerView.scrollEvents(records_RecyclerView)
                .subscribe {
                    if (!currentViewState.showInfo.showFloatingDate()) return@subscribe
                    var i = (records_RecyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                    val floatingCenter = floatingDate_TextView.getGlobalVisibleRectRightNow().centerY()
                    val viewHolder = records_RecyclerView.findViewHolderForAdapterPosition(i)
                    if (viewHolder == null) {
                        floatingDate_TextView.text = ""
                        return@subscribe
                    }
                    val vhTop = viewHolder.itemView.getGlobalVisibleRectRightNow().top
                    if (i > 0 && vhTop < floatingCenter) --i
                    val records = currentViewState.records
                    if (i in 1..records.lastIndex && records[i] is Totals) --i
                    i = records.indexOfFirst(startIndex = i) { it is DaySum }
                    if (i >= 0) {
                        floatingDate_TextView.text = (records[i] as DaySum).day.toFormattedString(resources, stringMonth = true)
                    } else {
                        floatingDate_TextView.text = ""
                    }
                }
                .disposeOnDestroyView()

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        createSpendViewImpl.onDialogResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_CHOOSE_LONG_SUM_PERIOD -> longSumPeriodDaysSelected.onNext(data.getIntExtra(ChooseLongSumPeriodDialog.DAYS_KEY, 0))
                REQUEST_CHOOSE_SHORT_SUM_PERIOD -> shortSumPeriodMinutesSelected.onNext(data.getIntExtra(ChooseShortSumPeriodDialog.MINUTES_KEY, 0))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.records_list, menu)

        menu.findItem(R.id.show_change_kinds).isVisible = E.env.syncWithServer
        menu.findItem(R.id.add_stub_records).isVisible = E.env.buildForTesting()
        menu.findItem(R.id.clear_all).isVisible = E.env.buildForTesting()

        menuHolder.menu = menu
        renderAll()
    }

    override fun onDestroyOptionsMenu() {
        menuHolder.menu = null
        super.onDestroyOptionsMenu()
    }

    override fun recordClicks(): Observable<Record> = adapter.itemClicks.mapNotNull { it as? Record }
    override fun recordLongClicks(): Observable<Record> = adapter.itemLongClicks.mapNotNull { it as? Record }
    override fun createProfitClicks(): Observable<Any> = menuHolder.menuItemClicks(R.id.new_profit)
    override fun chooseLongSumPeriodClicks(): Observable<Any> = menuHolder.menuItemClicks(R.id.long_sum)
    override fun chooseShortSumPeriodClicks(): Observable<Any> = menuHolder.menuItemClicks(R.id.short_sum)

    override fun showSpendsChanges(): Observable<Boolean> = menuHolder.menuItemCheckedChanges(R.id.show_spends)
    override fun showProfitsChanges(): Observable<Boolean> = menuHolder.menuItemCheckedChanges(R.id.show_profits)
    override fun showSumsChanges(): Observable<Boolean> = menuHolder.menuItemCheckedChanges(R.id.show_sums)
    override fun showChangeKindsChanges(): Observable<Boolean> = menuHolder.menuItemCheckedChanges(R.id.show_change_kinds)
    override fun showTimesChanges(): Observable<Boolean> = menuHolder.menuItemCheckedChanges(R.id.show_times)

    override fun longSumPeriodDaysSelected(): Observable<Int> = longSumPeriodDaysSelected
    override fun shortSumPeriodMinutesSelected(): Observable<Int> = shortSumPeriodMinutesSelected

    override fun addStubRecordsClicks(): Observable<Any> = menuHolder.menuItemClicks(R.id.add_stub_records)
    override fun clearAllClicks(): Observable<Any> = menuHolder.menuItemClicks(R.id.clear_all)

    override fun render(vs: RecordsListViewState) {
        LogUtils.withErrorLoggingOnly { super.render(vs) }

        renderIfChanged({ showInfo.showChangeKinds }) { adapter.showChangeKinds = it }
        renderIfChanged({ showInfo.showTimes }) { adapter.showTimesInRecords = it }
        renderIfChanged({ showInfo.showSums }) { adapter.showDatesInRecords = !it }

        renderIfChangedWithFirstRendering({ records }) { records, firstRender ->
            adapter.recordsChanges = vs.recordsChanges
            adapter.list = records
            if (!layoutAnimationShown) {
                layoutAnimationShown = true
                records_RecyclerView.layoutAnimation = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down)
            }
            if (firstRender) {
                adapter.notifyDataSetChanged()
            } else {
                vs.diff.dispatchToAdapter(adapter)
            }
            val pendingCreatedRecordUuid = itemAnimator.pendingCreatedRecordUuid
            if (pendingCreatedRecordUuid != null) {
                val position = records.indexOfFirst { it is Record && it.uuid == pendingCreatedRecordUuid }
                if (position >= 0) {
                    records_RecyclerView.scrollToPosition(position)
                }
            }
        }

        renderIfChanged({ showInfo.newSpendVisible() }) {
            createSpendViewImpl.setVisible(it)
            if (!it) (requireActivity() as KeyboardManager).hideKeyboard()
        }

        menuHolder.menu?.also { menu ->
            renderIfChanged({ showInfo }) {
                menu.findItem(R.id.new_profit).isEnabled = it.newProfitEnable()

                menu.findItem(R.id.show_spends).isChecked = it.showSpends
                menu.findItem(R.id.show_profits).isChecked = it.showProfits
                menu.findItem(R.id.show_sums).isChecked = it.showSums
                menu.findItem(R.id.show_change_kinds).isChecked = it.showChangeKinds
                menu.findItem(R.id.show_times).isChecked = it.showTimes

                menu.findItem(R.id.show_spends).isEnabled = it.showSpendsEnable()
                menu.findItem(R.id.show_profits).isEnabled = it.showProfitsEnable()
            }

            renderIfChanged({ longSumPeriodDays }) {
                menu.findItem(R.id.long_sum).title = resources.getString(
                        R.string.menu_item_long_sum_format,
                        ChooseLongSumPeriodDialog.variantToString(it, resources)
                )
            }
            renderIfChanged({ shortSumPeriodMinutes }) {
                menu.findItem(R.id.short_sum).title = resources.getString(
                        R.string.menu_item_short_sum_format,
                        ChooseShortSumPeriodDialog.variantToString(it, resources)
                )
            }
        }

        renderIfChangedThree({ Triple(sumsInfo, longSumPeriodDays, shortSumPeriodMinutes) }) { (sumsInfo, longSumPeriodDays, shortSumPeriodMinutes) ->
            val longSumText = sumsInfo.longSum
                    ?.let {
                        resources.getString(
                                R.string.text_period_sum_format,
                                resources.formatTimeLetters(longSumPeriodDays * Const.MINUTES_PER_DAY),
                                it.toPointedString()
                        )
                    }
            val shortSumText = sumsInfo.shortSum
                    ?.let {
                        resources.getString(
                                R.string.text_period_sum_format,
                                resources.formatTimeLetters(shortSumPeriodMinutes),
                                it.toPointedString()
                        )
                    }
            val changesCountText = sumsInfo.changesCount
                    .takeIf { E.env.syncWithServer }
                    ?.let {
                        when (it) {
                            0 -> getString(R.string.no_changes_text)
                            else -> getString(R.string.changes_count_format, it)
                        }
                    }
            toolbar.subtitle = listOfNotNull(longSumText, shortSumText, changesCountText)
                    .reduceEmptyToNull { acc, s -> "$acc    $s" }
        }

        renderIfChanged({ syncState }) {
            toolbar.title = getString(R.string.app_name) + when (it) {
                SyncState.SYNCING -> ".."
                SyncState.SYNCED -> ""
                SyncState.ERROR -> " X"
            }
        }
    }

    override fun executeAction(va: ViewAction) {
        if (va !is RecordsListViewAction) null!!
        when (va) {
            is RecordsListViewAction.AskToCreateRecord -> SaveRecordDialogFragmentBuilder
                    .newSaveRecordDialogFragment(SaveRecordKey.NewRecord(va.recordTypeId))
                    .makeShow()
            is RecordsListViewAction.AskToEditRecord -> SaveRecordDialogFragmentBuilder
                    .newSaveRecordDialogFragment(SaveRecordKey.EditRecord(va.recordUuid))
                    .makeShow()
            is RecordsListViewAction.AskToDeleteRecord -> DeleteRecordDialogFragmentBuilder
                    .newDeleteRecordDialogFragment(va.recordUuid)
                    .makeShow()
            is RecordsListViewAction.AskToChooseLongSumPeriod -> ChooseLongSumPeriodDialogBuilder
                    .newChooseLongSumPeriodDialog(va.days)
                    .makeShow(REQUEST_CHOOSE_LONG_SUM_PERIOD)
            is RecordsListViewAction.AskToChooseShortSumPeriod -> ChooseShortSumPeriodDialogBuilder
                    .newChooseShortSumPeriodDialog(va.minutes)
                    .makeShow(REQUEST_CHOOSE_SHORT_SUM_PERIOD)
            is RecordsListViewAction.OnRecordCreatedLocally -> itemAnimator.pendingCreatedRecordUuid = va.uuid
            is RecordsListViewAction.OnRecordEditedLocally -> Unit
            RecordsListViewAction.RerenderAll -> renderAll()
        }.also { }
    }

    private fun DialogFragment.makeShow(requestCode: Int? = null) = this
            .also { if (requestCode != null) it.setTargetFragment(this@RecordsListFragment, requestCode) }
            .show(this@RecordsListFragment.fragmentManager, null)
            .also { (this@RecordsListFragment.context as KeyboardManager).hideKeyboard() }
}