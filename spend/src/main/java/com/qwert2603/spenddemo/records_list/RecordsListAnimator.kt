package com.qwert2603.spenddemo.records_list

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.RecyclerView
import com.qwert2603.spenddemo.R

class RecordsListAnimator : DefaultItemAnimator() {

    companion object {
        const val PAYLOAD_HIGHLIGHT = "PAYLOAD_HIGHLIGHT"
    }

    class Q : RecyclerView.ItemAnimator.ItemHolderInfo()

    private val highlightAnimators = mutableMapOf<RecyclerView.ViewHolder, Animator>()

    override fun recordPreLayoutInformation(state: RecyclerView.State, viewHolder: RecyclerView.ViewHolder, changeFlags: Int, payloads: MutableList<Any>): ItemHolderInfo {
        if (PAYLOAD_HIGHLIGHT in payloads) return Q()
        return super.recordPreLayoutInformation(state, viewHolder, changeFlags, payloads)
    }

    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder, payloads: MutableList<Any>): Boolean {
        return PAYLOAD_HIGHLIGHT in payloads || super.canReuseUpdatedViewHolder(viewHolder, payloads)
    }

    override fun animateChange(oldHolder: RecyclerView.ViewHolder, newHolder: RecyclerView.ViewHolder, preInfo: ItemHolderInfo, postInfo: ItemHolderInfo): Boolean {
        if (preInfo is Q) {
            val animator = ValueAnimator.ofFloat(0f, 1f)
                    .also {
                        val th = 0.15f
                        it.setInterpolator {
                            when {
                                it < th -> it / th
                                it > 1f - th -> (1f - it) / th
                                else -> 1f
                            }
                        }
                        val color = ResourcesCompat.getColor(oldHolder.itemView.resources, R.color.highlight_created_record, null)
                        fun Float.makeColor() = color and ((255 * this).toInt() shl 24 or 0xffffff)
                        it.addUpdateListener { oldHolder.itemView.setBackgroundColor((it.animatedValue as Float).makeColor()) }
                        it.duration = 2000
                        it.addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                dispatchAnimationFinished(oldHolder)
                                oldHolder.itemView.background = null
                                highlightAnimators.remove(oldHolder)
                            }
                        })
                    }
            highlightAnimators.put(oldHolder, animator)
            animator.start()

            return false
        }
        return super.animateChange(oldHolder, newHolder, preInfo, postInfo)
    }


    override fun endAnimation(item: RecyclerView.ViewHolder?) {
        highlightAnimators.remove(item)?.cancel()
        super.endAnimation(item)
    }

    override fun endAnimations() {
        highlightAnimators.forEach { it.value.cancel() }
        highlightAnimators.clear()
        super.endAnimations()
    }
}