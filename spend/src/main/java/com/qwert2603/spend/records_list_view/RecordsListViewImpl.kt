package com.qwert2603.spend.records_list_view

import android.annotation.SuppressLint
import android.content.Context
import com.qwert2603.andrlib.base.mvi.BaseFrameLayout
import com.qwert2603.andrlib.base.mvi.ViewAction
import com.qwert2603.andrlib.base.recyclerview.BaseRecyclerViewAdapter
import com.qwert2603.andrlib.util.inflate
import com.qwert2603.andrlib.util.renderIfChanged
import com.qwert2603.spend.R
import com.qwert2603.spend.utils.setVisibleChild
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.view_records_list.*
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.parameter.parametersOf

@SuppressLint("ViewConstructor")
class RecordsListViewImpl(
        context: Context,
        private val recordsUuids: List<String>
) : BaseFrameLayout<RecordsListViewState, RecordsListView, RecordsListPresenter>(context), RecordsListView, LayoutContainer, KoinComponent {

    override val containerView = this

    override fun createPresenter() = get<RecordsListPresenter> { parametersOf(recordsUuids) }

    private val recordsAdapter = RecordsAdapter()

    var onRenderEmptyListListener: (() -> Unit)? = null

    var onCanChangeRecords: ((lock: Boolean) -> Unit)? = null

    init {
        inflate(R.layout.view_records_list, true)
        records_RecyclerView.adapter = recordsAdapter
    }

    override fun render(vs: RecordsListViewState) {
        super.render(vs)

        renderIfChanged({ canChangeRecords() }) { onCanChangeRecords?.invoke(it) }

        viewRecordsList_FrameLayout.setVisibleChild(when {
            vs.records == null -> R.id.loading_ProgressBar
            vs.records.isEmpty() -> R.id.noRecords_TextView
                    .also { onRenderEmptyListListener?.invoke() }
            else -> R.id.records_RecyclerView
                    .also { recordsAdapter.adapterList = BaseRecyclerViewAdapter.AdapterList(vs.records) }
        })
    }

    override fun executeAction(va: ViewAction) {
        if (va !is RecordsListViewAction) null!!
        when (va) {
            RecordsListViewAction.RerenderAll -> {
                renderAll()
                // items' content was not changed and they were not repainted in #render().
                // so repaint them here.
                recordsAdapter.notifyDataSetChanged()
            }
        }.also { }
    }
}