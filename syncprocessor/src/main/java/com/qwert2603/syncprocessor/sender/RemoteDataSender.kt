package com.qwert2603.syncprocessor.sender

import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import java.util.concurrent.ConcurrentHashMap

class RemoteDataSender<in I>(
        private val scheduler: Scheduler
) {

    private val disposables = ConcurrentHashMap<I, Disposable>()
    private val tasks = ConcurrentHashMap<I, Completable>()

    fun send(id: I, completable: Completable, force: Boolean) {
        val currentDisposable = disposables[id]
        if (currentDisposable != null) {
            if (force) {
                tasks.put(id, completable)
                currentDisposable.dispose()
            }
            return
        }
        val disposable = completable
                .doOnDispose { onCompletableFinished(id) }
                .doOnTerminate { onCompletableFinished(id) }
                .onErrorComplete()
                .subscribeOn(scheduler)
                .subscribe()
        disposables.put(id, disposable)
    }

    private fun onCompletableFinished(id: I) {
        disposables.remove(id)
        tasks.remove(id)?.also { send(id, it, true) }
    }
}