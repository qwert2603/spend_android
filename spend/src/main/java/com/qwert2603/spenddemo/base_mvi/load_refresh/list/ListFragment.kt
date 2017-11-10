package com.qwert2603.spenddemo.base_mvi.load_refresh.list

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.jakewharton.rxbinding2.view.RxView
import com.qwert2603.spenddemo.base_mvi.BasePresenter
import com.qwert2603.spenddemo.base_mvi.BaseView
import com.qwert2603.spenddemo.base_mvi.load_refresh.InitialModelHolder
import com.qwert2603.spenddemo.base_mvi.load_refresh.LRFragment
import com.qwert2603.spenddemo.base_mvi.load_refresh.LRViewState
import com.qwert2603.spenddemo.base_mvi.load_refresh.list.recyclerview.BaseRecyclerViewAdapter
import com.qwert2603.spenddemo.model.entity.IdentifiableLong
import com.qwert2603.spenddemo.utils.showIfNotYet
import io.reactivex.Observable
import kotlinx.android.synthetic.main.include_list.*

abstract class ListFragment<M, V : BaseView<LRViewState<M>>, P : BasePresenter<V, LRViewState<M>>, T : IdentifiableLong>
    : LRFragment<M, V, P>(), ListView<M> where M : InitialModelHolder<*>, M : ListModelHolder<T> {

    companion object {
        private const val LAYER_NOTHING = 0
        private const val LAYER_EMPTY = 1
        private const val LAYER_LIST = 2
    }

    abstract protected val adapter: BaseRecyclerViewAdapter<T>

    open protected fun createLayoutManager(): RecyclerView.LayoutManager = LinearLayoutManager(context)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list_RecyclerView.layoutManager = createLayoutManager()
    }

    override fun onDestroyView() {
        adapter.recyclerView = null
        super.onDestroyView()
    }

    override fun loadNextPage(): Observable<Any> = RxView.preDraws(list_RecyclerView, { true })
            .filter {
                with(currentViewState) {
                    isModelLoaded && !refreshing && !model.allItemsLoaded && !model.nextPageLoading && model.nextPageError == null
                }
            }
            .filter {
                val linearLayoutManager = list_RecyclerView.layoutManager as? LinearLayoutManager ?: return@filter false
                linearLayoutManager.findLastVisibleItemPosition() > adapter.itemCount - 5
            }
            .mergeWith(adapter.pageIndicatorErrorRetryClicks)

    override fun render(vs: LRViewState<M>) {
        super.render(vs)

        // we need to set adapter.modelList = emptyList() and list_RecyclerView.adapter = null,
        // to prevent from showing old list when new list is showing after loading or showing "list is empty".
        if (vs.isModelLoaded) {
            if (vs.model.showingList.isEmpty() && vs.model.allItemsLoaded) {
                if (list_RecyclerView.adapter != null) {
                    adapter.adapterList = BaseRecyclerViewAdapter.AdapterList(emptyList())
                    adapter.notifyDataSetChanged()
                    list_RecyclerView.adapter = null
                    adapter.recyclerView = null
                }
                list_ViewAnimator.showIfNotYet(LAYER_EMPTY)
            } else {
                if (list_RecyclerView.adapter == null) {
                    list_RecyclerView.adapter = adapter
                    adapter.recyclerView = list_RecyclerView
                }
                adapter.adapterList = BaseRecyclerViewAdapter.AdapterList(vs.model.showingList, vs.model.pageIndicatorItem())
                list_ViewAnimator.showIfNotYet(LAYER_LIST)
            }
        } else {
            if (list_RecyclerView.adapter != null) {
                adapter.adapterList = BaseRecyclerViewAdapter.AdapterList(emptyList())
                adapter.notifyDataSetChanged()
                list_RecyclerView.adapter = null
                adapter.recyclerView = null
            }
            list_ViewAnimator.showIfNotYet(LAYER_NOTHING, animate = false)
        }
    }
}