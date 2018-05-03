package com.qwert2603.spenddemo.utils

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

infix fun <T> List<T>?.plusNN(anth: List<T>?): List<T> = (this ?: emptyList()) + (anth ?: emptyList())