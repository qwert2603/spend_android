package com.qwert2603.spend.di.presenters

import com.qwert2603.spend.records_list_view.RecordsListPresenter
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
interface RecordsListViewPresenterCreatorComponent {
    fun createPresenter(): RecordsListPresenter

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun recordsUuids(recordsUuids: List<String>): Builder

        fun build(): RecordsListViewPresenterCreatorComponent
    }
}