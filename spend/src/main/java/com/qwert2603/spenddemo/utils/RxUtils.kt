package com.qwert2603.spenddemo.utils

import android.view.MenuItem
import com.jakewharton.rxbinding2.view.RxMenuItem
import com.qwert2603.andrlib.util.Quint
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.functions.Function5
import java.lang.NumberFormatException

fun <T : Any, R> Observable<T>.castAndFilter(toClass: Class<R>): Observable<R> = this
        .filter { it.javaClass.isAssignableFrom(toClass) }
        .cast(toClass)

fun Observable<String>.mapToInt(): Observable<Int> = this.map {
    try {
        it.toInt()
    } catch (e: NumberFormatException) {
        0
    }
}

fun <T, U> makePair() = BiFunction { t: T, u: U -> Pair(t, u) }
fun <T, U> firstOfTwo() = BiFunction { t: T, _: U -> t }
fun <T, U> secondOfTwo() = BiFunction { _: T, u: U -> u }
fun <T, U, V> makeTriple() = Function3 { t: T, u: U, v: V -> Triple(t, u, v) }
fun <T, U, V, W, X> makeQuint() = Function5 { t: T, u: U, v: V, w: W, x: X -> Quint(t, u, v, w, x) }

fun MenuItem.checkedChanges(): Observable<Boolean> = RxMenuItem.clicks(this).map { !this.isChecked }