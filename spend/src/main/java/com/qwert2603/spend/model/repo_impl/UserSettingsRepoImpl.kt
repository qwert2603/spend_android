package com.qwert2603.spend.model.repo_impl

import android.content.Context
import com.google.gson.Gson
import com.qwert2603.spend.model.entity.*
import com.qwert2603.spend.model.repo.UserSettingsRepo
import com.qwert2603.spend.utils.Const
import com.qwert2603.spend.utils.ObservableField
import com.qwert2603.spend.utils.PreferenceUtils
import com.qwert2603.spend.utils.RxUtils
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class UserSettingsRepoImpl(appContext: Context) : UserSettingsRepo {

    private val prefs = appContext.getSharedPreferences("user_settings.prefs", Context.MODE_PRIVATE)

    private val gson = Gson()

    override val longSumPeriod: ObservableField<Days> = PreferenceUtils.createPrefsObjectObservable(prefs, "longSumPeriod", gson, 30.days)
    override val shortSumPeriod: ObservableField<Minutes> = PreferenceUtils.createPrefsObjectObservable(prefs, "shortSumPeriod", gson, 5.minutes)
    override val showInfo: ObservableField<ShowInfo> = PreferenceUtils.createPrefsObjectObservable(prefs, "showInfo", gson, ShowInfo.DEFAULT)
    override val sumsShowInfo: ObservableField<SumsShowInfo> = PreferenceUtils.createPrefsObjectObservable(prefs, "sumsShowInfo", gson, SumsShowInfo.DEFAULT)

    private val setOldRecordsLockEvents = BehaviorSubject.createDefault(true)

    private val _oldRecordsLockStateChanges = BehaviorSubject.create<OldRecordsLockState>()

    init {
        setOldRecordsLockEvents
                .switchMap { setLock ->
                    // emit new "OldRecordsLockState.Locked" on date change to update UI related to "old records lock".
                    val lockedEvents = RxUtils.dateChanges()
                            .startWith(Any())
                            .map { OldRecordsLockState.Locked }

                    if (setLock) {
                        lockedEvents
                    } else {
                        Observable.interval(0, 1, TimeUnit.SECONDS)
                                .map { Const.OLD_RECORDS_UNLOCK_SECONDS - it.toInt() }
                                .takeWhile { it > 0 }
                                .map<OldRecordsLockState> { OldRecordsLockState.Unlocked(it) }
                                .concatWith(lockedEvents)
                    }
                }
                .subscribe(_oldRecordsLockStateChanges)
    }

    override fun setOldRecordsLock(lock: Boolean) {
        setOldRecordsLockEvents.onNext(lock)
    }

    override fun oldRecordsLockStateChanges(): Observable<OldRecordsLockState> = _oldRecordsLockStateChanges.hide()
}