package com.qwert2603.spenddemo.di.presenters

import com.qwert2603.spenddemo.about.AboutPresenter
import dagger.Subcomponent

@Subcomponent
interface AboutPresenterCreatorComponent {
    fun createPresenter(): AboutPresenter

    @Subcomponent.Builder
    interface Builder {
        fun build(): AboutPresenterCreatorComponent
    }
}