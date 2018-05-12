package com.qwert2603.spenddemo.records_list.vhs

import android.support.v4.widget.TextViewCompat
import android.util.TypedValue
import android.view.ViewGroup
import com.qwert2603.andrlib.base.recyclerview.BaseRecyclerViewHolder
import com.qwert2603.andrlib.util.color
import com.qwert2603.andrlib.util.setVisible
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.model.entity.ChangeKind
import com.qwert2603.spenddemo.model.entity.SyncStatus
import com.qwert2603.spenddemo.records_list.RecordsAdapter
import com.qwert2603.spenddemo.records_list.entity.SpendUI
import com.qwert2603.spenddemo.utils.setStrike
import com.qwert2603.spenddemo.utils.toFormattedString
import com.qwert2603.spenddemo.utils.toPointedString
import com.qwert2603.spenddemo.utils.zeroToEmpty
import kotlinx.android.synthetic.main.item_spend.view.*

class SpendViewHolder(parent: ViewGroup) : BaseRecyclerViewHolder<SpendUI>(parent, R.layout.item_spend) {

    init {
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                itemView.date_TextView,
                14,
                16,
                1,
                TypedValue.COMPLEX_UNIT_SP
        )
    }

    override fun bind(m: SpendUI) = with(itemView) {
        super.bind(m)

        val showIds = (adapter as? RecordsAdapter)?.showIds ?: true
        val showChangeKinds = (adapter as? RecordsAdapter)?.showChangeKinds ?: true
        val showDatesInRecords = (adapter as? RecordsAdapter)?.showDatesInRecords ?: true

        local_ImageView.setVisible(showChangeKinds)
        id_TextView.setVisible(showIds)
        date_TextView.setVisible(showDatesInRecords)

        local_ImageView.setImageResource(when (m.syncStatus) {
            SyncStatus.LOCAL -> R.drawable.ic_local
            SyncStatus.SYNCING -> R.drawable.ic_syncing
            SyncStatus.REMOTE -> R.drawable.ic_done_24dp
        })
        if (m.changeKind != null) {
            local_ImageView.setColorFilter(resources.color(when (m.changeKind) {
                ChangeKind.INSERT -> R.color.local_change_create
                ChangeKind.UPDATE -> R.color.local_change_edit
                ChangeKind.DELETE -> R.color.local_change_delete
            }))
        } else {
            local_ImageView.setColorFilter(resources.color(R.color.anth))
        }
        id_TextView.text = m.id.toString()
        date_TextView.text = m.date.toFormattedString(resources)
        kind_TextView.text = m.kind
        value_TextView.text = m.value.toLong().toPointedString().zeroToEmpty()

        isClickable = m.canEdit
        isLongClickable = m.canDelete

        val strike = showChangeKinds && m.changeKind == ChangeKind.DELETE
        listOf(id_TextView, date_TextView, kind_TextView, value_TextView)
                .forEach { it.setStrike(strike) }
    }
}