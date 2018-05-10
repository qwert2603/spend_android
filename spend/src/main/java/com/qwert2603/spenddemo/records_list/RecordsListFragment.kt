package com.qwert2603.spenddemo.records_list

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.view.animation.AnimationUtils
import com.jakewharton.rxbinding2.view.RxMenuItem
import com.qwert2603.andrlib.base.mvi.BaseFragment
import com.qwert2603.andrlib.base.mvi.ViewAction
import com.qwert2603.andrlib.base.recyclerview.BaseRecyclerViewAdapter
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spenddemo.BuildConfig
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.dialogs.*
import com.qwert2603.spenddemo.model.entity.CreatingProfit
import com.qwert2603.spenddemo.model.entity.Profit
import com.qwert2603.spenddemo.model.entity.Spend
import com.qwert2603.spenddemo.navigation.KeyboardManager
import com.qwert2603.spenddemo.navigation.ScreenKey
import com.qwert2603.spenddemo.records_list.entity.ProfitUI
import com.qwert2603.spenddemo.records_list.entity.SpendUI
import com.qwert2603.spenddemo.records_list.vhs.DateSumViewHolder
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_records_list.*
import kotlinx.android.synthetic.main.toolbar_default.*
import kotlinx.android.synthetic.main.view_spend_draft.view.*
import ru.terrakok.cicerone.Router
import java.sql.Date
import javax.inject.Inject

class RecordsListFragment : BaseFragment<RecordsListViewState, RecordsListView, RecordsListPresenter>(), RecordsListView {

    companion object {
        private const val REQUEST_DELETE_SPEND = 1
        private const val REQUEST_EDIT_SPEND = 2
        private const val REQUEST_ADD_PROFIT = 3
        private const val REQUEST_EDIT_PROFIT = 4
        private const val REQUEST_DELETE_PROFIT = 5
    }

    private val adapter = RecordsAdapter()

    @Inject
    lateinit var router: Router

    override fun viewForSnackbar(): View? = coordinator

    override fun createPresenter(): RecordsListPresenter = DIHolder.diManager.presentersCreatorComponent
            .recordsListPresenterComponentBuilder()
            .build()
            .createRecordsListPresenter()

    private val showChangesClicks = PublishSubject.create<Any>()
    private val sendRecordsClicks = PublishSubject.create<Any>()
    private val showAboutClicks = PublishSubject.create<Any>()
    private val showIdsChanges = PublishSubject.create<Boolean>()
    private val showChangeKindsChanges = PublishSubject.create<Boolean>()
    private val showDateSumsChanges = PublishSubject.create<Boolean>()
    private val showSpendsChanges = PublishSubject.create<Boolean>()
    private val showProfitsChanges = PublishSubject.create<Boolean>()
    private val addProfitClicks = PublishSubject.create<Any>()
    private val addStubSpendsClicks = PublishSubject.create<Any>()
    private val addStubProfitsClicks = PublishSubject.create<Any>()
    private val clearAllClicks = PublishSubject.create<Any>()

    private var optionsMenu: Menu? = null

    private val deleteSpendConfirmed = PublishSubject.create<Long>()
    private val editSpendConfirmed = PublishSubject.create<Spend>()
    private val addProfitConfirmed = PublishSubject.create<CreatingProfit>()
    private val editProfitConfirmed = PublishSubject.create<Profit>()
    private val deleteProfitConfirmed = PublishSubject.create<Long>()

    private var wasRecreated = false
    private var layoutAnimationShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        DIHolder.diManager.viewsComponent.inject(this)
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        wasRecreated = savedInstanceState != null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_records_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        records_RecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        records_RecyclerView.adapter = adapter
        records_RecyclerView.recycledViewPool.setMaxRecycledViews(RecordsAdapter.VIEW_TYPE_SPEND, 20)
        records_RecyclerView.recycledViewPool.setMaxRecycledViews(RecordsAdapter.VIEW_TYPE_PROFIT, 20)
        records_RecyclerView.recycledViewPool.setMaxRecycledViews(RecordsAdapter.VIEW_TYPE_DATE_SUM, 20)
        records_RecyclerView.itemAnimator = RecordsListAnimator()
                .also {
                    it.spendOrigin = object : RecordsListAnimator.SpendOrigin {
                        override fun getDateGlobalVisibleRect(): Rect = draftViewImpl.date_EditText.getGlobalVisibleRectRightNow()
                        override fun getKindGlobalVisibleRect(): Rect = draftViewImpl.kind_EditText.getGlobalVisibleRectRightNow()
                        override fun getValueGlobalVisibleRect(): Rect = draftViewImpl.value_EditText.getGlobalVisibleRectRightNow()
                    }
                }
        records_RecyclerView.addItemDecoration(ConditionDividerDecoration(requireContext(), { rv, vh ->
            vh.adapterPosition > 0 && rv.findViewHolderForAdapterPosition(vh.adapterPosition - 1) is DateSumViewHolder
        }))

