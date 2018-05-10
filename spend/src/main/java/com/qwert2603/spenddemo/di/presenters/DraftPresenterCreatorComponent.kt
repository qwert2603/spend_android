package com.qwert2603.spenddemo.di.presenters

import com.qwert2603.spenddemo.spend_draft.DraftPresenter
import dagger.Subcomponent

@Subcomponent
interface DraftPresenterCreatorComponent {
    fun createDraftPresenter(): DraftPresenter

    @Subcomponent.Builder
    interface Builder {
        fun build(): DraftPresenterCreatorComponent
    }
}