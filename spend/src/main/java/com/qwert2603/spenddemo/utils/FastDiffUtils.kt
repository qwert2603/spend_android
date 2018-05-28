package com.qwert2603.spenddemo.utils

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView


object FastDiffUtils {

    data class FastDiffResult(
            val inserts: List<Pair<Int, Int>>,
            val removes: List<Pair<Int, Int>>,
            val changes: List<Int>,
            val moves: List<Pair<Int, Int>>
    ) {
        fun dispatchToAdapter(adapter: RecyclerView.Adapter<*>) {
            moves.forEach { adapter.notifyItemMoved(it.first, it.second) }
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
            crossinline isEqual: (T, T) -> Boolean,
            possiblyMovedItemIds: List<I> = emptyList()
    ): FastDiffResult {

        val movedIds = mutableSetOf<I>()

        val inserts = mutableListOf<Pair<Int, Int>>()
        val removes = mutableListOf<Pair<Int, Int>>()
        val changes = mutableListOf<Int>()

        val moves = possiblyMovedItemIds.mapNotNull { movedId ->
            val fromPosition = oldList.indexOfFirst { it.id() == movedId }
            val toPosition = newList.indexOfFirst { it.id() == movedId }
            if (fromPosition >= 0 && toPosition >= 0) {
                movedIds.add(movedId)
                if (!isEqual(oldList[fromPosition], newList[toPosition])) {
                    changes.add(toPosition)
                }
                fromPosition to toPosition
            } else {
                null
            }
        }

        var oldIndex = 0
        var newIndex = 0

        var insertsCount = 0
        var removesCount = 0

        while (oldIndex < oldList.size && newIndex < newList.size) {
            if (oldList[oldIndex].id() in movedIds) {
                ++oldIndex
                continue
            }
            if (newList[newIndex].id() in movedIds) {
                ++newIndex
                continue
            }
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
            removes.add(oldIndex to oldList.size - oldIndex)
        }
        if (newIndex < newList.size && oldIndex == oldList.size) {
            inserts.add(oldIndex + insertsCount - removesCount to newList.size - newIndex)
        }

        return FastDiffResult(inserts = inserts, removes = removes, changes = changes, moves = moves)
    }

}