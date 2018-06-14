package com.qwert2603.spenddemo.di

import com.qwert2603.spenddemo.model.repo.*
import com.qwert2603.spenddemo.model.repo_impl.*
import dagger.Binds
import dagger.Module

@Module
@Suppress("UNUSED")
interface BindReposModule {
    @Binds fun bind0(repo: SpendsRepoImpl): SpendsRepo
    @Binds fun bind1(repo: SpendKindsRepoImpl): SpendKindsRepo
    @Binds fun bind2(repo: SpendChangesRepoImpl): SpendChangesRepo
    @Binds fun bind3(repo: SpendDraftRepoImpl): SpendDraftRepo
    @Binds fun bind4(repo: UserSettingsRepoImpl): UserSettingsRepo
    @Binds fun bind5(repo: ProfitsRepoImpl): ProfitsRepo
    @Binds fun bind6(repo: ProfitKindsRepoImpl): ProfitKindsRepo
}