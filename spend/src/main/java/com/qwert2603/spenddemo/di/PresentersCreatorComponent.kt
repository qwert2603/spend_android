package com.qwert2603.spenddemo.di

import com.qwert2603.spenddemo.di.presenters.AboutPresenterCreatorComponent
import com.qwert2603.spenddemo.di.presenters.RecordsListPresenterCreatorComponent
import com.qwert2603.spenddemo.di.presenters.SaveRecordPresenterCreatorComponent
import dagger.Subcomponent

@Subcomponent
interface PresentersCreatorComponent {
    fun saveRecordPresenterCreatorComponent(): SaveRecordPresenterCreatorComponent.Builder
    fun recordsListPresenterCreatorComponent(): RecordsListPresenterCreatorComponent.Builder
    fun aboutPresenterCreatorComponent(): AboutPresenterCreatorComponent.Builder
}