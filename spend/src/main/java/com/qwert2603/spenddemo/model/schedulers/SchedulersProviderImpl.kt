package com.qwert2603.spenddemo.model.schedulers

import android.os.Looper
import com.qwert2603.andrlib.schedulers.ModelSchedulersProvider
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SchedulersProviderImpl @Inject constructor() : UiSchedulerProvider, ModelSchedulersProvider {
    override val ui: Scheduler = AndroidSchedulers.mainThread()
    override fun isOnUi() = Looper.myLooper() == Looper.getMainLooper()
    override val io: Scheduler = Schedulers.from(Executors.newFixedThreadPool(8))//todo
    override val computation: Scheduler = Schedulers.from(Executors.newFixedThreadPool(4))//todo
}