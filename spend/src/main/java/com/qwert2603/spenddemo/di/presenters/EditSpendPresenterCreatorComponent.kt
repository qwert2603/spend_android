package com.qwert2603.spenddemo.di.presenters

import com.qwert2603.spenddemo.edit_spend.EditSpendPresenter
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
interface EditSpendPresenterCreatorComponent {
    fun createEditSpendPresenter(): EditSpendPresenter

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun spendId(id: Long): Builder

        fun build(): EditSpendPresenterCreatorComponent
    }
}