package com.qwert2603.spenddemo.records_list_mvvm

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView
import com.qwert2603.andrlib.util.Const
import com.qwert2603.andrlib.util.addTo
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.dialogs.*
import com.qwert2603.spenddemo.env.E
import com.qwert2603.spenddemo.model.entity.CreatingProfit
import com.qwert2603.spenddemo.model.entity.Profit
import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.spenddemo.model.remote_db.RemoteDBImpl
import com.qwert2603.spenddemo.navigation.KeyboardManager
import com.qwert2603.spenddemo.records_list_mvvm.entity.*
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_records_list.*
import kotlinx.android.synthetic.main.toolbar_default.*
import kotlinx.android.synthetic.main.view_spend_draft.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class RecordsListMvvmFragment : Fragment() {

    companion object {
        private const val REQUEST_DELETE_SPEND = 1
        private const val REQUEST_EDIT_SPEND = 2
        private const val REQUEST_ADD_PROFIT = 3
        private const val REQUEST_EDIT_PROFIT = 4
        private const val REQUEST_DELETE_PROFIT = 5
        private const val REQUEST_CHOOSE_LONG_SUM_PERIOD = 6
        private const val REQUEST_CHOOSE_SHORT_SUM_PERIOD = 7
    }

    private val viewModel by lazy {
        ViewModelProviders.of(this, ViewModelFactory()).get(RecordsListViewModel::class.java)
    }

    val adapter = RecordsListAdapter()
    private val viewDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_records_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* todo: fast scroll.
        https://blog.stylingandroid.com/recyclerview-fastscroll-part-1/
        https://github.com/danoz73/RecyclerViewFastScroller
        https://github.com/timusus/RecyclerView-FastScrollll
        */

        /*todo
        выделение записей.
        отображение кол-ва и суммы выделенных записей,
        удаление выделенных с подтверждением
         */

        // todo: show sync_status in toolbar's subtitle.
        // and last full sync time if sync was more than 1 sec ago.

        records_RecyclerView.adapter = adapter
        records_RecyclerView.recycledViewPool.setMaxRecycledViews(RecordsListAdapter.VIEW_TYPE_SPEND, 20)
        records_RecyclerView.recycledViewPool.setMaxRecycledViews(RecordsListAdapter.VIEW_TYPE_PROFIT, 20)
        records_RecyclerView.recycledViewPool.setMaxRecycledViews(RecordsListAdapter.VIEW_TYPE_DATE_SUM, 20)
        records_RecyclerView.addItemDecoration(ConditionDividerDecoration(requireContext()) { rv, vh ->
            vh.adapterPosition > 0 && rv.findViewHolderForAdapterPosition(vh.adapterPosition - 1) is DateSumViewHolder
        })
        val recordsListAnimator = RecordsListAnimator(object : RecordsListAnimator.SpendOrigin {
            override fun getDateGlobalVisibleRect(): Rect = draftViewImpl.date_EditText.getGlobalVisibleRectRightNow()
            override fun getKindGlobalVisibleRect(): Rect = draftViewImpl.kind_EditText.getGlobalVisibleRectRightNow()
            override fun getValueGlobalVisibleRect(): Rect = draftViewImpl.value_EditText.getGlobalVisibleRectRightNow()
            override fun getTimeGlobalVisibleRect(): Rect? = draftViewImpl.time_EditText.getGlobalVisibleRectRightNow()
                    .takeIf { draftViewImpl.time_EditText.visibility == View.VISIBLE }
        })
        records_RecyclerView.itemAnimator = recordsListAnimator
        viewModel.createdSpendsEvents.observe(this, Observer { recordsListAnimator.pendingCreatedSpendId = it?.id })
        viewModel.createdProfitsEvents.observe(this, Observer { recordsListAnimator.pendingCreatedProfitId = it?.id })
        viewModel.syncingItemIdsInList.observe(this, Observer { adapter.syncingItemIdsInList = it ?: emptySet() })

        viewModel.creatingRecordsText.observe(this, Observer {
            Toast.makeText(requireContext(), R.string.text_dumping_records, Toast.LENGTH_SHORT).show()
        })

        var showFloatingDate = false
        var records = emptyList<RecordsListItem>()

        var layoutAnimationShown = false
        viewModel.recordsLiveData.observe(this, Observer {
            val (list, diffResult) = it ?: return@Observer
            adapter.list = list
            diffResult.dispatchToAdapter(adapter)// todo: check if it is ok after view's recreation.
            recordsListAnimator.pendingCreatedSpendId?.let { createdId ->
                list.indexOfFirst { it is SpendUI && it.id == createdId }
                        .let { records_RecyclerView.scrollToPosition(it) }
            }
            recordsListAnimator.pendingCreatedProfitId?.let { createdId ->
                list.indexOfFirst { it is ProfitUI && it.id == createdId }
                        .let { records_RecyclerView.scrollToPosition(it) }
            }
            records = list
            if (savedInstanceState == null && !layoutAnimationShown) {
                layoutAnimationShown = true
                val layoutAnimation = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down)
                records_RecyclerView.layoutAnimation = layoutAnimation
            }
        })
        viewModel.redrawAllRecords.observe(this, Observer { adapter.notifyDataSetChanged() })
        viewModel.showInfo.observe(this, Observer {
            if (it == null) return@Observer
            adapter.showDatesInRecords = !it.showDateSums
            draftViewImpl.setVisible(it.newSpendVisible())
            if (!it.newSpendVisible()) (context as KeyboardManager).hideKeyboard()
            showFloatingDate = it.showFloatingDate()
        })

        adapter.itemClicks = {
            when (it) {
                is SpendUI -> EditSpendDialogFragmentBuilder(it.date.time, it.id, it.kind, it.value)
                        .also { builder -> if (it.time != null) builder.time(it.time.time) }
                        .build()
                        .also { it.setTargetFragment(this, REQUEST_EDIT_SPEND) }
                        .makeShow()
                is ProfitUI -> AddProfitDialogFragmentBuilder(false)
                        .id(it.id)
                        .kind(it.kind)
                        .value(it.value)
                        .date(it.date.time)
                        .also { builder -> if (it.time != null) builder.time(it.time.time) }
                        .build()
                        .also { it.setTargetFragment(this, REQUEST_EDIT_PROFIT) }
                        .makeShow()
            }
        }
        adapter.itemLongClicks = {
            val timeFormat = SimpleDateFormat(com.qwert2603.spenddemo.utils.Const.TIME_FORMAT_PATTERN, Locale.getDefault())
            when (it) {
                is SpendUI -> DeleteSpendDialogFragmentBuilder
                        .newDeleteSpendDialogFragment(it.id,
                                it.date.toFormattedString(resources) +
                                        " ${it.time?.let { timeFormat.format(it) } ?: ""}" +
                                        "\n${it.kind}\n${it.value.toLong().toPointedString()}"
                        )
                        .also { it.setTargetFragment(this, REQUEST_DELETE_SPEND) }
                        .makeShow()
                is ProfitUI -> DeleteProfitDialogFragmentBuilder
                        .newDeleteProfitDialogFragment(it.id,
                                it.date.toFormattedString(resources) +
                                        " ${it.time?.let { timeFormat.format(it) } ?: ""}" +
                                        "\n${it.kind}\n${it.value.toLong().toPointedString()}"
                        )
                        .also { it.setTargetFragment(this, REQUEST_DELETE_PROFIT) }
                        .makeShow()
            }
        }

        viewModel.sumsInfo.observe(this, Observer { sumsInfo ->
            sumsInfo!!
            val changesCountText = sumsInfo.changesCount
                    .takeIf { E.env.syncWithServer }
                    ?.let {
                        when (it) {
                            0 -> getString(R.string.no_changes_text)
                            else -> getString(R.string.changes_count_format, it)
                        }
                    }
            val longSumText = sumsInfo.longPeriodDays
                    .takeIf { it > 0 }
                    ?.let { longPeriodDays ->
                        resources.getString(
                                R.string.text_period_sum_format,
                                resources.formatTimeLetters(longPeriodDays * Const.MINUTES_PER_DAY),
                                sumsInfo.longPeriodSum.toPointedString()
                        )
                    }
            val shortSumText = sumsInfo.shortPeriodMinutes
                    .takeIf { it > 0 }
                    ?.let { shortPeriodMinutes ->
                        resources.getString(
                                R.string.text_period_sum_format,
                                resources.formatTimeLetters(shortPeriodMinutes),
                                sumsInfo.shortPeriodSum.toPointedString()
                        )
                    }
            toolbar.subtitle = listOfNotNull(longSumText, shortSumText, changesCountText)
                    .reduceEmptyToNull { acc, s -> "$acc    $s" }
        })
        viewModel.showIds.observe(this, Observer { adapter.showIds = it == true })
        viewModel.showChangeKinds.observe(this, Observer { adapter.showChangeKinds = it == true })
        viewModel.showTimes.observe(this, Observer { adapter.showTimesInRecords = it == true })

        draftViewImpl.dialogShower = object : DialogAwareView.DialogShower {
            override fun showDialog(dialogFragment: DialogFragment, requestCode: Int) {
                dialogFragment
                        .also { it.setTargetFragment(this@RecordsListMvvmFragment, requestCode) }
                        .makeShow()
            }
        }

        toolbar.title = "${getString(R.string.app_name)} ${E.env.titleSuffix()}"

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
                                            && records[lastVisiblePosition] is TotalsUI
                                    return@map !lastIsTotal
                                },
                        Boolean::and.toRxBiFunction()
                )
                .subscribe {
                    floatingDate_TextView.setVisible(it && showFloatingDate && records.any { it is DateSumUI })
                }
                .addTo(viewDisposable)
        RxRecyclerView.scrollEvents(records_RecyclerView)
                .subscribe {
                    if (!showFloatingDate) return@subscribe
                    var i = (records_RecyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                    val floatingTop = floatingDate_TextView.getGlobalVisibleRectRightNow().centerY()
                    val vhTop = records_RecyclerView.findViewHolderForAdapterPosition(i).itemView.getGlobalVisibleRectRightNow().top
                    if (i > 0 && vhTop < floatingTop) --i
                    if (i in 1..records.lastIndex && records[i] is TotalsUI) --i
                    if (i in 1..records.lastIndex && records[i] is MonthSumUI) --i
                    i = records.indexOfFirst(startIndex = i) { it is DateSumUI }
                    if (i >= 0) {
                        floatingDate_TextView.text = (records[i] as DateSumUI).date.toFormattedString(resources)
                    } else {
                        floatingDate_TextView.text = ""
                    }
                }
                .addTo(viewDisposable)

        viewModel.sendRecords.observe(this, Observer { text ->
            if (text == null) return@Observer
            Intent(Intent.ACTION_SEND)
                    .also { it.putExtra(Intent.EXTRA_TEXT, text) }
                    .also { it.type = "text/plain" }
                    .let { Intent.createChooser(it, getString(R.string.send_title)) }
                    .apply { requireActivity().startActivity(this) }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewDisposable.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        draftViewImpl.onDialogResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_DELETE_SPEND -> viewModel.deleteSpend(data.getLongExtra(DeleteSpendDialogFragment.ID_KEY, 0))
                REQUEST_EDIT_SPEND -> viewModel.editSpend(Spend(
                        id = data.getLongExtra(EditSpendDialogFragment.ID_KEY, 0),
                        kind = data.getStringExtra(EditSpendDialogFragment.KIND_KEY),
                        value = data.getIntExtra(EditSpendDialogFragment.VALUE_KEY, 0),
                        date = Date(data.getLongExtra(EditSpendDialogFragment.DATE_KEY, 0L)),
                        time = data.getLongExtraNullable(EditSpendDialogFragment.TIME_KEY)?.let { Date(it) }
                ))
                REQUEST_ADD_PROFIT -> viewModel.addProfit(CreatingProfit(
                        kind = data.getStringExtra(AddProfitDialogFragment.KIND_KEY),
                        value = data.getIntExtra(AddProfitDialogFragment.VALUE_KEY, 0),
                        date = data.getLongExtraNullable(AddProfitDialogFragment.DATE_KEY)?.let { Date(it) },
                        time = data.getLongExtraNullable(AddProfitDialogFragment.TIME_KEY)?.let { Date(it) }
                ))
                REQUEST_EDIT_PROFIT -> viewModel.editProfit(Profit(
                        id = data.getLongExtra(AddProfitDialogFragment.ID_KEY, 0),
                        kind = data.getStringExtra(AddProfitDialogFragment.KIND_KEY),
                        value = data.getIntExtra(AddProfitDialogFragment.VALUE_KEY, 0),
                        date = Date(data.getLongExtra(AddProfitDialogFragment.DATE_KEY, 0L)),
                        time = data.getLongExtraNullable(AddProfitDialogFragment.TIME_KEY)?.let { Date(it) }
                ))
                REQUEST_DELETE_PROFIT -> viewModel.deleteProfit(data.getLongExtra(DeleteProfitDialogFragment.ID_KEY, 0))
                REQUEST_CHOOSE_LONG_SUM_PERIOD -> viewModel.setLongSumPeriodDays(data.getIntExtra(ChooseLongSumPeriodDialog.DAYS_KEY, 0))
                REQUEST_CHOOSE_SHORT_SUM_PERIOD -> viewModel.setShortSumPeriodMinutes(data.getIntExtra(ChooseShortSumPeriodDialog.MINUTES_KEY, 0))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.records_list, menu)

        val showSpendsMenuItem = menu.findItem(R.id.show_spends)
        val showProfitsMenuItem = menu.findItem(R.id.show_profits)
        val showDateSumsMenuItem = menu.findItem(R.id.show_date_sums)
        val showMonthSumsMenuItem = menu.findItem(R.id.show_month_sums)
        val showEmptySumsMenuItem = menu.findItem(R.id.show_empty_sums)
        val showIdsMenuItem = menu.findItem(R.id.show_ids)
        val showChangeKindsMenuItem = menu.findItem(R.id.show_change_kinds)
        val showTimesMenuItem = menu.findItem(R.id.show_times)
        val showDeletedMenuItem = menu.findItem(R.id.show_deleted)
        val longSumMenuItem = menu.findItem(R.id.long_sum)
        val shortSumMenuItem = menu.findItem(R.id.short_sum)

        showIdsMenuItem.isVisible = E.env.showIdsSetting
        showChangeKindsMenuItem.isVisible = E.env.showChangeKindsSetting
        menu.findItem(R.id.show_local_changes).isVisible = E.env.showChangeKindsSetting
        menu.findItem(R.id.add_stub_spends).isVisible = E.env.buildForTesting()
        menu.findItem(R.id.add_stub_profits).isVisible = E.env.buildForTesting()
        menu.findItem(R.id.clear_all).isVisible = E.env.buildForTesting()
        menu.findItem(R.id.server_delay).isVisible = E.env.buildForTesting() && E.env.syncWithServer
        showDeletedMenuItem.isVisible = E.env.syncWithServer
        menu.findItem(R.id.server_info).isVisible = E.env.syncWithServer

        viewModel.showSpends.observe(this, Observer { showSpendsMenuItem.isChecked = it == true })
        viewModel.showProfits.observe(this, Observer { showProfitsMenuItem.isChecked = it == true })
        viewModel.showDateSums.observe(this, Observer { showDateSumsMenuItem.isChecked = it == true })
        viewModel.showMonthSums.observe(this, Observer { showMonthSumsMenuItem.isChecked = it == true })
        viewModel.showEmptySums.observe(this, Observer { showEmptySumsMenuItem.isChecked = it == true })
        viewModel.showIds.observe(this, Observer { showIdsMenuItem.isChecked = it == true })
        viewModel.showChangeKinds.observe(this, Observer { showChangeKindsMenuItem.isChecked = it == true })
        viewModel.showTimes.observe(this, Observer { showTimesMenuItem.isChecked = it == true })
        viewModel.showDeleted.observe(this, Observer { showDeletedMenuItem.isChecked = it == true })
        viewModel.longSumPeriodDays.observe(this, Observer {
            longSumMenuItem.title = resources.getString(
                    R.string.long_sum_text_format,
                    ChooseLongSumPeriodDialog.variantToString(it!!, resources)
            )
        })
        viewModel.shortSumPeriodMinutes.observe(this, Observer {
            shortSumMenuItem.title = resources.getString(
                    R.string.short_sum_text_format,
                    ChooseShortSumPeriodDialog.variantToString(it!!, resources)
            )
        })

        showSpendsMenuItem.setOnMenuItemClickListener { viewModel.showSpends(!showSpendsMenuItem.isChecked);true }
        showProfitsMenuItem.setOnMenuItemClickListener { viewModel.showProfits(!showProfitsMenuItem.isChecked);true }
        showDateSumsMenuItem.setOnMenuItemClickListener { viewModel.showDateSums(!showDateSumsMenuItem.isChecked);true }
        showMonthSumsMenuItem.setOnMenuItemClickListener { viewModel.showMonthSums(!showMonthSumsMenuItem.isChecked);true }
        showEmptySumsMenuItem.setOnMenuItemClickListener { viewModel.showEmptySums(!showEmptySumsMenuItem.isChecked);true }
        showIdsMenuItem.setOnMenuItemClickListener { viewModel.showIds(!showIdsMenuItem.isChecked);true }
        showChangeKindsMenuItem.setOnMenuItemClickListener { viewModel.showChangeKinds(!showChangeKindsMenuItem.isChecked);true }
        showTimesMenuItem.setOnMenuItemClickListener { viewModel.showTimes(!showTimesMenuItem.isChecked);true }
        showDeletedMenuItem.setOnMenuItemClickListener { viewModel.showDeleted(!showDeletedMenuItem.isChecked);true }

        viewModel.showInfo.observe(this, Observer {
            if (it == null) return@Observer
            menu.findItem(R.id.new_profit).isEnabled = it.newProfitEnable()
            showSpendsMenuItem.isEnabled = it.showSpendsEnable()
            showProfitsMenuItem.isEnabled = it.showProfitsEnable()
            showDateSumsMenuItem.isEnabled = it.showDateSumsEnable()
            showMonthSumsMenuItem.isEnabled = it.showMonthSumsEnable()
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.new_profit -> AddProfitDialogFragmentBuilder.newAddProfitDialogFragment(true)
                    .also { it.setTargetFragment(this, REQUEST_ADD_PROFIT) }
                    .makeShow()
            R.id.add_stub_spends -> viewModel.addStubSpends()
            R.id.add_stub_profits -> viewModel.addStubProfits()
            R.id.about -> AppInfoDialogFragment().makeShow()
            R.id.clear_all -> viewModel.clearAll()
            R.id.send_records -> viewModel.sendRecords()
            R.id.show_local_changes -> viewModel.moveToChangesList()
            R.id.server_delay -> {
                RemoteDBImpl.IMITATE_DELAY = !RemoteDBImpl.IMITATE_DELAY
                item.isChecked = RemoteDBImpl.IMITATE_DELAY
            }
            R.id.short_sum -> ChooseShortSumPeriodDialogBuilder
                    .newChooseShortSumPeriodDialog(viewModel.shortSumPeriodMinutes.value!!)
                    .also { it.setTargetFragment(this, REQUEST_CHOOSE_SHORT_SUM_PERIOD) }
                    .makeShow()
            R.id.long_sum -> ChooseLongSumPeriodDialogBuilder
                    .newChooseLongSumPeriodDialog(viewModel.longSumPeriodDays.value!!)
                    .also { it.setTargetFragment(this, REQUEST_CHOOSE_LONG_SUM_PERIOD) }
                    .makeShow()
            R.id.server_info -> ServerInfoDialog().makeShow()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun DialogFragment.makeShow() = this
            .show(this@RecordsListMvvmFragment.fragmentManager, null)
            .also { (this@RecordsListMvvmFragment.context as KeyboardManager).hideKeyboard() }
}