package com.qwert2603.spenddemo.utils

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import java.lang.NumberFormatException
import java.util.*

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