package com.qwert2603.spenddemo.records_list

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.jakewharton.rxbinding2.view.RxMenuItem
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.base_mvi.BaseFragment
import com.qwert2603.spenddemo.base_mvi.ViewAction
import com.qwert2603.spenddemo.base_mvi.load_refresh.list.recyclerview.BaseRecyclerViewAdapter
import com.qwert2603.spenddemo.base_mvi.load_refresh.list.recyclerview.page_list_item.AllItemsLoaded
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.dialogs.*
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.navigation.KeyboardManager
import com.qwert2603.spenddemo.navigation.ScreenKeys
import com.qwert2603.spenddemo.records_list.entity.RecordUI
import com.qwert2603.spenddemo.utils.castAndFilter
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_records_list.*
import kotlinx.android.synthetic.main.toolbar_default.*
import ru.terrakok.cicerone.Router
import java.sql.Date
import javax.inject.Inject

class RecordsListFragment : BaseFragment<RecordsListViewState, RecordsListView, RecordsListPresenter>(), RecordsListView {

    companion object {
        private const val REQUEST_DELETE_RECORD = 1
        private const val REQUEST_EDIT_RECORD = 2
    }

    private val adapter = RecordsAdapter()

    @Inject lateinit var router: Router

    override fun createPresenter(): RecordsListPresenter = DIHolder.diManager.presentersCreatorComponent
            .recordsListPresenterComponentBuilder()
            .build()
            .createRecordsListPresenter()

    private val showChangesClicks = PublishSubject.create<Any>()
    private val sendRecordsClicks = PublishSubject.create<Any>()
    private val showAboutClicks = PublishSubject.create<Any>()

    private val deleteRecordConfirmed = PublishSubject.create<Long>()
    private val editRecordConfirmed = PublishSubject.create<Record>()

    private val showChangeKinds: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    private val changesCount: BehaviorSubject<Int> = BehaviorSubject.createDefault(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        DIHolder.diManager.viewsComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
            = inflater.inflate(R.layout.fragment_records_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        records_RecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
                .also { it.initialPrefetchItemCount = 10 }
        records_RecyclerView.adapter = adapter
        records_RecyclerView.recycledViewPool.setMaxRecycledViews(RecordsAdapter.VIEW_TYPE_RECORD, 20)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.records_list, menu)
        Observable
                .combineLatest(
                        changesCount.map { it > 0 },
                        showChangeKinds,
                        BiFunction { notZero: Boolean, show: Boolean -> notZero && show }
                )
                .subscribe(RxMenuItem.visible(menu.findItem(R.id.show_local_changes)))
        RxMenuItem.clicks(menu.findItem(R.id.show_local_changes)).subscribeWith(showChangesClicks)
        RxMenuItem.clicks(menu.findItem(R.id.send)).subscribeWith(sendRecordsClicks)
        RxMenuItem.clicks(menu.findItem(R.id.about)).subscribeWith(showAboutClicks)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_DELETE_RECORD && resultCode == Activity.RESULT_OK && data != null) {
            deleteRecordConfirmed.onNext(data.getLongExtra(DeleteRecordDialogFragment.ID_KEY, 0))
        }
        if (requestCode == REQUEST_EDIT_RECORD && resultCode == Activity.RESULT_OK && data != null) {
            editRecordConfirmed.onNext(Record(
                    data.getLongExtra(EditRecordDialogFragment.ID_KEY, 0),
                    data.getStringExtra(EditRecordDialogFragment.KIND_KEY),
                    data.getIntExtra(EditRecordDialogFragment.VALUE_KEY, 0),
                    Date(data.getLongExtra(EditRecordDialogFragment.DATE_KEY, 0L))
            ))
        }
        super.onActivityResult(requestCode, resultCode, data)
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

    override fun render(vs: RecordsListViewState) {
        // todo: TEST
        // on api19 records are not shown when app starts, but they are showing if create new record.
        super.render(vs)
        adapter.showIds = vs.showIds
        adapter.showChangeKinds = vs.showChangeKinds
        if (adapter.adapterList.size <= 1) records_RecyclerView.scrollToPosition(0)
        adapter.adapterList = BaseRecyclerViewAdapter.AdapterList(vs.records, AllItemsLoaded(vs.recordsCount))
        toolbar.title = getString(R.string.app_name) +
                if (vs.showChangeKinds && vs.changesCount > 0) " (${vs.changesCount})" else ""
        // todo: show changesCount on menuItem's icon.
        showChangeKinds.onNext(vs.showChangeKinds)
        changesCount.onNext(vs.changesCount)
    }

    override fun executeAction(va: ViewAction) {
        when (va) {
            is RecordsListViewAction.MoveToChangesScreen -> router.navigateTo(ScreenKeys.CHANGES_LIST)
            is RecordsListViewAction.AskToDeleteRecord -> DeleteRecordDialogFragmentBuilder.newDeleteRecordDialogFragment(va.id, va.text)
                    .also { it.setTargetFragment(this, REQUEST_DELETE_RECORD) }
                    .show(fragmentManager, "delete_record")
                    .also { (context as KeyboardManager).hideKeyboard() }
            is RecordsListViewAction.AskToEditRecord -> EditRecordDialogFragmentBuilder()
                    .id(va.record.id)
                    .kind(va.record.kind)
                    .date(va.record.date.time)
                    .value(va.record.value)
                    .build()
                    .also { it.setTargetFragment(this, REQUEST_EDIT_RECORD) }
                    .show(fragmentManager, "edit_record")
                    .also { (context as KeyboardManager).hideKeyboard() }
        // todo: highlight created item.
            is RecordsListViewAction.ScrollToPosition -> records_RecyclerView.smoothScrollToPosition(va.position)
            is RecordsListViewAction.SendRecords -> {
                Intent(Intent.ACTION_SEND)
                        .also { it.putExtra(Intent.EXTRA_TEXT, va.text) }
                        .also { it.type = "text/plain" }
                        .let { Intent.createChooser(it, context?.getString(R.string.send_title)) }
                        .apply { context?.startActivity(this) }
            }
            is RecordsListViewAction.ShowAbout -> AppInfoDialogFragment()
                    .show(fragmentManager, "app_info")
                    .also { (context as KeyboardManager).hideKeyboard() }
        }
    }
}