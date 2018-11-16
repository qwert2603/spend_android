package com.qwert2603.spenddemo.records_list_mvvm

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.qwert2603.spenddemo.model.entity.*
import com.qwert2603.spenddemo.records_list_mvvm.vh.*
import io.reactivex.subjects.PublishSubject

class RecordsListAdapter(private val recyclerView: RecyclerView) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var showChangeKinds = true
        set(value) {
            if (value == field) return
            field = value
            redrawVisibleViewHolders()
        }

    var showDatesInRecords = true
        set(value) {
            if (value == field) return
            field = value
            redrawVisibleViewHolders()
        }

    var showTimesInRecords = true
        set(value) {
            if (value == field) return
            field = value
            redrawVisibleViewHolders()
        }

    var syncingRecordsUuids: Set<String> = emptySet()
        set(value) {
            if (value == field) return
            field = value
            redrawVisibleViewHolders()
        }

    @Suppress("UNCHECKED_CAST")
    private fun redrawVisibleViewHolders() {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        for (i in layoutManager.findFirstVisibleItemPosition()..layoutManager.findLastVisibleItemPosition()) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(i)
            if (viewHolder as? BaseViewHolder<RecordsListItem> != null) {
                viewHolder.bind(viewHolder.t!!, this)
            }
        }
    }

    var list: List<RecordsListItem> = emptyList()

    var itemClicks = PublishSubject.create<RecordsListItem>()
    var itemLongClicks = PublishSubject.create<RecordsListItem>()

    override fun getItemCount() = list.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        VIEW_TYPE_RECORD -> RecordViewHolder(parent)
        VIEW_TYPE_DATE_SUM -> DaySumViewHolder(parent)
        VIEW_TYPE_MONTH_SUM -> MonthSumViewHolder(parent)
        VIEW_TYPE_YEAR_SUM -> YearSumViewHolder(parent)
        VIEW_TYPE_TOTALS -> TotalsViewHolder(parent)
        else -> null!!
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as BaseViewHolder<RecordsListItem>
        holder.bind(list[position], this)
    }

    override fun getItemViewType(position: Int) = when (list[position]) {
        is Record -> VIEW_TYPE_RECORD
        is DaySum -> VIEW_TYPE_DATE_SUM
        is MonthSum -> VIEW_TYPE_MONTH_SUM
        is YearSum -> VIEW_TYPE_YEAR_SUM
        is Totals -> VIEW_TYPE_TOTALS
        else -> null!!
    }

    companion object {
        const val VIEW_TYPE_RECORD = 1
        const val VIEW_TYPE_DATE_SUM = 2
        const val VIEW_TYPE_MONTH_SUM = 3
        const val VIEW_TYPE_YEAR_SUM = 4
        const val VIEW_TYPE_TOTALS = 5
    }
}