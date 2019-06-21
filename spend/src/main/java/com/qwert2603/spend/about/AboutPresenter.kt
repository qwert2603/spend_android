package com.qwert2603.spend.about

import com.qwert2603.andrlib.base.mvi.BasePresenter
import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.andrlib.util.LogUtils
import com.qwert2603.spend.model.entity.OldRecordsLockState
import io.reactivex.Observable

class AboutPresenter(
        private val interactor: AboutInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : BasePresenter<AboutView, AboutViewState>(uiSchedulerProvider) {

    override val initialState = AboutViewState(false, OldRecordsLockState.Locked)

    override val partialChanges: Observable<PartialChange> = Observable.merge(
            intent { it.sendDumpClicks() }
                    .switchMap {
                        interactor
                                .getDumpFile()
                                .doOnSuccess { viewActions.onNext(AboutViewAction.SendSump(it)) }
                                .map<AboutPartialChange> { AboutPartialChange.MakingDumpFinished }
                                .toObservable()
                                .onErrorReturn { t: Throwable ->
                                    LogUtils.e("AboutPresenter dump error", t)
                                    viewActions.onNext(AboutViewAction.DumpError)
                                    AboutPartialChange.MakingDumpFinished
                                }
                                .startWith(AboutPartialChange.MakingDumpStarted)
                    },
            interactor
                    .oldRecordsLockStateChanges()
                    .map { AboutPartialChange.OldRecordsLockStateChanged(it) }
    )

    override fun stateReducer(vs: AboutViewState, change: PartialChange): AboutViewState {
        if (change !is AboutPartialChange) throw Exception()
        return when (change) {
            AboutPartialChange.MakingDumpStarted -> vs.copy(isMakingDump = true)
            AboutPartialChange.MakingDumpFinished -> vs.copy(isMakingDump = false)
            is AboutPartialChange.OldRecordsLockStateChanged -> vs.copy(oldRecordsLockState = change.oldRecordsLockState)
        }
    }

    override fun bindIntents() {
        intent { it.oldRecordsLockInput() }
                .doOnNext { interactor.setOldRecordsLock(it) }
                .subscribeToView()

        super.bindIntents()
    }
}