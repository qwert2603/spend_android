package com.qwert2603.spenddemo.base_mvi.load_refresh.list.recyclerview

import android.support.annotation.CallSuper
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.qwert2603.spenddemo.model.entity.IdentifiableLong
import com.qwert2603.spenddemo.utils.LogUtils
import com.qwert2603.spenddemo.utils.inflate

abstract class BaseRecyclerViewHolder<M : IdentifiableLong>(parent: ViewGroup, @LayoutRes layoutRes: Int) : RecyclerView.ViewHolder(parent.inflate(layoutRes)) {

    var adapter: BaseRecyclerViewAdapter<M>? = null

    var m: M? = null

    init {
        itemView.setOnClickListener {
            adapter?.let {
                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                if (adapterPosition < it.adapterList.modelList.size) {
                    it.modelItemClicks.onNext(it.adapterList.getModelItem(adapterPosition))
                } else {
                    it.pageIndicatorClicks.onNext(it.adapterList.pageIndicator!!)
                }
            }
        }
        itemView.setOnLongClickListener(View.OnLongClickListener {
            adapter?.let {
                if (adapterPosition == RecyclerView.NO_POSITION) return@OnLongClickListener false
                if (adapterPosition < it.adapterList.modelList.size) {
                    it.modelItemLongClicks.onNext(it.adapterList.getModelItem(adapterPosition))
                    return@OnLongClickListener it.modelItemLongClicks.hasObservers().also {
                        //todo:check
                        LogUtils.d("BaseRecyclerViewHolder OnLongClickListener $it")
                    }
                } else {
                    it.pageIndicatorLongClicks.onNext(it.adapterList.pageIndicator!!)
                    return@OnLongClickListener it.pageIndicatorLongClicks.hasObservers()
                }
            }
            return@OnLongClickListener false
        })
    }

    @CallSuper
    open fun bind(m: M) {
        this.m = m
    }

    @CallSuper
    open fun onRecycled() {
        m = null
    }
}
