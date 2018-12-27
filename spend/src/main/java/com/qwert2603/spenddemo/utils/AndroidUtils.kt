package com.qwert2603.spenddemo.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.support.annotation.ColorRes
import android.support.v4.content.res.ResourcesCompat
import android.widget.Button
import io.reactivex.functions.BiFunction
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import kotlin.math.absoluteValue
import kotlin.reflect.KFunction2
import android.app.AlertDialog as SystemDialog
import android.support.v7.app.AlertDialog as AppCompatDialog

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

inline fun <T> ExecutorService.executeAndWait(crossinline action: () -> T): T = submit(Callable<T> { action() }).get()

fun <T> List<T>.reduceEmptyToNull(reducer: (T, T) -> T): T? = if (this.isEmpty()) null else this.reduce(reducer)

fun <T, U, R> KFunction2<T, U, R>.toRxBiFunction() = BiFunction { t: T, u: U -> this.invoke(t, u) }

fun Intent.getLongExtraNullable(key: String) =
        if (hasExtra(key)) {
            getLongExtra(key, 0)
        } else {
            null
        }

fun Intent.getIntExtraNullable(key: String) =
        if (hasExtra(key)) {
            getIntExtra(key, 0)
        } else {
            null
        }

val AppCompatDialog.positiveButton: Button
    get() = getButton(DialogInterface.BUTTON_POSITIVE)

val SystemDialog.positiveButton: Button
    get() = getButton(DialogInterface.BUTTON_POSITIVE)

val AppCompatDialog.neutralButton: Button
    get() = getButton(DialogInterface.BUTTON_NEUTRAL)

val SystemDialog.neutralButton: Button
    get() = getButton(DialogInterface.BUTTON_NEUTRAL)

val Dialog.positiveButton: Button
    get() = when (this) {
        is AppCompatDialog -> this.positiveButton
        is SystemDialog -> this.positiveButton
        else -> null!!
    }
val Dialog.neutralButton: Button
    get() = when (this) {
        is AppCompatDialog -> this.neutralButton
        is SystemDialog -> this.neutralButton
        else -> null!!
    }

fun Resources.colorStateList(@ColorRes colorRes: Int, theme: Resources.Theme? = null) = ResourcesCompat
        .getColorStateList(this, colorRes, theme)

fun <T> HashSet<T>.toggleAndFilter(itemToToggle: T, filter: (T) -> Boolean): HashSet<T> =
        if (itemToToggle in this) {
            this.filterTo(HashSet(size)) { filter(it) && it != itemToToggle }
        } else {
            HashSet<T>(size + 1)
                    .also {
                        it.addAll(this.filter(filter))
                        if (filter(itemToToggle)) it.add(itemToToggle)
                    }
        }