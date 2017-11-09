package com.qwert2603.syncprocessor.sender

import com.qwert2603.syncprocessor.logger.Logger
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.Executors

class LocalDataSender(
        private val logger: Logger
) {

    private val tasks: PublishSubject<Completable> = PublishSubject.create()

    init {
        tasks
                .observeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
                .concatMap {
                    it.toObservable<Nothing>()
                            .doOnError { logger.e("LocalDataSender", "error sending change to local!", it) }
                }
                .subscribe()
    }

    fun send(completable: Completable) {
        tasks.onNext(completable)
    }

}