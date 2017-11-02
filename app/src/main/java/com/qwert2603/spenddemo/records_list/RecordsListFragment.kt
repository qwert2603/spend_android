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
import com.qwert2603.spenddemo.changes_list.ChangesListFragment
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.dialogs.DeleteRecordDialogFragment
import com.qwert2603.spenddemo.dialogs.DeleteRecordDialogFragmentBuilder
import com.qwert2603.spenddemo.dialogs.EditRecordDialogFragment
import com.qwert2603.spenddemo.dialogs.EditRecordDialogFragmentBuilder
import com.qwert2603.spenddemo.model.entity.Record
import com.qwert2603.spenddemo.records_list.entity.RecordUI
import com.qwert2603.spenddemo.utils.castAndFilter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_records_list.*
import ru.terrakok.cicerone.Router
import java.sql.Date
import javax.inject.Inject

class RecordsListFragment : BaseFragment<RecordsListViewState, RecordsListView, RecordsListPresenter>(), RecordsListView {

    companion object {
        const val TAG = "records_list"
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

    private val deleteRecordConfirmed = PublishSubject.create<Long>()
    private val editRecordConfirmed = PublishSubject.create<Record>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        DIHolder.diManager.viewsComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
            = inflater.inflate(R.layout.fragment_records_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        records_RecyclerView.layoutManager = LinearLayoutManager(context)
        records_RecyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.records_list, menu)
        RxMenuItem.clicks(menu.findItem(R.id.show_local_changes)).subscribeWith(showChangesClicks)
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

    override fun editRecordClicks(): Observable<RecordUI> = adapter.modelItemClicks
            .castAndFilter(RecordUI::class.java)

    override fun deleteRecordClicks(): Observable<RecordUI> = adapter.modelItemLongClicks
            .castAndFilter(RecordUI::class.java)

    override fun showChangesClicks(): Observable<Any> = showChangesClicks

    override fun deleteRecordConfirmed(): Observable<Long> = deleteRecordConfirmed

    override fun editRecordConfirmed(): Observable<Record> = editRecordConfirmed

    override fun render(vs: RecordsListViewState) {
        super.render(vs)
        adapter.adapterList = BaseRecyclerViewAdapter.AdapterList(vs.records, AllItemsLoaded(vs.recordsCount))
    }

    override fun executeAction(va: ViewAction) {
        when (va) {
            is RecordsListViewAction.MoveToChangesScreen -> router.navigateTo(ChangesListFragment.TAG)
            is RecordsListViewAction.AskToDeleteRecord -> DeleteRecordDialogFragmentBuilder.newDeleteRecordDialogFragment(va.id, va.text)
                    .also { it.setTargetFragment(this, REQUEST_DELETE_RECORD) }
                    .show(fragmentManager, "delete_record")
            is RecordsListViewAction.AskToEditRecord -> EditRecordDialogFragmentBuilder()
                    .id(va.record.id)
                    .kind(va.record.kind)
                    .date(va.record.date.time)
                    .value(va.record.value)
                    .build()
                    .also { it.setTargetFragment(this, REQUEST_EDIT_RECORD) }
                    .show(fragmentManager, "edit_record")
        }
    }
}