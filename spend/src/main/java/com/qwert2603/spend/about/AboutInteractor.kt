package com.qwert2603.spend.about

import com.qwert2603.spend.model.repo.RecordsRepo
import io.reactivex.Single
import java.io.File

class AboutInteractor(
        private val recordsRepo: RecordsRepo
) {
    fun getDumpFile(): Single<File> = recordsRepo.getDumpFile()
}