package com.qwert2603.spend.utils

import android.support.annotation.IdRes
import android.support.annotation.MainThread
import android.view.Menu
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

@MainThread
class MenuHolder {

    var menu: Menu? = null
        set(value) {
            field = value
            if (value != null) {
                for (i in 0 until value.size()) {
                    val menuItem = value.getItem(i)
                    menuItem.setOnMenuItemClickListener {
                        if (menuItem.isCheckable) {
                            menuItem.isChecked = !menuItem.isChecked
                        }
                        menuItemClicksObservable[menuItem.itemId]?.onNext(Unit)
                        menuItemCheckedObservable[menuItem.itemId]?.onNext(menuItem.isChecked)
                        true
                    }
                }
            }
        }

    private val menuItemClicksObservable = mutableMapOf<Int, PublishSubject<Any>>()
    private val menuItemCheckedObservable = mutableMapOf<Int, PublishSubject<Boolean>>()

    fun menuItemClicks(@IdRes itemId: Int): Observable<Any> {
        menuItemClicksObservable[itemId]?.let { return it }

        val publishSubject = PublishSubject.create<Any>()
        menuItemClicksObservable[itemId] = publishSubject
        return publishSubject
    }

    fun menuItemCheckedChanges(@IdRes itemId: Int): Observable<Boolean> {
        menuItemCheckedObservable[itemId]?.let { return it }

        val publishSubject = PublishSubject.create<Boolean>()
        menuItemCheckedObservable[itemId] = publishSubject
        return publishSubject
    }

}