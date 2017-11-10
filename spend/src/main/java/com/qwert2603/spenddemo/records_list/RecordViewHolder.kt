package com.qwert2603.spenddemo.records_list

import android.support.v4.content.res.ResourcesCompat
import android.view.ViewGroup
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.base_mvi.load_refresh.list.recyclerview.BaseRecyclerViewHolder
import com.qwert2603.spenddemo.model.entity.ChangeKind
import com.qwert2603.spenddemo.model.entity.SyncStatus
import com.qwert2603.spenddemo.records_list.entity.RecordUI
import com.qwert2603.spenddemo.utils.Const
import com.qwert2603.spenddemo.utils.setStrike
import kotlinx.android.synthetic.main.item_record.view.*

class RecordViewHolder(parent: ViewGroup) : BaseRecyclerViewHolder<RecordUI>(parent, R.layout.item_record) {
    override fun bind(m: RecordUI) = with(itemView) {
        super.bind(m)

        local_ImageView.setImageResource(when (m.syncStatus) {
            SyncStatus.LOCAL -> R.drawable.ic_local
            SyncStatus.SYNCING -> R.drawable.ic_syncing
            SyncStatus.REMOTE -> R.drawable.ic_done_24dp
        })
        if (m.changeKind != null) {
            local_ImageView.setColorFilter(ResourcesCompat.getColor(resources, when (m.changeKind) {
                ChangeKind.INSERT -> R.color.local_change_create
                ChangeKind.UPDATE -> R.color.local_change_edit
                ChangeKind.DELETE -> R.color.local_change_delete
            }, null))
        } else {
            local_ImageView.setColorFilter(ResourcesCompat.getColor(resources, R.color.anth, null))
        }
        id_TextView.text = m.id.toString()
        date_TextView.text = Const.DATE_FORMAT.format(m.date)
        kind_TextView.text = m.kind
        value_TextView.text = m.value.toString()

        isClickable = m.canEdit
        isLongClickable = m.canDelete

        id_TextView.setStrike(m.changeKind == ChangeKind.DELETE)
        date_TextView.setStrike(m.changeKind == ChangeKind.DELETE)
        kind_TextView.setStrike(m.changeKind == ChangeKind.DELETE)
        value_TextView.setStrike(m.changeKind == ChangeKind.DELETE)
    }
}