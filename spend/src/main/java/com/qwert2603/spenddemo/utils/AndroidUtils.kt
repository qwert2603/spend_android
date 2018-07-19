package com.qwert2603.spenddemo.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.qwert2603.andrlib.util.LogUtils
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import kotlin.math.absoluteValue

inline fun <T, R1 : Comparable<R1>, R2 : Comparable<R2>> Iterable<T>.sortedBy(
        crossinline first: (T) -> R1,
        crossinline second: (T) -> R2
) = this.sortedWith(kotlin.Comparator { i1, i2 ->
    kotlin.comparisons.compareBy(first)
            .compare(i1, i2)
            .takeIf { it != 0 }
            ?: kotlin.comparisons.compareBy(second)
                    .compare(i1, i2)
})

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

fun String.pointedToInt() = this.filter { it != '.' }.toInt()

fun <T> List<T>.indexOfFirst(startIndex: Int, predicate: (T) -> Boolean): Int {
    for (i in startIndex..lastIndex) {
        if (predicate(this[i])) return i
    }
    return -1
}

fun <T, U> LiveData<T>.map(mapper: (T) -> U): LiveData<U> = Transformations.map(this, mapper)
fun <T, U> LiveData<T>.switchMap(func: (T) -> LiveData<U>): LiveData<U> = Transformations.switchMap(this, func)

fun <T, U> LiveData<T>.mapBG(mapper: Mapper<T, U>): LiveData<U> {
    val result = MediatorLiveData<U>()
    LogUtils.d("mapBG qq")
    result.addSource(this) { x ->
        if (x == null) {
            result.value = null
            return@addSource
        }
        LogUtils.d("mapBG onChanged 1")
        val coroutineContext = newSingleThreadContext("mapBG ${Random().nextInt()}")
        val job = async(coroutineContext) {
            LogUtils.d("mapBG bg")
            mapper(x)
                    .also { LogUtils.d("mapBG bg end") }
        }
        LogUtils.d("mapBG onChanged 2")
        launch(UI) {
            LogUtils.d("mapBG launch(UI) 1")
            result.value = job.await()
            LogUtils.d("mapBG launch(UI) 2")
        }
    }
    LogUtils.d("mapBG ww")
    return result
}

fun <T, U, V> combineLatest(liveDataT: LiveData<T>, liveDataU: LiveData<U>, combiner: (T, U) -> V, startT: T? = null, startU: U? = null) = MediatorLiveData<V>()
        .apply {
            var lastT: T? = startT
            var lastU: U? = startU

            fun update() {
                val localLastT = lastT ?: return
                val localLastU = lastU ?: return
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

fun <T> combineLatest(liveDatas: List<LiveData<T>>) = MediatorLiveData<List<T>>()
        .apply {
            val lasts: MutableList<T?> = (1..liveDatas.size).map { null }.toMutableList()

            fun update() {
                lasts.toList()
                        .takeIf { it.all { it != null } }
                        ?.let { value = it.map { it!! } }
            }
            liveDatas.forEachIndexed { index, liveData ->
                addSource(liveData) {
                    lasts[index] = it
                    update()
                }
            }
        }

/** Pair is <prev, current>. */
fun <T> LiveData<T>.pairWithPrev(): LiveData<Pair<T?, T?>> {
    val result = MediatorLiveData<Pair<T?, T?>>()
    var prev: T? = null
    result.addSource(this) {
        result.value = prev to it
        prev = it
    }
    return result
}

object LDUtils {
    fun <T> just(t: T): LiveData<T> = MutableLiveData<T>().also { it.value = t }
}

fun <T> ExecutorService.executeAndWait(action: () -> T): T = submit(Callable<T> { action() }).get()