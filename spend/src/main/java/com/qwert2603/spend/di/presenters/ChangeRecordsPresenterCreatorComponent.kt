package com.qwert2603.spend.di.presenters

import com.qwert2603.spend.change_records.ChangeRecordsPresenter
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
interface ChangeRecordsPresenterCreatorComponent {
    fun createPresenter(): ChangeRecordsPresenter

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun recordsUuids(recordsUuids: List<String>): Builder

        fun build(): ChangeRecordsPresenterCreatorComponent
    }
}