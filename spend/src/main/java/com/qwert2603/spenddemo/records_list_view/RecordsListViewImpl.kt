package com.qwert2603.spenddemo.records_list_view

import android.annotation.SuppressLint
import android.content.Context
import com.qwert2603.andrlib.base.mvi.BaseFrameLayout
import com.qwert2603.andrlib.base.mvi.ViewAction
import com.qwert2603.andrlib.base.recyclerview.BaseRecyclerViewAdapter
import com.qwert2603.andrlib.util.inflate
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.utils.setVisibleChild
import kotlinx.android.synthetic.main.view_records_list.view.*

@SuppressLint("ViewConstructor")
class RecordsListViewImpl(
        context: Context,
        private val recordsUuids: List<String>
) : BaseFrameLayout<RecordsListViewState, RecordsListView, RecordsListPresenter>(context), RecordsListView {

    override fun createPresenter() = DIHolder.diManager.presentersCreatorComponent
            .recordsListViewPresenterCreatorComponent()
            .recordsUuids(recordsUuids)
            .build()
            .createPresenter()

    private val recordsAdapter = RecordsAdapter()

    init {
        inflate(R.layout.view_records_list, true)
        records_RecyclerView.adapter = recordsAdapter
    }

    override fun render(vs: RecordsListViewState) {
        super.render(vs)

        viewRecordsList_FrameLayout.setVisibleChild(when {
            vs.records == null -> R.id.loading_ProgressBar
            vs.records.isEmpty() -> R.id.noRecords_TextView
            else -> R.id.records_RecyclerView
                    .also { recordsAdapter.adapterList = BaseRecyclerViewAdapter.AdapterList(vs.records) }
        })
    }

    override fun executeAction(va: ViewAction) {
        // nth
    }
}