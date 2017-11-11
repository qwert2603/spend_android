package com.qwert2603.syncprocessor.sender

import com.qwert2603.syncprocessor.logger.Logger
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors

class RemoteDataSender(
        private val logger: Logger
) {
    private val scheduler = Schedulers.from(Executors.newSingleThreadExecutor())

//    private var disposable: Disposable? = null
//    private val tasks = ConcurrentHashMap<Any, Completable>()

    fun send(taskId: Any, completable: Completable, force: Boolean) {
//        val localDisposable = disposable
//        if (localDisposable != null) {
//            if (force) {
//                tasks.put(taskId, completable)
//                localDisposable.dispose()
//            }
//            return
//        }
//        disposable =
                completable
//                .doOnDispose { onCompletableFinished(taskId) }
//                .doOnTerminate { onCompletableFinished(taskId) }
                .doOnError { logger.e("RemoteDataSender", "error sending change to remote!", it) }
                .onErrorComplete()
                .subscribeOn(scheduler)
                .subscribe()
    }

//    private fun onCompletableFinished(taskId: Any) {
//        disposable = null
//        tasks.remove(taskId)?.also { send(taskId, it, true) }
//    }
}