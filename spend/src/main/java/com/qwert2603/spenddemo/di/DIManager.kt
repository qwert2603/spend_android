package com.qwert2603.spenddemo.di

import android.content.Context

class DIManager(appContext: Context) {
    private val appComponent: AppComponent = DaggerAppComponent.builder()
            .appContext(appContext)
            .build()

    val viewsComponent = appComponent.viewsComponent()
    val presentersCreatorComponent = appComponent.presentersCreatorComponent()
}