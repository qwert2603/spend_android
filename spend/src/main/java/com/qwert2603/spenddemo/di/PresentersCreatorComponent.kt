package com.qwert2603.spenddemo.di

import com.qwert2603.spenddemo.di.presenters.DraftPresenterCreatorComponent
import com.qwert2603.spenddemo.di.presenters.EditSpendPresenterCreatorComponent
import dagger.Subcomponent

@Subcomponent
interface PresentersCreatorComponent {
    fun draftPresenterComponentBuilder(): DraftPresenterCreatorComponent.Builder
    fun editSpendPresenterCreatorComponent(): EditSpendPresenterCreatorComponent.Builder
}