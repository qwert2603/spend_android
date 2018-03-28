package com.qwert2603.spenddemo.utils

import android.content.res.Resources
import android.support.annotation.ColorRes
import android.support.v4.content.res.ResourcesCompat

fun Resources.color(@ColorRes colorId: Int, theme: Resources.Theme? = null) = ResourcesCompat.getColor(this, colorId, theme)

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