package com.qwert2603.spenddemo.di

import com.qwert2603.spenddemo.di.presenters.DraftPresenterCreatorComponent
import com.qwert2603.spenddemo.di.presenters.EditRecordPresenterCreatorComponent
import com.qwert2603.spenddemo.di.presenters.RecordsListPresenterCreatorComponent
import dagger.Subcomponent

@Subcomponent
interface PresentersCreatorComponent {
    fun draftPresenterComponentBuilder(): DraftPresenterCreatorComponent.Builder
    fun editRecordPresenterCreatorComponent(): EditRecordPresenterCreatorComponent.Builder
    fun recordsListPresenterCreatorComponent(): RecordsListPresenterCreatorComponent.Builder
}