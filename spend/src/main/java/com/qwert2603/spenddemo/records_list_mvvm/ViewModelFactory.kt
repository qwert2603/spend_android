package com.qwert2603.spenddemo.records_list_mvvm

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.qwert2603.spenddemo.di.DIHolder
import com.qwert2603.spenddemo.model.local_db.LocalDB
import javax.inject.Inject

class ViewModelFactory : ViewModelProvider.Factory {

    @Inject
    lateinit var localDB: LocalDB

    init {
        DIHolder.diManager.viewsComponent.inject(this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass == RecordsListViewModel::class.java) {
            return RecordsListViewModel(localDB) as T
        }
        throw Exception()
    }
}