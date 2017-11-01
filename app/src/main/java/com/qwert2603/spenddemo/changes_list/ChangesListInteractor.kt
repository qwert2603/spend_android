package com.qwert2603.spenddemo.changes_list

import com.qwert2603.spenddemo.model.entity.Change
import com.qwert2603.spenddemo.model.pagination.Page
import com.qwert2603.spenddemo.model.repo.ChangesRepo
import io.reactivex.Single
import javax.inject.Inject

class ChangesListInteractor @Inject constructor(
        private val changesRepo: ChangesRepo
) {
    fun getAllChanges(): Single<Page<Change>> = changesRepo.getAllChanges()
            .map { Page(it, true) }
}