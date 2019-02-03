package com.qwert2603.spend.di.presenters

import com.qwert2603.spend.sums.SumsPresenter
import dagger.Subcomponent

@Subcomponent
interface SumsPresenterCreatorComponent {
    fun createPresenter(): SumsPresenter

    @Subcomponent.Builder
    interface Builder {
        fun build(): SumsPresenterCreatorComponent
    }
}