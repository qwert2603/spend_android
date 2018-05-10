package com.qwert2603.spenddemo.di

import com.qwert2603.spenddemo.model.repo.*
import com.qwert2603.spenddemo.model.repo_impl.*
import dagger.Binds
import dagger.Module

@Module
@Suppress("UNUSED")
interface BindReposModule {
    @Binds fun bind0(repo: RecordsRepoImpl): RecordsRepo
    @Binds fun bind1(repo: KindsRepoImpl): KindsRepo
    @Binds fun bind2(repo: ChangesRepoImpl): ChangesRepo
    @Binds fun bind3(repo: DraftRepoImpl): DraftRepo
    @Binds fun bind4(repo: UserSettingsRepoImpl): UserSettingsRepo
    @Binds fun bind5(repo: ProfitsRepoImpl): ProfitsRepo
    @Binds fun bind6(repo: ProfitKindsRepoImpl): ProfitKindsRepo
}