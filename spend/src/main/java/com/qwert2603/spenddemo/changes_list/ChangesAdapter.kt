package com.qwert2603.spenddemo.changes_list

import android.view.ViewGroup
import com.qwert2603.andrlib.base.recyclerview.BaseRecyclerViewAdapter
import com.qwert2603.spenddemo.model.entity.SpendChange

class ChangesAdapter : BaseRecyclerViewAdapter<SpendChange>() {
    override fun onCreateViewHolderModel(parent: ViewGroup, viewType: Int) = ChangeViewHolder(parent)
}