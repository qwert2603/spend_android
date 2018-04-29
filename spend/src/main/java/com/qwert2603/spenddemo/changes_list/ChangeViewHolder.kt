package com.qwert2603.spenddemo.changes_list

import android.view.ViewGroup
import com.qwert2603.andrlib.base.recyclerview.BaseRecyclerViewHolder
import com.qwert2603.andrlib.util.color
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.model.entity.Change
import com.qwert2603.spenddemo.model.entity.ChangeKind
import kotlinx.android.synthetic.main.item_change.view.*

class ChangeViewHolder(parent: ViewGroup) : BaseRecyclerViewHolder<Change>(parent, R.layout.item_change) {
    override fun bind(m: Change) = with(itemView) {
        super.bind(m)

        kind_TextView.text = m.changeKind.name
        kind_TextView.setTextColor(resources.color(when (m.changeKind) {
            ChangeKind.INSERT -> R.color.local_change_create
            ChangeKind.UPDATE -> R.color.local_change_edit
            ChangeKind.DELETE -> R.color.local_change_delete
        }))

        recordId_TextView.text = m.recordId.toString()
    }
}