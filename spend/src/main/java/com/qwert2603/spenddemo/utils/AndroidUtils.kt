package com.qwert2603.spenddemo.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Transformations
import kotlin.math.absoluteValue

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

inline fun <T> Iterable<T>.sumByLong(crossinline selector: (T) -> Long): Long {
    var sum = 0L
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

fun Long.toPointedString(): String {
    val negative = this < 0
    val absString = this.absoluteValue.toString().reversed()
    val stringBuilder = StringBuilder()
    absString.forEachIndexed { index, c ->
        stringBuilder.append(c)
        if (index % 3 == 2 && index != absString.lastIndex) stringBuilder.append('.')
    }
    if (negative) stringBuilder.append('-')
    return stringBuilder.reverse().toString()
}

fun String.zeroToEmpty() = if (this == "0") "" else this

fun <T> List<T>.indexOfFirst(startIndex: Int, predicate: (T) -> Boolean): Int {
    for (i in startIndex..lastIndex) {
        if (predicate(this[i])) return i
    }
    return -1
}

fun <T, U> LiveData<T>.map(mapper: (T) -> U): LiveData<U> = Transformations.map(this, mapper)
fun <T, U> LiveData<T>.switchMap(func: (T) -> LiveData<U>): LiveData<U> = Transformations.switchMap(this, func)

fun <T, U, V> combineLatest(liveDataT: LiveData<T?>, liveDataU: LiveData<U?>, combiner: (T?, U?) -> V?) = MediatorLiveData<V?>()
        .apply {
            var lastT: T? = null
            var lastU: U? = null

            fun update() {
                val localLastT = lastT
                val localLastU = lastU
                value = combiner(localLastT, localLastU)
            }

            addSource(liveDataT) {
                lastT = it
                update()
            }
            addSource(liveDataU) {
                lastU = it
                update()
            }
        }

fun combineLatest(liveDatas: List<LiveData<Boolean>>) = MediatorLiveData<List<Boolean?>>()
        .apply {
            val lasts: MutableList<Boolean?> = (1..liveDatas.size).map { null }.toMutableList()

            fun update() {
                value = lasts.toList()
            }
            liveDatas.forEachIndexed { index, liveData ->
                addSource(liveData) {
                    lasts[index] = it
                    update()
                }
            }
        }