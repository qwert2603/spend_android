package com.qwert2603.spenddemo.di

import com.qwert2603.spenddemo.di.presenters.ChangesListPresenterCreatorComponent
import com.qwert2603.spenddemo.di.presenters.DraftPresenterCreatorComponent
import com.qwert2603.spenddemo.di.presenters.RecordsListPresenterCreatorComponent
import dagger.Subcomponent

@Subcomponent
interface PresentersCreatorComponent {
    fun recordsListPresenterComponentBuilder(): RecordsListPresenterCreatorComponent.Builder
    fun changesListPresenterComponentBuilder(): ChangesListPresenterCreatorComponent.Builder
    fun draftPresenterComponentBuilder(): DraftPresenterCreatorComponent.Builder
}