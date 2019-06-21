package com.qwert2603.spend.model.sync_processor

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.qwert2603.spend.navigation.MainActivity
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class IsShowingToUserHolder() {

    private val isStartedChanges = PublishSubject.create<Boolean>()

    val isShowingToUser: Observable<Boolean> = isStartedChanges
            .distinctUntilChanged()
            .switchMapSingle { started ->
                Single.just(started)
                        .let {
                            if (started) {
                                it
                            } else {
                                it.delay(1, TimeUnit.SECONDS)
                            }
                        }
            }
            .startWith(false)
            .distinctUntilChanged()
            .subscribeWith(BehaviorSubject.create())

    fun onActivityCreated(activity: MainActivity) {
        activity.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {
                isStartedChanges.onNext(true)
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                isStartedChanges.onNext(false)
            }
        })
    }
}