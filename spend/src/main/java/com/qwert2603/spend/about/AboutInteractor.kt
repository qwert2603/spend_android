package com.qwert2603.spend.about

import com.qwert2603.spend.model.entity.OldRecordsLockState
import com.qwert2603.spend.model.repo.RecordsRepo
import com.qwert2603.spend.model.repo.UserSettingsRepo
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File

class AboutInteractor(
        private val recordsRepo: RecordsRepo,
        private val userSettingsRepo: UserSettingsRepo
) {
    fun getDumpFile(): Single<File> = recordsRepo.getDumpFile()

    fun setOldRecordsLock(lock: Boolean) = userSettingsRepo.setOldRecordsLock(lock)
    fun oldRecordsLockStateChanges(): Observable<OldRecordsLockState> = userSettingsRepo.oldRecordsLockStateChanges()
}