package com.qwert2603.spenddemo.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter

inline fun <T, R1 : Comparable<R1>, R2 : Comparable<R2>> Iterable<T>.sortedByDescending(
        crossinline first: (T) -> R1,
        crossinline second: (T) -> R2
) = this.sortedWith(kotlin.Comparator { i1, i2 ->
    kotlin.comparisons.compareByDescending(first)
            .compare(i1, i2)
            .takeIf { it != 0 }
            ?: kotlin.comparisons.compareByDescending(second)
                    .compare(i1, i2)
})

infix fun <T> List<T>?.plusNN(anth: List<T>?): List<T> = (this ?: emptyList()) + (anth
        ?: emptyList())

inline fun Animator.doOnEnd(crossinline action: () -> Unit): Animator {
    addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            action()
        }
    })
    return this
}

inline fun Animator.doOnStart(crossinline action: () -> Unit): Animator {
    addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator?) {
            action()
        }
    })
    return this
}