package com.qwert2603.spenddemo.records_list

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spenddemo.model.entity.*
import com.qwert2603.spenddemo.records_list.vh.*
import io.reactivex.subjects.PublishSubject
import java.lang.ref.WeakReference

class RecordsListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var showChangeKinds = true
        set(value) {
            if (value == field) return
            field = value
            redrawViewHolders()
        }

    var showDatesInRecords = true
        set(value) {
            if (value == field) return
            field = value
            redrawViewHolders()
        }

    var showTimesInRecords = true
        set(value) {
            if (value == field) return
            field = value
            redrawViewHolders()
        }

    @Suppress("UNCHECKED_CAST")
    private fun redrawViewHolders() {
        LogUtils.d("RecordsListAdapter redrawVisibleViewHolders")
        vhs.forEach {
            val viewHolder = it.get()
            val recordsListItem = viewHolder?.t
            if (viewHolder != null && recordsListItem != null) {
                viewHolder.bind(recordsListItem, this)
            }
        }
    }

    var list: List<RecordsListItem> = emptyList()

    var itemClicks = PublishSubject.create<RecordsListItem>()
    var itemLongClicks = PublishSubject.create<RecordsListItem>()

    private val vhs = mutableSetOf<WeakReference<BaseViewHolder<RecordsListItem>>>()

    override fun getItemCount() = list.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        VIEW_TYPE_RECORD -> RecordViewHolder(parent)
        VIEW_TYPE_DATE_SUM -> DaySumViewHolder(parent)
        VIEW_TYPE_MONTH_SUM -> MonthSumViewHolder(parent)
        VIEW_TYPE_YEAR_SUM -> YearSumViewHolder(parent)
        VIEW_TYPE_TOTALS -> TotalsViewHolder(parent)
        else -> null!!
    }.also {
        @Suppress("UNCHECKED_CAST")
        vhs.add(WeakReference(it as BaseViewHolder<RecordsListItem>))
        LogUtils.d { "RecordsListAdapter onCreateViewHolder $it" }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        LogUtils.d { "RecordsListAdapter onBindViewHolder $holder" }
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

    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        LogUtils.e("RecordsListAdapter onFailedToRecycleView $holder")
        return super.onFailedToRecycleView(holder)
    }

    companion object {
        const val VIEW_TYPE_RECORD = 1
        const val VIEW_TYPE_DATE_SUM = 2
        const val VIEW_TYPE_MONTH_SUM = 3
        const val VIEW_TYPE_YEAR_SUM = 4
        const val VIEW_TYPE_TOTALS = 5
    }
}