package com.qwert2603.spenddemo.model.sync_processor

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.andrlib.util.addTo
import com.qwert2603.spenddemo.SpendDemoApplication
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.model.entity.SyncState
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class SyncWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    @Inject
    lateinit var syncProcessor: SyncProcessor

    private val disposable = CompositeDisposable()

    @Volatile
    var result: ListenableWorker.Result? = null

    init {
        DIHolder.diManager.viewsComponent.inject(this)
    }

    override fun doWork(): Result {

        LogUtils.d("SyncWorker doWork")
        SpendDemoApplication.debugHolder.logLine { "SyncWorker doWork" }

        syncProcessor.syncState
                .doOnNext { LogUtils.d("SyncWorker doOnNext $it")  }
                .filter { it != SyncState.SYNCING }
                .firstOrError()
                .subscribe { syncState, t ->
                    syncState?.let { LogUtils.d("SyncWorker subscribe $it") }
                    t?.let { LogUtils.e("SyncWorker subscribe", it) }
                    SpendDemoApplication.debugHolder.logLine { "SyncWorker subscribe $syncState $t" }

                    result = if (t == null && syncState == SyncState.SYNCED) {
                        Result.success()
                    } else {
                        Result.failure()
                    }
                }
                .addTo(disposable)

        while (result == null) Thread.yield()

        LogUtils.d("SyncWorker return $result")
        SpendDemoApplication.debugHolder.logLine { "SyncWorker return $result" }

        SpendDemoApplication.scheduleSync()

        return result!!
    }

    override fun onStopped() {
        super.onStopped()
        disposable.dispose()
        LogUtils.d("SyncWorker onStopped")
        SpendDemoApplication.debugHolder.logLine { "SyncWorker onStopped" }
    }
}