package com.qwert2603.spenddemo.di.presenters

import com.qwert2603.spenddemo.records_list.RecordsListPresenter
import dagger.Subcomponent

@Subcomponent
interface RecordsListPresenterCreatorComponent {
    fun createRecordsListPresenter(): RecordsListPresenter

    @Subcomponent.Builder
    interface Builder {
        fun build(): RecordsListPresenterCreatorComponent
    }
}