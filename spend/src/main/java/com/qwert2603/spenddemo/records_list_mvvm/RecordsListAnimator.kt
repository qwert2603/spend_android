package com.qwert2603.spenddemo.records_list_mvvm

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Path
import android.graphics.Rect
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.qwert2603.andrlib.util.color
import com.qwert2603.andrlib.util.toPx
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.utils.doOnEnd
import com.qwert2603.spenddemo.utils.getGlobalVisibleRectRightNow
import kotlinx.android.synthetic.main.item_spend.view.*
import java.util.concurrent.ConcurrentHashMap

class RecordsListAnimator(private val spendOrigin: SpendOrigin?) : DefaultItemAnimator() {

    interface SpendOrigin {
        fun getDateGlobalVisibleRect(): Rect
        fun getKindGlobalVisibleRect(): Rect
        fun getValueGlobalVisibleRect(): Rect
    }

    // pair is <Animator, cancel_action>.
    private val createSpendAnimators = ConcurrentHashMap<RecyclerView.ViewHolder, Pair<Animator, () -> Unit>>()

    var pendingCreatedSpendId: Long? = null
    var pendingCreatedProfitId: Long? = null

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        if (holder is SpendViewHolder && holder.t?.id == pendingCreatedSpendId
                || holder is ProfitViewHolder && holder.t?.id == pendingCreatedProfitId) {

            if (holder is SpendViewHolder) pendingCreatedSpendId = null
            if (holder is ProfitViewHolder) pendingCreatedProfitId = null

            endAnimation(holder)

            val animators = mutableListOf<Animator>()
            val resetActions = mutableListOf<() -> Unit>()

            val prevBackground = holder.itemView.background
            val resetHighlightAction = { holder.itemView.background = prevBackground }
            val highlightAnimator = ValueAnimator.ofFloat(0f, 1f)
                    .also {
                        val th = 0.25f
                        it.setInterpolator {
                            when {
                                it < th -> it / th
                                it > 1f - th -> (1f - it) / th
                                else -> 1f
                            }
                        }
                        val color = holder.itemView.resources.color(R.color.highlight_created_record)
                        fun Float.makeColor(): Int {
                            val alpha = (Color.alpha(color) * this).toInt()
                            return color and 0x00_ff_ff_ff or (alpha shl 24)
                        }
                        it.addUpdateListener { holder.itemView.setBackgroundColor((it.animatedValue as Float).makeColor()) }
                        it.duration = 2000
                        it.doOnEnd(resetHighlightAction)
                    }
            animators.add(highlightAnimator)
            resetActions.add(resetHighlightAction)

            val recyclerView = holder.itemView.parent as View
            val spendOrigin = spendOrigin
            if (holder is SpendViewHolder && spendOrigin != null) {

                // todo: don't do it.
                recyclerView.elevation = holder.itemView.resources.toPx(4).toFloat()

                listOf(
                        createTranslationAnimator(spendOrigin.getDateGlobalVisibleRect(), holder.itemView.date_TextView, 100),
                        createTranslationAnimator(spendOrigin.getKindGlobalVisibleRect(), holder.itemView.kind_TextView, 170),
                        createTranslationAnimator(spendOrigin.getValueGlobalVisibleRect(), holder.itemView.value_TextView, 240)
                ).forEach {
                    animators.add(it.first)
                    resetActions.add(it.second)
                }
            }

            val animatorSet = AnimatorSet()
            animatorSet.playTogether(animators)
            animatorSet.doOnEnd {
                dispatchAnimationFinished(holder)
                createSpendAnimators.remove(holder)
                recyclerView.elevation = 0f
            }

            createSpendAnimators[holder] = Pair(
                    animatorSet,
                    { resetActions.forEach { it() } }
            )
            animatorSet.start()
            return false
        }
        return super.animateAdd(holder)
    }

    override fun endAnimation(item: RecyclerView.ViewHolder?) {
        createSpendAnimators.remove(item)?.apply {
            first.cancel()
            second.invoke()
        }
        super.endAnimation(item)
    }

    override fun endAnimations() {
        createSpendAnimators.forEach { endAnimation(it.key) }
        createSpendAnimators.clear()
        super.endAnimations()
    }

    private fun createTranslationAnimator(originGlobalVisibleRect: Rect, target: View, delay: Long): Pair<Animator, () -> Unit> {
        val targetGlobalVisibleRect = target.getGlobalVisibleRectRightNow()
        val translationXDate = (originGlobalVisibleRect.left - targetGlobalVisibleRect.left).toFloat()
        val translationYDate = (originGlobalVisibleRect.centerY() - targetGlobalVisibleRect.centerY()).toFloat()
        // +4dp because EditText's text has padding.
        target.translationX = translationXDate + target.resources.toPx(4)
        target.translationY = translationYDate
        val pathDate = Path()
        pathDate.moveTo(translationXDate, translationYDate)
        pathDate.lineTo(0f, 0f)
        val resetAction = {
            target.translationX = 0f
            target.translationY = 0f
        }
        return ObjectAnimator
                .ofFloat(target, "translationX", "translationY", pathDate)
                .setDuration(500)
                .also { it.startDelay = delay }
                .also { it.interpolator = AccelerateDecelerateInterpolator() }
                .doOnEnd(resetAction)
                .let { Pair(it, resetAction) }
    }
}