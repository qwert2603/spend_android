package com.qwert2603.spenddemo.di

import dagger.Module
import dagger.Provides
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import javax.inject.Singleton

@Module
class NavigationModule {
    private val ciceron: Cicerone<Router> = Cicerone.create()

    @Provides @Singleton fun navigatorHolder(): NavigatorHolder = ciceron.navigatorHolder
    @Provides @Singleton fun router(): Router = ciceron.router
}