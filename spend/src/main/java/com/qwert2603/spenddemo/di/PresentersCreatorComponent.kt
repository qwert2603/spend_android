package com.qwert2603.spenddemo.di

import com.qwert2603.spenddemo.di.presenters.ChangesListPresenterCreatorComponent
import com.qwert2603.spenddemo.di.presenters.DraftPresenterCreatorComponent
import dagger.Subcomponent

@Subcomponent
interface PresentersCreatorComponent {
    fun changesListPresenterComponentBuilder(): ChangesListPresenterCreatorComponent.Builder
    fun draftPresenterComponentBuilder(): DraftPresenterCreatorComponent.Builder
}