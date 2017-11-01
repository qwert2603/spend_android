package com.qwert2603.spenddemo.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(
        SchedulersModule::class,
        NavigationModule::class,
        BindReposModule::class,
        ModelModule::class
))
interface AppComponent {

    fun viewsComponent(): ViewsComponent
    fun presentersCreatorComponent(): PresentersCreatorComponent

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun appContext(appContext: Context): Builder

        fun build(): AppComponent
    }
}