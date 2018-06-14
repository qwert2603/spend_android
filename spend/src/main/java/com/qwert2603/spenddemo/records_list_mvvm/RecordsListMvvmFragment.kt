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
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView
import com.qwert2603.andrlib.util.addTo
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.dialogs.*
import com.qwert2603.spenddemo.env.E
import com.qwert2603.spenddemo.model.entity.CreatingProfit
import com.qwert2603.spenddemo.model.entity.Profit
import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.spenddemo.navigation.KeyboardManager
import com.qwert2603.spenddemo.records_list_mvvm.entity.*
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_records_list.*
import kotlinx.android.synthetic.main.toolbar_default.*
import kotlinx.android.synthetic.main.view_spend_draft.view.*
import java.util.*
import java.util.concurrent.TimeUnit

class RecordsListMvvmFragment : Fragment() {

    companion object {
        private const val REQUEST_DELETE_SPEND = 1
        private const val REQUEST_EDIT_SPEND = 2
        private const val REQUEST_ADD_PROFIT = 3
        private const val REQUEST_EDIT_PROFIT = 4
        private const val REQUEST_DELETE_PROFIT = 5
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
        records_RecyclerView.adapter = adapter
        records_RecyclerView.recycledViewPool.setMaxRecycledViews(RecordsListAdapter.VIEW_TYPE_SPEND, 20)
        records_RecyclerView.recycledViewPool.setMaxRecycledViews(RecordsListAdapter.VIEW_TYPE_PROFIT, 20)
        records_RecyclerView.recycledViewPool.setMaxRecycledViews(RecordsListAdapter.VIEW_TYPE_DATE_SUM, 20)
        records_RecyclerView.addItemDecoration(ConditionDividerDecoration(requireContext(), { rv, vh ->
            vh.adapterPosition > 0 && rv.findViewHolderForAdapterPosition(vh.adapterPosition - 1) is DateSumViewHolder
        }))
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

        viewModel.creatingRecordsText.observe(this, Observer { Toast.makeText(requireContext(), R.string.text_dumping_records, Toast.LENGTH_SHORT).show() })

        var showFloatingDate = false
        var records = emptyList<RecordsListItem>()

        var layoutAnimationShown = false
        viewModel.recordsLiveData.observe(this, Observer {
            if (it == null) return@Observer
            val (list, diffResult) = it
            adapter.list = list
            diffResult.dispatchToAdapter(adapter)
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
        viewModel.showInfo.observe(this, Observer {
            if (it == null) return@Observer
            adapter.showDatesInRecords = !it.showDateSums
            draftPanel_LinearLayout.setVisible(it.newSpendVisible())
            if (!it.newSpendVisible()) (context as KeyboardManager).hideKeyboard()
            showFloatingDate = it.showFloatingDate()
        })

        adapter.itemClicks = {
            when (it) {
                is SpendUI -> EditSpendDialogFragmentBuilder
                        .newEditSpendDialogFragment(it.date.time, it.id, it.kind, it.value)
                        .also { it.setTargetFragment(this, REQUEST_EDIT_SPEND) }
                        .show(fragmentManager, "edit_record")
                        .also { (context as KeyboardManager).hideKeyboard() }
                is ProfitUI -> AddProfitDialogFragmentBuilder(false)
                        .id(it.id)
                        .kind(it.kind)
                        .value(it.value)
                        .date(it.date.time)
                        .build()
                        .also { it.setTargetFragment(this, REQUEST_EDIT_PROFIT) }
                        .show(fragmentManager, "edit_profit")
                        .also { (context as KeyboardManager).hideKeyboard() }
            }
        }
        adapter.itemLongClicks = {
            when (it) {
                is SpendUI -> DeleteSpendDialogFragmentBuilder
                        .newDeleteSpendDialogFragment(it.id, "${it.date.toFormattedString(resources)}\n${it.kind}\n${it.value}")
                        .also { it.setTargetFragment(this, REQUEST_DELETE_SPEND) }
                        .show(fragmentManager, "delete_record")
                        .also { (context as KeyboardManager).hideKeyboard() }
                is ProfitUI -> DeleteProfitDialogFragmentBuilder
                        .newDeleteProfitDialogFragment(it.id, "${it.date.toFormattedString(resources)}\n${it.kind}\n${it.value}")
                        .also { it.setTargetFragment(this, REQUEST_DELETE_PROFIT) }
                        .show(fragmentManager, "delete_profit")
                        .also { (context as KeyboardManager).hideKeyboard() }
            }
        }

        viewModel.balance30Days.observe(this, Observer {
            toolbar.subtitle = it?.toPointedString()?.let { getString(R.string.text_balance_30_days_format, it) }
        })
        viewModel.showIds.observe(this, Observer { adapter.showIds = it == true })
        viewModel.showChangeKinds.observe(this, Observer { adapter.showChangeKinds = it == true })
        viewModel.showTimes.observe(this, Observer { adapter.showTimesInRecords = it == true })

        draftViewImpl.dialogShower = object : DialogAwareView.DialogShower {
            override fun showDialog(dialogFragment: DialogFragment, requestCode: Int) {
                dialogFragment
                        .also { it.setTargetFragment(this@RecordsListMvvmFragment, requestCode) }
                        .show(fragmentManager, dialogFragment.toString())
            }
        }

        toolbar.title = "${getString(R.string.app_name)} ${E.env.titleSuffix()}"

        RxRecyclerView.scrollEvents(records_RecyclerView)
                .switchMap {
                    Observable.interval(0, 1, TimeUnit.SECONDS)
                            .take(2)
                            .map { it == 0L }
                }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    floatingDate_TextView.animate()
                            .setStartDelay(0L)
                            .setDuration(250L)
                            .alpha(if (it && showFloatingDate && records.any { it is DateSumUI }) 1f else 0f)
                }
                .addTo(viewDisposable)
        RxRecyclerView.scrollEvents(records_RecyclerView)
                .subscribe {
                    if (!showFloatingDate) return@subscribe
                    var i = (records_RecyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                    val floatingTop = floatingDate_TextView.getGlobalVisibleRectRightNow().bottom
                    val vhTop = records_RecyclerView.findViewHolderForAdapterPosition(i).itemView.getGlobalVisibleRectRightNow().bottom
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
                        data.getLongExtra(EditSpendDialogFragment.ID_KEY, 0),
                        data.getStringExtra(EditSpendDialogFragment.KIND_KEY),
                        data.getIntExtra(EditSpendDialogFragment.VALUE_KEY, 0),
                        Date(data.getLongExtra(EditSpendDialogFragment.DATE_KEY, 0L))
                ))
                REQUEST_ADD_PROFIT -> viewModel.addProfit(CreatingProfit(
                        data.getStringExtra(AddProfitDialogFragment.KIND_KEY),
                        data.getIntExtra(AddProfitDialogFragment.VALUE_KEY, 0),
                        Date(data.getLongExtra(AddProfitDialogFragment.DATE_KEY, 0))
                ))
                REQUEST_EDIT_PROFIT -> viewModel.editProfit(Profit(
                        data.getLongExtra(AddProfitDialogFragment.ID_KEY, 0),
                        data.getStringExtra(AddProfitDialogFragment.KIND_KEY),
                        data.getIntExtra(AddProfitDialogFragment.VALUE_KEY, 0),
                        Date(data.getLongExtra(AddProfitDialogFragment.DATE_KEY, 0L))
                ))
                REQUEST_DELETE_PROFIT -> viewModel.deleteProfit(data.getLongExtra(DeleteProfitDialogFragment.ID_KEY, 0))
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
        val showIdsMenuItem = menu.findItem(R.id.show_ids)
        val showChangeKindsMenuItem = menu.findItem(R.id.show_change_kinds)
        val showTimesMenuItem = menu.findItem(R.id.show_times)
        val showBalanceMenuItem = menu.findItem(R.id.show_balance)

        showIdsMenuItem.isVisible = E.env.showIdsSetting
        showChangeKindsMenuItem.isVisible = E.env.showChangeKindsSetting
        menu.findItem(R.id.show_local_changes).isVisible = E.env.showChangeKindsSetting
        menu.findItem(R.id.add_stub_spends).isVisible = E.env.buildForTesting()
        menu.findItem(R.id.add_stub_profits).isVisible = E.env.buildForTesting()
        menu.findItem(R.id.clear_all).isVisible = E.env.buildForTesting()

        viewModel.showSpends.observe(this, Observer { showSpendsMenuItem.isChecked = it == true })
        viewModel.showProfits.observe(this, Observer { showProfitsMenuItem.isChecked = it == true })
        viewModel.showDateSums.observe(this, Observer { showDateSumsMenuItem.isChecked = it == true })
        viewModel.showMonthSums.observe(this, Observer { showMonthSumsMenuItem.isChecked = it == true })
        viewModel.showIds.observe(this, Observer { showIdsMenuItem.isChecked = it == true })
        viewModel.showChangeKinds.observe(this, Observer { showChangeKindsMenuItem.isChecked = it == true })
        viewModel.showTimes.observe(this, Observer { showTimesMenuItem.isChecked = it == true })
        viewModel.showBalance.observe(this, Observer { showBalanceMenuItem.isChecked = it == true })
        showSpendsMenuItem.setOnMenuItemClickListener { viewModel.showSpends(!showSpendsMenuItem.isChecked);true }
        showProfitsMenuItem.setOnMenuItemClickListener { viewModel.showProfits(!showProfitsMenuItem.isChecked);true }
        showDateSumsMenuItem.setOnMenuItemClickListener { viewModel.showDateSums(!showDateSumsMenuItem.isChecked);true }
        showMonthSumsMenuItem.setOnMenuItemClickListener { viewModel.showMonthSums(!showMonthSumsMenuItem.isChecked);true }
        showIdsMenuItem.setOnMenuItemClickListener { viewModel.showIds(!showIdsMenuItem.isChecked);true }
        showChangeKindsMenuItem.setOnMenuItemClickListener { viewModel.showChangeKinds(!showChangeKindsMenuItem.isChecked);true }
        showTimesMenuItem.setOnMenuItemClickListener { viewModel.showTimes(!showTimesMenuItem.isChecked);true }
        showBalanceMenuItem.setOnMenuItemClickListener { viewModel.showBalance(!showBalanceMenuItem.isChecked);true }

        viewModel.showInfo.observe(this, Observer {
            if (it == null) return@Observer
            menu.findItem(R.id.new_profit).isEnabled = it.newProfitEnable()
            showSpendsMenuItem.isEnabled = it.showSpendsEnable()
            showProfitsMenuItem.isEnabled = it.showProfitsEnable()
            showDateSumsMenuItem.isEnabled = it.showDateSumsEnable()
            showMonthSumsMenuItem.isEnabled = it.showMonthSumsEnable()
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.new_profit -> AddProfitDialogFragmentBuilder.newAddProfitDialogFragment(true)
                    .also { it.setTargetFragment(this, REQUEST_ADD_PROFIT) }
                    .show(fragmentManager, "add_profit")
                    .also { (context as KeyboardManager).hideKeyboard() }
            R.id.add_stub_spends -> viewModel.addStubSpends()
            R.id.add_stub_profits -> viewModel.addStubProfits()
            R.id.about -> AppInfoDialogFragment()
                    .show(fragmentManager, "app_info")
                    .also { (context as KeyboardManager).hideKeyboard() }
            R.id.clear_all -> viewModel.clearAll()
            R.id.send_records -> viewModel.sendRecords()
        }
        return super.onOptionsItemSelected(item)
    }
}