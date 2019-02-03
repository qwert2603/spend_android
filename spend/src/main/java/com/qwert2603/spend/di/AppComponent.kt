package com.qwert2603.spend.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
    NavigationModule::class,
    BindReposModule::class,
    ModelModule::class,
    BindSchedulersModule::class
])
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