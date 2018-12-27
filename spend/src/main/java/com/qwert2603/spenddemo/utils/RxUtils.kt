package com.qwert2603.spenddemo.utils

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import com.qwert2603.andrlib.util.Quad
import com.qwert2603.andrlib.util.Quint
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

object RxUtils {
    fun dateChanges(): Observable<Any> = Observable
            .interval(300, TimeUnit.MILLISECONDS)
            .map { DateUtils.getNow().first }
            .distinctUntilChanged()
            .skip(1)
            .cast(Any::class.java)

    fun minuteChanges(): Observable<Any> = Observable
            .interval(300, TimeUnit.MILLISECONDS)
            .map { DateUtils.getNow() }
            .distinctUntilChanged()
            .skip(1)
            .cast(Any::class.java)
}

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
fun <T, U, V, W> makeQuad() = Function4 { t: T, u: U, v: V, w: W -> Quad(t, u, v, w) }
fun <T, U, V, W> makeQuad3() = BiFunction { (t, u, v): Triple<T, U, V>, w: W -> Quad(t, u, v, w) }
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

fun Disposable.disposeOnPause(lifecycleOwner: LifecycleOwner) {
    lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onPause() {
            this@disposeOnPause.dispose()
        }
    })
}