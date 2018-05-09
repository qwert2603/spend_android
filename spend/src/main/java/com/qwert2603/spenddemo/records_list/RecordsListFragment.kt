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
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.navigation.KeyboardManager
import com.qwert2603.spenddemo.navigation.ScreenKey
import com.qwert2603.spenddemo.records_list.entity.ProfitUI
import com.qwert2603.spenddemo.records_list.entity.RecordUI
import com.qwert2603.spenddemo.records_list.vhs.DateSumViewHolder
import com.qwert2603.spenddemo.utils.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_records_list.*
import kotlinx.android.synthetic.main.toolbar_default.*
import kotlinx.android.synthetic.main.view_draft.view.*
import ru.terrakok.cicerone.Router
import java.sql.Date
import javax.inject.Inject

class RecordsListFragment : BaseFragment<RecordsListViewState, RecordsListView, RecordsListPresenter>(), RecordsListView {

    companion object {
        private const val REQUEST_DELETE_RECORD = 1
        private const val REQUEST_EDIT_RECORD = 2
        private const val REQUEST_ADD_PROFIT = 3
        private const val REQUEST_DELETE_PROFIT = 4
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

    private val deleteRecordConfirmed = PublishSubject.create<Long>()
    private val editRecordConfirmed = PublishSubject.create<Record>()
    private val addProfitConfirmed = PublishSubject.create<CreatingProfit>()
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
        records_RecyclerView.recycledViewPool.setMaxRecycledViews(RecordsAdapter.VIEW_TYPE_RECORD, 20)
        records_RecyclerView.recycledViewPool.setMaxRecycledViews(RecordsAdapter.VIEW_TYPE_PROFIT, 20)
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
                REQUEST_DELETE_RECORD -> deleteRecordConfirmed.onNext(data.getLongExtra(DeleteRecordDialogFragment.ID_KEY, 0))
                REQUEST_EDIT_RECORD -> editRecordConfirmed.onNext(Record(
                        data.getLongExtra(EditRecordDialogFragment.ID_KEY, 0),
                        data.getStringExtra(EditRecordDialogFragment.KIND_KEY),
                        data.getIntExtra(EditRecordDialogFragment.VALUE_KEY, 0),
                        Date(data.getLongExtra(EditRecordDialogFragment.DATE_KEY, 0L))
                ))
                REQUEST_ADD_PROFIT -> addProfitConfirmed.onNext(CreatingProfit(
                        data.getStringExtra(AddProfitDialogFragment.KIND_KEY),
                        data.getIntExtra(AddProfitDialogFragment.VALUE_KEY, 0),
                        Date(data.getLongExtra(AddProfitDialogFragment.DATE_KEY, 0))
                ))
                REQUEST_DELETE_PROFIT -> deleteProfitConfirmed.onNext(data.getLongExtra(DeleteProfitDialogFragment.ID_KEY, 0))
            }
        }
    }

    override fun viewCreated(): Observable<Any> = Observable.just(Any())

    override fun editRecordClicks(): Observable<RecordUI> = adapter.modelItemClicks
            .castAndFilter(RecordUI::class.java)

    override fun deleteRecordClicks(): Observable<RecordUI> = adapter.modelItemLongClicks
            .castAndFilter(RecordUI::class.java)

    override fun showChangesClicks(): Observable<Any> = showChangesClicks

    override fun deleteRecordConfirmed(): Observable<Long> = deleteRecordConfirmed

    override fun editRecordConfirmed(): Observable<Record> = editRecordConfirmed

    override fun sendRecordsClicks(): Observable<Any> = sendRecordsClicks

    override fun showAboutClicks(): Observable<Any> = showAboutClicks

    override fun showIdsChanges(): Observable<Boolean> = showIdsChanges

    override fun showChangeKindsChanges(): Observable<Boolean> = showChangeKindsChanges

    override fun showDateSumsChanges(): Observable<Boolean> = showDateSumsChanges

    override fun showSpendsChanges(): Observable<Boolean> = showSpendsChanges

    override fun showProfitsChanges(): Observable<Boolean> = showProfitsChanges

    override fun addProfitClicks(): Observable<Any> = addProfitClicks

    override fun deleteProfitClicks(): Observable<ProfitUI> = adapter.modelItemLongClicks
            .castAndFilter(ProfitUI::class.java)

    override fun addProfitConfirmed(): Observable<CreatingProfit> = addProfitConfirmed

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

        adapter.adapterList = BaseRecyclerViewAdapter.AdapterList(vs.records)

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

        toolbar.subtitle = getString(R.string.text_balance_30_days_format, vs.balance30Days)

        optionsMenu?.apply {
            findItem(R.id.show_local_changes).isVisible = vs.showChangeKinds && vs.changesCount > 0
            findItem(R.id.show_ids).isChecked = vs.showIds
            findItem(R.id.show_change_kinds).isChecked = vs.showChangeKinds
            findItem(R.id.show_date_sums).isChecked = vs.showDateSums
            findItem(R.id.show_spends).isChecked = vs.showSpends
            findItem(R.id.show_spends).isEnabled = vs.showSpendsEnable()
            findItem(R.id.show_profits).isChecked = vs.showProfits
            findItem(R.id.show_profits).isEnabled = vs.showProfitsEnable()
            findItem(R.id.new_profit).isVisible = vs.newProfitVisible()
        }

        draftPanel_LinearLayout.setVisible(vs.newSpendVisible())
        if (!vs.newSpendVisible()) (context as KeyboardManager).hideKeyboard()
    }

    override fun executeAction(va: ViewAction) {
        if (va !is RecordsListViewAction) return
        @Suppress("IMPLICIT_CAST_TO_ANY")
        when (va) {
            RecordsListViewAction.MoveToChangesScreen -> router.navigateTo(ScreenKey.CHANGES_LIST.name)
            is RecordsListViewAction.AskToDeleteRecord -> DeleteRecordDialogFragmentBuilder.newDeleteRecordDialogFragment(va.id)
                    .also { it.setTargetFragment(this, REQUEST_DELETE_RECORD) }
                    .show(fragmentManager, "delete_record")
                    .also { (context as KeyboardManager).hideKeyboard() }
            is RecordsListViewAction.AskToEditRecord -> EditRecordDialogFragmentBuilder
                    .newEditRecordDialogFragment(va.record.date.time, va.record.id, va.record.kind, va.record.value)
                    .also { it.setTargetFragment(this, REQUEST_EDIT_RECORD) }
                    .show(fragmentManager, "edit_record")
                    .also { (context as KeyboardManager).hideKeyboard() }
            is RecordsListViewAction.ScrollToRecordAndHighlight -> {
                currentViewState.records
                        .indexOfFirst { it is RecordUI && it.id == va.recordId }
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
                        .apply { context?.startActivity(this) }
            }
            RecordsListViewAction.ShowAbout -> AppInfoDialogFragment()
                    .show(fragmentManager, "app_info")
                    .also { (context as KeyboardManager).hideKeyboard() }
            RecordsListViewAction.OpenAddProfitDialog -> AddProfitDialogFragment()
                    .also { it.setTargetFragment(this, REQUEST_ADD_PROFIT) }
                    .show(fragmentManager, "add_profit")
                    .also { (context as KeyboardManager).hideKeyboard() }
            is RecordsListViewAction.AskToDeleteProfit -> DeleteProfitDialogFragmentBuilder.newDeleteProfitDialogFragment(va.id)
                    .also { it.setTargetFragment(this, REQUEST_DELETE_PROFIT) }
                    .show(fragmentManager, "delete_profit")
                    .also { (context as KeyboardManager).hideKeyboard() }
        }.also { }
    }
}