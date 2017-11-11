package com.qwert2603.spenddemo.base_mvi.load_refresh.list.recyclerview

import android.support.annotation.PluralsRes
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.base_mvi.load_refresh.list.recyclerview.page_list_item.*
import com.qwert2603.spenddemo.base_mvi.load_refresh.list.recyclerview.vh.AllItemsLoadedViewHolder
import com.qwert2603.spenddemo.base_mvi.load_refresh.list.recyclerview.vh.NextPageErrorViewHolder
import com.qwert2603.spenddemo.base_mvi.load_refresh.list.recyclerview.vh.NextPageLoadingViewHolder
import com.qwert2603.spenddemo.model.entity.IdentifiableLong
import com.qwert2603.spenddemo.utils.LogUtils
import io.reactivex.subjects.PublishSubject

/**
 * Base adapter that contains list of model items and can show page indicator items.
 *
 * Page indicator items are:
 * [NextPageLoadingViewHolder]
 * [NextPageErrorViewHolder]
 * [AllItemsLoadedViewHolder]
 *
 * Classes-inheritor should override [getItemViewTypeModel], [onCreateViewHolderModel] and [onBindViewHolderModel]
 * for view-type defining, creating and binding model-ViewHolders.
 */
abstract class BaseRecyclerViewAdapter<M : IdentifiableLong> : RecyclerView.Adapter<BaseRecyclerViewHolder<IdentifiableLong>>() {

    var recyclerView: RecyclerView? = null

    data class AdapterList<out M : IdentifiableLong>(
            val modelList: List<M>,
            val pageIndicator: PageListItem? = null
    ) {
        val size = modelList.size + if (pageIndicator != null) 1 else 0

        operator fun get(position: Int): IdentifiableLong {
            if (position == modelList.size && pageIndicator != null) return pageIndicator
            return modelList[position]
        }

        fun getModelItem(position: Int) = modelList[position]
    }

    var adapterList: AdapterList<M> = AdapterList(emptyList(), null)
        set(value) {
            if (value == field) return
            val old = field
            field = value

            DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = old.size
                override fun getNewListSize() = field.size
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = old[oldItemPosition].id == field[newItemPosition].id
                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = old[oldItemPosition] == field[newItemPosition]
            }).dispatchUpdatesTo(this)
        }

    var modelItemClicks: PublishSubject<M> = PublishSubject.create()
    var modelItemLongClicks: PublishSubject<M> = PublishSubject.create()

    var pageIndicatorClicks: PublishSubject<PageListItem> = PublishSubject.create()
    var pageIndicatorLongClicks: PublishSubject<PageListItem> = PublishSubject.create()
    var pageIndicatorErrorRetryClicks: PublishSubject<Any> = PublishSubject.create()

    @PluralsRes open protected val pluralsRes = R.plurals.items

    init {
        setHasStableIds(true)
    }

    override final fun setHasStableIds(hasStableIds: Boolean) {
        if (hasStableIds == this.hasStableIds()) return
        super.setHasStableIds(hasStableIds)
    }

    override final fun getItemViewType(position: Int) = when (adapterList[position]) {
        is NextPageLoading -> ViewTypeConstPageList.NEXT_PAGE_LOADING
        is NextPageError -> ViewTypeConstPageList.NEXT_PAGE_ERROR
        is AllItemsLoaded -> ViewTypeConstPageList.ALL_ITEMS_LOADED
        else -> getItemViewTypeModel(adapterList.getModelItem(position))
    }

    @Suppress("UNCHECKED_CAST")
    override final fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseRecyclerViewHolder<IdentifiableLong> = when (viewType) {
        ViewTypeConstPageList.NEXT_PAGE_LOADING -> NextPageLoadingViewHolder(parent)
        ViewTypeConstPageList.NEXT_PAGE_ERROR -> NextPageErrorViewHolder(parent)
        ViewTypeConstPageList.ALL_ITEMS_LOADED -> AllItemsLoadedViewHolder(parent, pluralsRes)
        else -> onCreateViewHolderModel(parent, viewType)
    } as BaseRecyclerViewHolder<IdentifiableLong>

    @Suppress("UNCHECKED_CAST")
    override final fun onBindViewHolder(holder: BaseRecyclerViewHolder<IdentifiableLong>, position: Int) {
        holder.adapter = this as BaseRecyclerViewAdapter<IdentifiableLong>
        if (position < adapterList.modelList.size) {
            onBindViewHolderModel(holder, position)
        } else {
            holder.bind(adapterList.pageIndicator!!)
        }
    }

    open fun getItemViewTypeModel(m: M) = 0

    abstract fun onCreateViewHolderModel(parent: ViewGroup, viewType: Int): BaseRecyclerViewHolder<M>

    open fun onBindViewHolderModel(holder: BaseRecyclerViewHolder<M>, position: Int) {
        holder.bind(adapterList.getModelItem(position))
    }

    override fun onViewRecycled(holder: BaseRecyclerViewHolder<IdentifiableLong>) {
        holder.adapter = null
        holder.onRecycled()
        super.onViewRecycled(holder)
    }

    override fun onFailedToRecycleView(holder: BaseRecyclerViewHolder<IdentifiableLong>?): Boolean {
        LogUtils.e("BaseRecyclerViewAdapter onFailedToRecycleView $holder")
        return super.onFailedToRecycleView(holder)
    }

    override final fun getItemCount() = adapterList.size

    override final fun getItemId(position: Int) = adapterList[position].id
}