        draftViewImpl.dialogShower = object : DialogAwareView.DialogShower {
            override fun showDialog(dialogFragment: DialogFragment, requestCode: Int) {
                dialogFragment
                        .also { it.setTargetFragment(this@RecordsListFragment, requestCode) }
                        .show(fragmentManager, dialogFragment.toString())
            }
        }

        toolbar.title = "${getString(R.string.app_name)} ${BuildConfig.FLAVOR} ${BuildConfig.BUILD_TYPE}"

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.records_list, menu)
        optionsMenu = menu

        RxMenuItem.clicks(menu.findItem(R.id.show_local_changes)).subscribeWith(showChangesClicks)
        RxMenuItem.clicks(menu.findItem(R.id.send_records)).subscribeWith(sendRecordsClicks)
        RxMenuItem.clicks(menu.findItem(R.id.about)).subscribeWith(showAboutClicks)
        RxMenuItem.clicks(menu.findItem(R.id.new_profit)).subscribeWith(addProfitClicks)
        menu.findItem(R.id.show_ids).checkedChanges().subscribeWith(showIdsChanges)
        menu.findItem(R.id.show_change_kinds).checkedChanges().subscribeWith(showChangeKindsChanges)
        menu.findItem(R.id.show_date_sums).checkedChanges().subscribeWith(showDateSumsChanges)
        menu.findItem(R.id.show_spends).checkedChanges().subscribeWith(showSpendsChanges)
        menu.findItem(R.id.show_profits).checkedChanges().subscribeWith(showProfitsChanges)
        RxMenuItem.clicks(menu.findItem(R.id.add_stub_spends)).subscribeWith(addStubSpendsClicks)
        RxMenuItem.clicks(menu.findItem(R.id.add_stub_profits)).subscribeWith(addStubProfitsClicks)
        RxMenuItem.clicks(menu.findItem(R.id.clear_all)).subscribeWith(clearAllClicks)

        renderAll()
    }

    override fun onDestroyOptionsMenu() {
        optionsMenu = null
        super.onDestroyOptionsMenu()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        draftViewImpl.onDialogResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_DELETE_SPEND -> deleteSpendConfirmed.onNext(data.getLongExtra(DeleteSpendDialogFragment.ID_KEY, 0))
                REQUEST_EDIT_SPEND -> editSpendConfirmed.onNext(Spend(
                        data.getLongExtra(EditSpendDialogFragment.ID_KEY, 0),
                        data.getStringExtra(EditSpendDialogFragment.KIND_KEY),
                        data.getIntExtra(EditSpendDialogFragment.VALUE_KEY, 0),
                        Date(data.getLongExtra(EditSpendDialogFragment.DATE_KEY, 0L))
                ))
                REQUEST_ADD_PROFIT -> addProfitConfirmed.onNext(CreatingProfit(
                        data.getStringExtra(AddProfitDialogFragment.KIND_KEY),
                        data.getIntExtra(AddProfitDialogFragment.VALUE_KEY, 0),
                        Date(data.getLongExtra(AddProfitDialogFragment.DATE_KEY, 0))
                ))
                REQUEST_EDIT_PROFIT -> editProfitConfirmed.onNext(Profit(
                        data.getLongExtra(AddProfitDialogFragment.ID_KEY, 0),
                        data.getStringExtra(AddProfitDialogFragment.KIND_KEY),
                        data.getIntExtra(AddProfitDialogFragment.VALUE_KEY, 0),
                        Date(data.getLongExtra(AddProfitDialogFragment.DATE_KEY, 0L))
                ))
                REQUEST_DELETE_PROFIT -> deleteProfitConfirmed.onNext(data.getLongExtra(DeleteProfitDialogFragment.ID_KEY, 0))
            }
        }
    }

    override fun viewCreated(): Observable<Any> = Observable.just(Any())

    override fun editSpendClicks(): Observable<SpendUI> = adapter.modelItemClicks
            .castAndFilter(SpendUI::class.java)

    override fun deleteSpendClicks(): Observable<SpendUI> = adapter.modelItemLongClicks
            .castAndFilter(SpendUI::class.java)

    override fun showChangesClicks(): Observable<Any> = showChangesClicks

    override fun deleteSpendConfirmed(): Observable<Long> = deleteSpendConfirmed

    override fun editSpendConfirmed(): Observable<Spend> = editSpendConfirmed

    override fun sendRecordsClicks(): Observable<Any> = sendRecordsClicks

    override fun showAboutClicks(): Observable<Any> = showAboutClicks

    override fun showIdsChanges(): Observable<Boolean> = showIdsChanges

    override fun showChangeKindsChanges(): Observable<Boolean> = showChangeKindsChanges

    override fun showDateSumsChanges(): Observable<Boolean> = showDateSumsChanges

    override fun showSpendsChanges(): Observable<Boolean> = showSpendsChanges

    override fun showProfitsChanges(): Observable<Boolean> = showProfitsChanges

    override fun addProfitClicks(): Observable<Any> = addProfitClicks

    override fun editProfitClicks(): Observable<ProfitUI> = adapter.modelItemClicks
            .castAndFilter(ProfitUI::class.java)

    override fun deleteProfitClicks(): Observable<ProfitUI> = adapter.modelItemLongClicks
            .castAndFilter(ProfitUI::class.java)

    override fun addProfitConfirmed(): Observable<CreatingProfit> = addProfitConfirmed

    override fun editProfitConfirmed(): Observable<Profit> = editProfitConfirmed

    override fun deleteProfitConfirmed(): Observable<Long> = deleteProfitConfirmed

    override fun addStubSpendsClicks(): Observable<Any> = addStubSpendsClicks

    override fun addStubProfitsClicks(): Observable<Any> = addStubProfitsClicks

    override fun clearAllClicks(): Observable<Any> = clearAllClicks

    override fun render(vs: RecordsListViewState) {
        super.render(vs)

        // todo: show floating date on top of screen when showing date sums.

        renderIfChanged({ showIds }) { adapter.showIds = it }
        renderIfChanged({ showChangeKinds }) { adapter.showChangeKinds = it }
        renderIfChanged({ showDateSums }) { adapter.showDatesInRecords = !it }
        renderIfChangedThree({ Triple(showIds, showChangeKinds, showDateSums) }) { adapter.notifyDataSetChanged() }

        renderIfChanged({ records }) { adapter.adapterList = BaseRecyclerViewAdapter.AdapterList(it) }

        if (!wasRecreated && vs.records.size > 1) {
            renderIfChanged({ records }) {
                if (!layoutAnimationShown) {
                    layoutAnimationShown = true
                    val layoutAnimation = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down)
                    records_RecyclerView.layoutAnimation = layoutAnimation
                }
            }
        }

        // todo: show changesCount on menuItem's icon.
