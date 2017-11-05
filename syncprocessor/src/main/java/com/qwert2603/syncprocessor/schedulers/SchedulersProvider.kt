package com.qwert2603.syncprocessor.schedulers

import io.reactivex.Scheduler

interface SchedulersProvider {
    val localDataSourceScheduler: Scheduler
    val remoteDataSourceScheduler: Scheduler
}

