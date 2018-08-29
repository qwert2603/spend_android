package com.qwert2603.spenddemo.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.Transformations
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.widget.Button
import io.reactivex.functions.BiFunction
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import kotlinx.coroutines.experimental.withContext
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import kotlin.math.absoluteValue
import kotlin.reflect.KFunction2

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

fun Int.toPointedString() = toLong().toPointedString()

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

fun <T, U> LiveData<T>.mapBG(suspendMapper: SuspendMapper<T, U>): LiveData<U> {
    val result = MediatorLiveData<U>()
    val coroutineContext = newSingleThreadContext("mapBG ${UUID.randomUUID()}")
    result.addSource(this, Observer { x ->
        x ?: return@Observer
        launch(UI) {
            result.value = withContext(coroutineContext) { suspendMapper(x) }
        }
    })
    return result
}

fun <T, U, V> combineLatest(
        liveDataT: LiveData<T>,
        liveDataU: LiveData<U>,
        combiner: (T, U) -> V,
        startT: T? = null,
        startU: U? = null
) = MediatorLiveData<V>()
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

object LDUtils {
    fun <T> just(t: T): LiveData<T> = MutableLiveData<T>().also { it.value = t }
}

inline fun <T> ExecutorService.executeAndWait(crossinline action: () -> T): T = submit(Callable<T> { action() }).get()

fun <T> List<T>.reduceEmptyToNull(reducer: (T, T) -> T): T? = if (this.isEmpty()) null else this.reduce(reducer)

fun <T, U, R> KFunction2<T, U, R>.toRxBiFunction() = BiFunction { t: T, u: U -> this.invoke(t, u) }

fun Intent.getLongExtraNullable(key: String) =
        if (hasExtra(key)) {
            getLongExtra(key, 0)
        } else {
            null
        }

val AlertDialog.positiveButton: Button
    get() = getButton(DialogInterface.BUTTON_POSITIVE)