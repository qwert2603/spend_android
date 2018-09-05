package com.qwert2603.spenddemo.edit_spend

import com.qwert2603.spenddemo.model.repo.SpendsRepo
import javax.inject.Inject

class EditSpendInteractor @Inject constructor(
        private val spendsRepo: SpendsRepo
) {
    fun getSpendChanges(id: Long) = spendsRepo.getSpend(id)
}