package com.qwert2603.spenddemo.utils

import com.qwert2603.spenddemo.model.schedulers.UiSchedulerProvider
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function

fun <T, U> Single<List<T>>.mapList(mapper: (T) -> U): Single<List<U>> = this
        .map { it.map(mapper) }

/**
 * Cancel Observable when [anth] emits item.
 * If [anth] emits items before Observable emit first item, it will not trigger cancellation.
 */
fun <T, U> Observable<T>.cancelOn(anth: Observable<U>, cancelItem: T): Observable<T> {
    val completed = Exception()
    return this
            .materialize()
            .map { if (it.isOnComplete) throw completed else it }
            .dematerialize<T>()
            .mergeWith(anth
                    .skipUntil(this@cancelOn)
                    .materialize()
                    .filter { it.value != null }
                    .map { cancelItem }
            )
            .takeUntil { it == cancelItem }
            .onErrorResumeNext { t: Throwable -> if (t === completed) Observable.empty() else Observable.error(t) }
}

fun <T> Observable<T>.switchToUiIfNotYet(uiSchedulerProvider: UiSchedulerProvider): Observable<T> = this
        .concatMap {
            Observable.just(it)
                    .compose { if (uiSchedulerProvider.isOnUi()) it else it.observeOn(uiSchedulerProvider.ui) }
        }

fun <T : Any, R> Observable<T>.castAndFilter(toClass: Class<R>): Observable<R> = this
        .filter { it.javaClass.isAssignableFrom(toClass) }
        .cast(toClass)

fun <T> Observable<T>.pausable(isOn: Observable<Boolean>): Observable<T> {
    abstract class Message

    val itemOn = object : Message() {}
    val itemOff = object : Message() {}

    class Item(val item: T) : Message()

    return Observable
            .merge(
                    this.map { Item(it) },
                    isOn.startWith(false).map { if (it) itemOn else itemOff }
            )
            .concatMap(object : Function<Message, Observable<T>> {
                private val buffer = mutableListOf<T>()
                private var on = false
                override fun apply(t: Message): Observable<T> = when (t) {
                    itemOn -> {
                        on = true
                        val b = ArrayList(buffer)
                        buffer.clear()
                        Observable.fromIterable(b)
                    }
                    itemOff -> {
                        on = false
                        Observable.empty<T>()
                    }
                    is Item -> {
                        if (on) {
                            Observable.just(t.item)
                        } else {
                            buffer += t.item
                            Observable.empty()
                        }
                    }
                    else -> null!!
                }
            })
}

fun Disposable.addTo(compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(this)
}