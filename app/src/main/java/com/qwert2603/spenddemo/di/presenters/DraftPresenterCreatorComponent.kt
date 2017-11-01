package com.qwert2603.spenddemo.di.presenters

import com.qwert2603.spenddemo.draft.DraftPresenter
import dagger.Subcomponent

@Subcomponent
interface DraftPresenterCreatorComponent {
    fun createRecordsListPresenter(): DraftPresenter

    @Subcomponent.Builder
    interface Builder {
        fun build(): DraftPresenterCreatorComponent
    }
}