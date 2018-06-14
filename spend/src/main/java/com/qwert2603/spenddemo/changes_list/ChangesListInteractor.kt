package com.qwert2603.spenddemo.changes_list

import com.qwert2603.spenddemo.model.entity.SpendChange
import com.qwert2603.spenddemo.model.repo.SpendChangesRepo
import io.reactivex.Single
import javax.inject.Inject

class ChangesListInteractor @Inject constructor(
        private val spendChangesRepo: SpendChangesRepo
) {
    fun getAllChanges(): Single<List<SpendChange>> = spendChangesRepo.getAllChanges()
}