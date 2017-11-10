package com.qwert2603.spenddemo.changes_list

import android.support.v4.content.res.ResourcesCompat
import android.view.ViewGroup
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.base_mvi.load_refresh.list.recyclerview.BaseRecyclerViewHolder
import com.qwert2603.spenddemo.model.entity.Change
import com.qwert2603.spenddemo.model.entity.ChangeKind
import kotlinx.android.synthetic.main.item_change.view.*

class ChangeViewHolder(parent: ViewGroup) : BaseRecyclerViewHolder<Change>(parent, R.layout.item_change) {
    override fun bind(m: Change) = with(itemView) {
        super.bind(m)

        kind_TextView.text = m.changeKind.name
        kind_TextView.setTextColor(ResourcesCompat.getColor(resources, when (m.changeKind) {
            ChangeKind.INSERT -> R.color.local_change_create
            ChangeKind.UPDATE -> R.color.local_change_edit
            ChangeKind.DELETE -> R.color.local_change_delete
        }, null))

        recordId_TextView.text = m.recordId.toString()
    }
}