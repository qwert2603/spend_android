package com.qwert2603.spend.records_list_view

import android.view.ViewGroup
import com.qwert2603.andrlib.base.recyclerview.BaseRecyclerViewAdapter
import com.qwert2603.spend.model.entity.Record

class RecordsAdapter : BaseRecyclerViewAdapter<Record>() {
    override fun onCreateViewHolderModel(parent: ViewGroup, viewType: Int) = RecordViewHolder(parent)
}