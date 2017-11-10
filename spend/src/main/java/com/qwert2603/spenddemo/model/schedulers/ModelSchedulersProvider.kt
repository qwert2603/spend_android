package com.qwert2603.spenddemo.model.schedulers

import io.reactivex.Scheduler

interface ModelSchedulersProvider {
    val io: Scheduler
    val computation: Scheduler
}