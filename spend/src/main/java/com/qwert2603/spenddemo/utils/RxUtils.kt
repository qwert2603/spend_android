package com.qwert2603.spenddemo.utils

import com.qwert2603.andrlib.util.Quint
import io.reactivex.Observable
import io.reactivex.functions.*
import java.lang.NumberFormatException
import java.util.concurrent.atomic.AtomicLong

fun <T : Any, R> Observable<T>.castAndFilter(toClass: Class<R>): Observable<R> = this
        .filter { it.javaClass.isAssignableFrom(toClass) }
        .cast(toClass)

inline fun <T, R> Observable<T>.mapNotNull(crossinline mapper: (T) -> R?): Observable<R> = this
        .filter { mapper(it) != null }
        .map { mapper(it)!! }

fun Observable<String>.mapToInt(): Observable<Int> = this.map {
    try {
        it.pointedToInt()
    } catch (e: NumberFormatException) {
        0
    }
}

fun <T, U> makePair() = BiFunction { t: T, u: U -> Pair(t, u) }
fun <T, U> firstOfTwo() = BiFunction { t: T, _: U -> t }
fun <T, U> secondOfTwo() = BiFunction { _: T, u: U -> u }
fun <T, U, V> makeTriple() = Function3 { t: T, u: U, v: V -> Triple(t, u, v) }
fun <T, U, V, W, X> makeQuint() = Function5 { t: T, u: U, v: V, w: W, x: X -> Quint(t, u, v, w, x) }
fun <T, U, V, W, X, Y> makeSextuple() = Function6 { t: T, u: U, v: V, w: W, x: X, y: Y -> Sextuple(t, u, v, w, x, y) }

inline fun <T> Observable<T>.doOnNextIndexed(crossinline action: (T, Long) -> Unit): Observable<T> = this
        .doOnNext(object : Consumer<T> {
            private val nextIndex = AtomicLong(0L)
            override fun accept(t: T) {
                action(t, nextIndex.getAndIncrement())
            }
        })

fun <T> Observable<T>.shareReplayLast() = this
        .replay(1)
        .refCount()