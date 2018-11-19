package com.qwert2603.spenddemo.records_list

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Path
import android.graphics.Rect
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.qwert2603.andrlib.util.toPx
import com.qwert2603.spenddemo.R
import com.qwert2603.spenddemo.records_list.vh.RecordViewHolder
import com.qwert2603.spenddemo.spend_draft.DraftViewImpl
import com.qwert2603.spenddemo.utils.AnimatorUtils
import com.qwert2603.spenddemo.utils.Const
import com.qwert2603.spenddemo.utils.doOnEnd
import com.qwert2603.spenddemo.utils.getGlobalVisibleRectRightNow
import kotlinx.android.synthetic.main.item_record.view.*
import java.util.concurrent.ConcurrentHashMap

class RecordsListAnimator(private val spendOrigin: SpendOrigin?) : DefaultItemAnimator() {

    interface SpendOrigin {
        fun getDateGlobalVisibleRect(): Rect
        fun getKindGlobalVisibleRect(): Rect
        fun getValueGlobalVisibleRect(): Rect
    }

    // pair is <Animator, cancel_action>.
    private val createRecordAnimators = ConcurrentHashMap<RecyclerView.ViewHolder, Pair<Animator, () -> Unit>>()

    var pendingCreatedRecordUuid: String? = null

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        if (holder is RecordViewHolder && holder.t?.uuid == pendingCreatedRecordUuid) {

            pendingCreatedRecordUuid = null

            endAnimation(holder)

            val animators = mutableListOf<Animator>()
            val resetActions = mutableListOf<() -> Unit>()

            AnimatorUtils.animateHighlight(
                    view = holder.itemView,
                    colorRes = R.color.highlight_created_record
            ).also {
                animators.add(it.animator)
                resetActions.add(it.resetAction)
            }

            val recyclerView = holder.itemView.parent as View
            val spendOrigin = spendOrigin
            if (holder.t?.recordTypeId == Const.RECORD_TYPE_ID_SPEND && spendOrigin != null) {

                /** created item should be above spendOrigin ([DraftViewImpl]) */
                recyclerView.elevation = holder.itemView.resources.toPx(12).toFloat()

                listOf(
                        createTranslationAnimator(spendOrigin.getDateGlobalVisibleRect(), holder.itemView.date_TextView, 100),
                        createTranslationAnimator(spendOrigin.getDateGlobalVisibleRect(), holder.itemView.time_TextView, 130),
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
                createRecordAnimators.remove(holder)
                recyclerView.elevation = 0f
            }

            createRecordAnimators[holder] = Pair(
                    animatorSet,
                    { resetActions.forEach { it() } }
            )
            animatorSet.start()
            return false
        }
        return super.animateAdd(holder)
    }

    override fun endAnimation(item: RecyclerView.ViewHolder) {
        createRecordAnimators.remove(item)?.apply {
            first.cancel()
            second.invoke()
        }
        super.endAnimation(item)
    }

    override fun endAnimations() {
        createRecordAnimators.forEach { endAnimation(it.key) }
        createRecordAnimators.clear()
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