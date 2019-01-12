package com.qwert2603.spenddemo.utils

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Color
import android.support.annotation.ColorRes
import android.view.View
import com.qwert2603.andrlib.util.color

object AnimatorUtils {
    data class AnimateHighlightResult(
            val animator: Animator,
            val resetAction: () -> Unit
    )

    fun animateHighlight(
            view: View,
            @ColorRes colorRes: Int,
            duration: Long = 2000,
            th: Float = 0.25f
    ): AnimateHighlightResult {
        val resetHighlightAction = { view.background = null }
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.setInterpolator {
            when {
                it < th -> it / th
                it > 1f - th -> (1f - it) / th
                else -> 1f
            }
        }
        val color = view.resources.color(colorRes)
        fun Float.makeColor(): Int {
            val alpha = (Color.alpha(color) * this).toInt()
            return color and 0x00_ff_ff_ff or (alpha shl 24)
        }
        valueAnimator.addUpdateListener { view.setBackgroundColor((it.animatedValue as Float).makeColor()) }
        valueAnimator.duration = duration
        valueAnimator.doOnEnd(resetHighlightAction)
        return AnimateHighlightResult(
                animator = valueAnimator,
                resetAction = resetHighlightAction
        )
    }
}