package com.qwert2603.spend.records_list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spend.model.entity.*
import com.qwert2603.spend.records_list.vh.*
import com.qwert2603.spend.utils.toPointedString
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import io.reactivex.subjects.PublishSubject
import java.lang.ref.WeakReference

class RecordsListAdapter(val isDaySumsClickable: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), FastScrollRecyclerView.SectionedAdapter {

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

    var showBalancesInSums = false
        set(value) {
            if (value == field) return
            field = value
            redrawViewHolders()
        }

    var selectedRecordsUuids: HashSet<String> = hashSetOf()
        set(value) {
            if (value == field) return
            field = value
            redrawViewHolders()
        }

    var selectMode = false
        set(value) {
            if (value == field) return
            field = value
            redrawViewHolders()
        }

    var sortByValue = false

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

    /**
     * changes are rendered separately,
     * because so it can be changes without breaking item's insert/change animation.
     */
    var recordsChanges: HashMap<String, RecordChange> = hashMapOf()
        set(value) {
            if (value == field) return
            field = value
            redrawViewHolders()
        }

    var list: List<RecordsListItem> = emptyList()
        set(value) {
            if (value == field) return
            field = value
            vhs.forEach { vhRef ->
                val viewHolder = vhRef.get()
                if (viewHolder as? RecordViewHolder != null) {
                    val uuid = viewHolder.t?.uuid
                    if (uuid != null) {
                        viewHolder.drawChange(recordsChanges[uuid])
                    }
                }
            }
        }

    var itemClicks = PublishSubject.create<RecordsListItem>()
    var itemLongClicks = PublishSubject.create<RecordsListItem>()

    private val vhs = mutableSetOf<WeakReference<BaseViewHolder<RecordsListItem>>>()

    override fun getItemCount() = list.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        VIEW_TYPE_RECORD -> RecordViewHolder(parent)
        VIEW_TYPE_DATE_SUM -> DaySumViewHolder(parent, isDaySumsClickable)
        VIEW_TYPE_MONTH_SUM -> MonthSumViewHolder(parent)
        VIEW_TYPE_YEAR_SUM -> YearSumViewHolder(parent)
        VIEW_TYPE_PERIOD_DIVIDER -> PeriodDividerViewHolder(parent)
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
        is PeriodDivider -> VIEW_TYPE_PERIOD_DIVIDER
        is Totals -> VIEW_TYPE_TOTALS
        else -> null!!
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        LogUtils.d { "RecordsListAdapter onViewRecycled $holder" }
        holder as BaseViewHolder<RecordsListItem>
        holder.unbind()
        super.onViewRecycled(holder)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        LogUtils.e("RecordsListAdapter onFailedToRecycleView $holder")
        holder as BaseViewHolder<RecordsListItem>
        holder.unbind()
        return super.onFailedToRecycleView(holder)
    }

    override fun getSectionName(position: Int): String = list[position]
            .let {
                when {
                    sortByValue -> (it as? Record)?.value?.toPointedString() ?: ""
                    it is Totals -> ""
                    else -> {
                        val date = it.date().date
                        String.format("%04d-%02d", date / (100 * 100), date / 100 % 100)
                    }
                }
            }

    companion object {
        const val VIEW_TYPE_RECORD = 1
        const val VIEW_TYPE_DATE_SUM = 2
        const val VIEW_TYPE_MONTH_SUM = 3
        const val VIEW_TYPE_YEAR_SUM = 4
        const val VIEW_TYPE_PERIOD_DIVIDER = 5
        const val VIEW_TYPE_TOTALS = 6
    }
}