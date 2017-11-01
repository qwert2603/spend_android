package com.qwert2603.spenddemo.changes_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.base_mvi.load_refresh.LoadRefreshPanel
import com.qwert2603.spenddemo.base_mvi.load_refresh.list.ListFragment
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.model.entity.Change
import kotlinx.android.synthetic.main.fragment_changes_list.*
import kotlinx.android.synthetic.main.include_list.*
import kotlinx.android.synthetic.main.toolbar_default.*

class ChangesListFragment : ListFragment<ChangesListModel, ChangesListView, ChangesListPresenter, Change>(), ChangesListView {

    companion object {
        const val TAG = "changes_list"
    }

    override fun loadRefreshPanel(): LoadRefreshPanel = changesList_LRPanelImpl

    override val adapter = ChangesAdapter()

    override fun createPresenter() = DIHolder.diManager.presentersCreatorComponent
            .changesListPresenterComponentBuilder()
            .build()
            .createRecordsListPresenter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
            = inflater.inflate(R.layout.fragment_changes_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setTitle(R.string.local_changes)
        listEmpty_TextView.setText(R.string.no_local_changes)
    }
}
