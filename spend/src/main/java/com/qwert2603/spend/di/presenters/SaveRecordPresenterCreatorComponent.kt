package com.qwert2603.spend.di.presenters

import com.qwert2603.spend.save_record.SaveRecordKey
import com.qwert2603.spend.save_record.SaveRecordPresenter
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
interface SaveRecordPresenterCreatorComponent {
    fun createSaveRecordPresenter(): SaveRecordPresenter

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun saveRecordKey(saveRecordKey: SaveRecordKey): Builder

        fun build(): SaveRecordPresenterCreatorComponent
    }
}