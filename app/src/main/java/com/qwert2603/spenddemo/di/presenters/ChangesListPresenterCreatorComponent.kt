package com.qwert2603.spenddemo.di.presenters

import com.qwert2603.spenddemo.changes_list.ChangesListPresenter
import dagger.Subcomponent

@Subcomponent
interface ChangesListPresenterCreatorComponent {
    fun createRecordsListPresenter(): ChangesListPresenter

    @Subcomponent.Builder
    interface Builder {
        fun build(): ChangesListPresenterCreatorComponent
    }
}