package com.qwert2603.spenddemo.model.schedulers

import io.reactivex.Scheduler

interface UiSchedulerProvider {
    val ui: Scheduler
    fun isOnUi(): Boolean
}