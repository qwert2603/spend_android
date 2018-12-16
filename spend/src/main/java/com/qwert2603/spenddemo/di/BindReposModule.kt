package com.qwert2603.spenddemo.di

import com.qwert2603.spenddemo.model.repo.RecordAggregationsRepo
import com.qwert2603.spenddemo.model.repo.RecordsDraftsRepo
import com.qwert2603.spenddemo.model.repo.RecordsRepo
import com.qwert2603.spenddemo.model.repo.UserSettingsRepo
import com.qwert2603.spenddemo.model.repo_impl.RecordAggregationsRepoImpl
import com.qwert2603.spenddemo.model.repo_impl.RecordsDraftsRepoImpl
import com.qwert2603.spenddemo.model.repo_impl.RecordsRepoImpl
import com.qwert2603.spenddemo.model.repo_impl.UserSettingsRepoImpl
import dagger.Binds
import dagger.Module

@Module
@Suppress("UNUSED")
interface BindReposModule {
    @Binds fun bind4(repo: UserSettingsRepoImpl): UserSettingsRepo
    @Binds fun bind7(repo: RecordsRepoImpl): RecordsRepo
    @Binds fun bind8(repo: RecordAggregationsRepoImpl): RecordAggregationsRepo
    @Binds fun bind9(repo: RecordsDraftsRepoImpl): RecordsDraftsRepo
}