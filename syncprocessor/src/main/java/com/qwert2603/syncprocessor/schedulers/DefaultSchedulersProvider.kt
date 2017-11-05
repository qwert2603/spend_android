package com.qwert2603.syncprocessor.schedulers

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

class DefaultSchedulersProvider : SchedulersProvider {
    override val localDataSourceScheduler: Scheduler = Schedulers.io()
    override val remoteDataSourceScheduler: Scheduler = Schedulers.io()
}