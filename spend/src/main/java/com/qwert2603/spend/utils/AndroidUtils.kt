package com.qwert2603.spend.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import io.reactivex.functions.BiFunction
import java.lang.ref.WeakReference
import java.security.MessageDigest
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import kotlin.math.absoluteValue
import kotlin.reflect.KFunction2
import kotlin.reflect.KMutableProperty0
import android.app.AlertDialog as SystemDialog
import androidx.appcompat.app.AlertDialog as AppCompatDialog

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

private val AppCompatDialog.positiveButton: Button
    get() = getButton(DialogInterface.BUTTON_POSITIVE)

private val SystemDialog.positiveButton: Button
    get() = getButton(DialogInterface.BUTTON_POSITIVE)

private val AppCompatDialog.neutralButton: Button
    get() = getButton(DialogInterface.BUTTON_NEUTRAL)

private val SystemDialog.neutralButton: Button
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

fun <T> HashSet<T>.toggle(itemToToggle: T): HashSet<T> =
        if (itemToToggle in this) {
            this.filterTo(HashSet(size)) { it != itemToToggle }
        } else {
            HashSet<T>(size + 1)
                    .also {
                        it.addAll(this)
                        it.add(itemToToggle)
                    }
        }


fun String.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(this.toByteArray(charset("UTF-8")))
    val hexString = StringBuffer()

    for (i in hash.indices) {
        val hex = Integer.toHexString(0xff and hash[i].toInt())
        if (hex.length == 1) hexString.append('0')
        hexString.append(hex)
    }

    return hexString.toString()
}

fun <T> KMutableProperty0<T>.getLateInitOrNull(): T? = try {
    get()
} catch (ignored: UninitializedPropertyAccessException) {
    null
}

fun Bundle?.toMap(): Map<String, Any?> =
        if (this != null) {
            this
                    .keySet()
                    .map { it to this[it] }
                    .toMap()
        } else {
            emptyMap()
        }

infix fun Map<*, *>.sameIn(anth: Map<*, *>): Boolean {
    // = keys.all { this[it] != anth[it] }
    this.keys.forEach {
        if (this[it] != anth[it]) return false
    }
    return true
}

private val LAST_NAVS_MILLIS = mutableMapOf<WeakReference<NavController>, Long>()

private fun NavController.canNavigate(): Boolean {
    LAST_NAVS_MILLIS.keys
            .toList()
            .forEach {
                if (it.get() == null) LAST_NAVS_MILLIS.remove(it)
            }
    val weakReference = LAST_NAVS_MILLIS.keys
            .find { it.get() == this }
            ?: WeakReference(this)
    val lastMillis = LAST_NAVS_MILLIS.getOrElse(weakReference) { 0L }
    val nowMillis = SystemClock.elapsedRealtime()
    if (lastMillis < nowMillis - 500L) {
        LAST_NAVS_MILLIS[weakReference] = nowMillis
        return true
    }
    return false
}

fun NavController.navigateFixed(navDirections: NavDirections, navOptions: NavOptions? = null) {
    if (canNavigate()) navigate(navDirections, navOptions)
}

fun NavController.navigateFixed(@IdRes resId: Int, args: Bundle) {
    if (canNavigate()) navigate(resId, args)
}
