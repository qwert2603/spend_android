package com.qwert2603.spenddemo.di

import com.qwert2603.spenddemo.model.repo.*
import com.qwert2603.spenddemo.model.repo_impl.*
import dagger.Binds
import dagger.Module

@Module
@Suppress("UNUSED")
interface BindReposModule {
    @Binds fun bind0(recordsRepoImpl: RecordsRepoImpl): RecordsRepo
    @Binds fun bind1(kindsRepoImpl: KindsRepoImpl): KindsRepo
    @Binds fun bind2(changesRepoImpl: ChangesRepoImpl): ChangesRepo
    @Binds fun bind3(draftRepoImpl: DraftRepoImpl): DraftRepo
    @Binds fun bind4(draftRepoImpl: UserSettingsRepoImpl): UserSettingsRepo
}