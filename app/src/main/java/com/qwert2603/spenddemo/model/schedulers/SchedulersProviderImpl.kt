package com.qwert2603.spenddemo.model.schedulers

import android.os.Looper
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SchedulersProviderImpl : UiSchedulerProvider, ModelSchedulersProvider {
    override val ui: Scheduler = AndroidSchedulers.mainThread()
    override fun isOnUi() = Looper.myLooper() == Looper.getMainLooper()
    override val io: Scheduler = Schedulers.io()
    override val computation: Scheduler = Schedulers.computation()
}