//        toolbar.title = getString(R.string.app_name) + if (vs.showChangeKinds && vs.changesCount > 0) " (${vs.changesCount})" else ""

        renderIfChanged({ balance30Days }) { toolbar.subtitle = getString(R.string.text_balance_30_days_format, it) }

        optionsMenu?.apply {
            renderIfChanged({ showChangeKinds && changesCount > 0 }) { findItem(R.id.show_local_changes).isVisible = it }
            renderIfChanged({ showIds }) { findItem(R.id.show_ids).isChecked = it }
            renderIfChanged({ showChangeKinds }) { findItem(R.id.show_change_kinds).isChecked = it }
            renderIfChanged({ showDateSums }) { findItem(R.id.show_date_sums).isChecked = it }
            renderIfChanged({ showSpends }) { findItem(R.id.show_spends).isChecked = it }
            renderIfChanged({ showSpendsEnable() }) { findItem(R.id.show_spends).isEnabled = it }
            renderIfChanged({ showProfits }) { findItem(R.id.show_profits).isChecked = it }
            renderIfChanged({ showProfitsEnable() }) { findItem(R.id.show_profits).isEnabled = it }
            renderIfChanged({ newProfitVisible() }) { findItem(R.id.new_profit).isVisible = it }
        }

        renderIfChanged({ newSpendVisible() }) {
            draftPanel_LinearLayout.setVisible(it)
            if (!it) (context as KeyboardManager).hideKeyboard()
        }
    }

    override fun executeAction(va: ViewAction) {
        if (va !is RecordsListViewAction) return
        @Suppress("IMPLICIT_CAST_TO_ANY")
        when (va) {
            RecordsListViewAction.MoveToChangesScreen -> router.navigateTo(ScreenKey.CHANGES_LIST.name)
            is RecordsListViewAction.AskToDeleteSpend -> DeleteSpendDialogFragmentBuilder.newDeleteSpendDialogFragment(va.id)
                    .also { it.setTargetFragment(this, REQUEST_DELETE_SPEND) }
                    .show(fragmentManager, "delete_record")
                    .also { (context as KeyboardManager).hideKeyboard() }
            is RecordsListViewAction.AskToEditSpend -> EditSpendDialogFragmentBuilder
                    .newEditSpendDialogFragment(va.spend.date.time, va.spend.id, va.spend.kind, va.spend.value)
                    .also { it.setTargetFragment(this, REQUEST_EDIT_SPEND) }
                    .show(fragmentManager, "edit_record")
                    .also { (context as KeyboardManager).hideKeyboard() }
            is RecordsListViewAction.ScrollToSpendAndHighlight -> {
                currentViewState.records
                        .indexOfFirst { it is SpendUI && it.id == va.spendId }
                        .takeIf { it >= 0 }
                        ?.let {
                            records_RecyclerView.scrollToPosition(it)
                            records_RecyclerView.postDelayed(
                                    { adapter.notifyItemChanged(it, RecordsListAnimator.PAYLOAD_HIGHLIGHT) },
                                    100
                            )
                        }
            }
            is RecordsListViewAction.ScrollToProfitAndHighlight -> {
                // todo: remove this delay when profits list will use sync processor like spends list.
                records_RecyclerView.postDelayed({
                    currentViewState.records
                            .indexOfFirst { it is ProfitUI && it.id == va.profitId }
                            .takeIf { it >= 0 }
                            ?.let {
                                records_RecyclerView?.scrollToPosition(it)
                                records_RecyclerView?.postDelayed(
                                        { adapter.notifyItemChanged(it, RecordsListAnimator.PAYLOAD_HIGHLIGHT) },
                                        100
                                )
                            }
                }, 500)
            }
            is RecordsListViewAction.SendRecords -> {
                Intent(Intent.ACTION_SEND)
                        .also { it.putExtra(Intent.EXTRA_TEXT, va.text) }
                        .also { it.type = "text/plain" }
                        .let { Intent.createChooser(it, context?.getString(R.string.send_title)) }
                        .apply { requireActivity().startActivity(this) }
            }
            RecordsListViewAction.ShowAbout -> AppInfoDialogFragment()
                    .show(fragmentManager, "app_info")
                    .also { (context as KeyboardManager).hideKeyboard() }
            RecordsListViewAction.OpenAddProfitDialog -> AddProfitDialogFragmentBuilder.newAddProfitDialogFragment(true)
                    .also { it.setTargetFragment(this, REQUEST_ADD_PROFIT) }
                    .show(fragmentManager, "add_profit")
                    .also { (context as KeyboardManager).hideKeyboard() }
            is RecordsListViewAction.AskToEditProfit -> AddProfitDialogFragmentBuilder(false)
                    .id(va.profitUI.id)
                    .kind(va.profitUI.kind)
                    .value(va.profitUI.value)
                    .date(va.profitUI.date.time)
                    .build()
                    .also { it.setTargetFragment(this, REQUEST_EDIT_PROFIT) }
                    .show(fragmentManager, "edit_profit")
                    .also { (context as KeyboardManager).hideKeyboard() }
            is RecordsListViewAction.AskToDeleteProfit -> DeleteProfitDialogFragmentBuilder.newDeleteProfitDialogFragment(va.id)
                    .also { it.setTargetFragment(this, REQUEST_DELETE_PROFIT) }
                    .show(fragmentManager, "delete_profit")
                    .also { (context as KeyboardManager).hideKeyboard() }
        }.also { }
    }
}