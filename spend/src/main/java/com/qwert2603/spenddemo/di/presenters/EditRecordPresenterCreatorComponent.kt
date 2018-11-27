package com.qwert2603.spenddemo.di.presenters

import com.qwert2603.spenddemo.save_spend.SaveRecordKey
import com.qwert2603.spenddemo.save_spend.SaveRecordPresenter
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
interface EditRecordPresenterCreatorComponent {
    fun createEditSpendPresenter(): SaveRecordPresenter

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun saveRecordKey(saveRecordKey: SaveRecordKey): Builder

        fun build(): EditRecordPresenterCreatorComponent
    }
}