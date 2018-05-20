package com.qwert2603.spenddemo.records_list_mvvm

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import com.qwert2603.spenddemo.R
import kotlinx.android.synthetic.main.fragment_records_list_mvvm.*
import kotlinx.android.synthetic.main.toolbar_default.*

class RecordsListMvvmFragment : Fragment() {

    private val viewModel by lazy {
        ViewModelProviders.of(this, ViewModelFactory()).get(RecordsListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_records_list_mvvm, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = RecordsListAdapter()
        records_RecyclerView.adapter = adapter
        var prevSecond: Boolean? = null
        var prevThird: Boolean? = null
        viewModel.recordsLiveData.observe(this, Observer {
            if (it?.second != prevSecond || it?.third != prevThird) {
                adapter.submitList(null)
            }
            prevSecond = it?.second
            prevThird = it?.third
            adapter.submitList(it?.first)
        })
        viewModel.recordsCounts.observe(this, Observer { toolbar.subtitle = it })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.records_list, menu)

        val showSpendsMenuItem = menu.findItem(R.id.show_spends)
        val showProfitsMenuItem = menu.findItem(R.id.show_profits)
        viewModel.showSpends.observe(this, Observer { showSpendsMenuItem.isChecked = it == true })
        viewModel.showProfits.observe(this, Observer { showProfitsMenuItem.isChecked = it == true })
        showSpendsMenuItem.setOnMenuItemClickListener { viewModel.showSpends(!showSpendsMenuItem.isChecked);true }
        showProfitsMenuItem.setOnMenuItemClickListener { viewModel.showProfits(!showProfitsMenuItem.isChecked);true }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.add_stub_spends -> viewModel.addStubSpends()
            R.id.add_stub_profits -> viewModel.addStubProfits()
            R.id.clear_all -> viewModel.clearAll()
        }
        return super.onOptionsItemSelected(item)
    }
}