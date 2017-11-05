package com.qwert2603.syncprocessor.sender

import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject

class LocalDataSender(
        scheduler: Scheduler
) {

    private val tasks: PublishSubject<Completable> = PublishSubject.create()

    init {
        tasks
                .observeOn(scheduler)
                .concatMap { it.toObservable<Nothing>() }
                .subscribe()
    }

    fun send(completable: Completable) {
        tasks.onNext(completable)
    }

}