package com.qwert2603.spenddemo.records_list_mvvm

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.model.repo.ProfitsRepo
import com.qwert2603.spenddemo.model.repo.SpendsRepo
import com.qwert2603.spenddemo.model.repo.UserSettingsRepo
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class ViewModelFactory : ViewModelProvider.Factory {

    @Inject
    lateinit var userSettingsRepo: UserSettingsRepo

    @Inject
    lateinit var spendsRepo: SpendsRepo

    @Inject
    lateinit var profitsRepo: ProfitsRepo

    @Inject
    lateinit var router: Router

    init {
        DIHolder.diManager.viewsComponent.inject(this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass == RecordsListViewModel::class.java) {
            return RecordsListViewModel(
                    spendsRepo = spendsRepo,
                    profitsRepo = profitsRepo,
                    userSettingsRepo = userSettingsRepo,
                    router = router
            ) as T
        }
        throw Exception()
    }
}