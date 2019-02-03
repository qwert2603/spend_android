package com.qwert2603.spend.about

import com.qwert2603.andrlib.base.mvi.BasePresenter
import com.qwert2603.andrlib.base.mvi.PartialChange
import com.qwert2603.andrlib.schedulers.UiSchedulerProvider
import com.qwert2603.andrlib.util.LogUtils
import io.reactivex.Observable
import javax.inject.Inject

class AboutPresenter @Inject constructor(
        private val interactor: AboutInteractor,
        uiSchedulerProvider: UiSchedulerProvider
) : BasePresenter<AboutView, AboutViewState>(uiSchedulerProvider) {

    override val initialState = AboutViewState(false)

    override val partialChanges: Observable<PartialChange> = intent { it.sendDumpClicks() }
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
            }

    override fun stateReducer(vs: AboutViewState, change: PartialChange): AboutViewState {
        if (change !is AboutPartialChange) throw Exception()
        return when (change) {
            AboutPartialChange.MakingDumpStarted -> vs.copy(isMakingDump = true)
            AboutPartialChange.MakingDumpFinished -> vs.copy(isMakingDump = false)
        }
    }
}