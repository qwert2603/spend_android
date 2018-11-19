package com.qwert2603.spenddemo.utils

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import com.qwert2603.andrlib.util.LogUtils


object FastDiffUtils {

    data class FastDiffResult(
            val removes: List<Pair<Int, Int>>,
            val inserts: List<Pair<Int, Int>>,
            val changes: List<Int>
    ) {
        companion object {
            val EMPTY = FastDiffResult(emptyList(), emptyList(), emptyList())
        }

        fun dispatchToAdapter(adapter: RecyclerView.Adapter<*>) {
            changes.forEach { adapter.notifyItemChanged(it) }
            removes.forEach { adapter.notifyItemRangeRemoved(it.first, it.second) }
            inserts.forEach { adapter.notifyItemRangeInserted(it.first, it.second) }
        }
    }

    /**
     * Optimized version on [DiffUtil].
     * Calculates inserts / removes / changes of items in list.
     * All items in lists must have unique ids. [id]
     * All items in lists must be sorted by [compareOrder].
     * [isEqual] is used to determine if same item is changed.
     */
    inline fun <T : Any, I : Any> fastCalculateDiff(
            oldList: List<T>,
            newList: List<T>,
            crossinline id: T.() -> I,
            crossinline compareOrder: (T, T) -> Int,
            crossinline isEqual: (T, T) -> Boolean
    ): FastDiffResult {

        val currentTimeMillis = System.currentTimeMillis()

        val inserts = mutableListOf<Pair<Int, Int>>()
        val removes = mutableListOf<Pair<Int, Int>>()
        val changes = mutableListOf<Int>() // todo: calculate payload.

        var oldIndex = 0
        var newIndex = 0

        var insertsCount = 0
        var removesCount = 0

        while (oldIndex < oldList.size && newIndex < newList.size) {
            if (oldList[oldIndex].id() == newList[newIndex].id()) {
                if (!isEqual(oldList[oldIndex], newList[newIndex])) {
                    changes.add(oldIndex)
                }
                ++oldIndex
                ++newIndex
                continue
            }
            if (compareOrder(oldList[oldIndex], newList[newIndex]) < 0) {
                var count = 1
                ++oldIndex
                while (oldIndex < oldList.size && compareOrder(oldList[oldIndex], newList[newIndex]) < 0) {
                    ++count
                    ++oldIndex
                }
                removes.add(oldIndex - count - removesCount to count)
                removesCount += count
            } else {
                var count = 1
                ++newIndex
                while (newIndex < newList.size && compareOrder(oldList[oldIndex], newList[newIndex]) > 0) {
                    ++count
                    ++newIndex
                }
                inserts.add(oldIndex + insertsCount - removesCount to count)
                insertsCount += count
            }
        }
        if (oldIndex < oldList.size && newIndex == newList.size) {
            removes.add(oldIndex - removesCount to oldList.size - oldIndex)
        }
        if (newIndex < newList.size && oldIndex == oldList.size) {
            inserts.add(oldIndex + insertsCount - removesCount to newList.size - newIndex)
        }

        LogUtils.d("timing_ FastDiffUtils.fastCalculateDiff() ${System.currentTimeMillis() - currentTimeMillis} ms")

        return FastDiffResult(inserts = inserts, removes = removes, changes = changes)
    }

}