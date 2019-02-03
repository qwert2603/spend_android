package com.qwert2603.spend.utils

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.qwert2603.spend.model.rest.entity.LastChangeInfo
import com.qwert2603.spend.model.sync_processor.IdCounter
import com.qwert2603.spend.model.sync_processor.LastChangeStorage
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

inline fun SharedPreferences.makeEdit(crossinline editAction: SharedPreferences.Editor.() -> Unit) {
    this.edit()
            .apply(editAction)
            .apply()
}

class PrefsBoolean(
        private val prefs: SharedPreferences,
        private val key: String,
        private val defaultValue: Boolean = false
) : ReadWriteProperty<Any, Boolean> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean = prefs.getBoolean(key, defaultValue)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        prefs.makeEdit { putBoolean(key, value) }
    }
}

class PrefsString(
        private val prefs: SharedPreferences,
        private val key: String,
        private val defaultValue: String = ""
) : ReadWriteProperty<Any, String> {
    override fun getValue(thisRef: Any, property: KProperty<*>): String = prefs.getString(key, defaultValue)!!

    override fun setValue(thisRef: Any, property: KProperty<*>, value: String) {
        prefs.makeEdit { putString(key, value) }
    }
}

class PrefsInt(
        private val prefs: SharedPreferences,
        private val key: String,
        private val defaultValue: Int = 0
) : ReadWriteProperty<Any, Int> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Int = prefs.getInt(key, defaultValue)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Int) {
        prefs.makeEdit { putInt(key, value) }
    }
}

class PrefsLong(
        private val prefs: SharedPreferences,
        private val key: String,
        private val defaultValue: Long = 0L
) : ReadWriteProperty<Any, Long> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Long = prefs.getLong(key, defaultValue)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
        prefs.makeEdit { putLong(key, value) }
    }
}

class PrefsLongNullable(
        private val prefs: SharedPreferences,
        private val key: String
) : ReadWriteProperty<Any, Long?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Long? =
            if (key in prefs) {
                prefs.getLong(key, 0)
            } else {
                null
            }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Long?) {
        prefs.makeEdit {
            if (value != null) {
                putLong(key, value)
            } else {
                remove(key)
            }
        }
    }
}

class PrefsCounter(
        private val prefs: SharedPreferences,
        private val key: String,
        private val defaultValue: Long = 0
) : IdCounter {

    override fun getNext(): Long {
        val next = prefs.getLong(key, defaultValue) + 1
        prefs.makeEdit { putLong(key, next) }
        return next
    }
}

class PrefsLastChangeStorage(prefs: SharedPreferences, gson: Gson) : LastChangeStorage {
    override var lastChangeInfo: LastChangeInfo? by PreferenceUtils.createPrefsObjectNullable(prefs, "lastChangeInfo", gson)
}

abstract class ObservableField<T> {
    protected val lock = Any()

    abstract var field: T
    abstract val changes: Observable<T>

    fun updateField(updater: (T) -> T) {
        synchronized(lock) {
            field = updater(field)
        }
    }
}

object PreferenceUtils {

    inline fun <reified T : Any> createPrefsObjectNullable(
            prefs: SharedPreferences,
            key: String,
            gson: Gson
    ) = object : ReadWriteProperty<Any, T?> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T? =
                if (key in prefs) {
                    gson.fromJson(prefs.getString(key, ""), object : TypeToken<T>() {}.type)
                } else {
                    null
                }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
            prefs.makeEdit {
                if (value != null) {
                    putString(key, gson.toJson(value))
                } else {
                    remove(key)
                }
            }
        }
    }

    inline fun <reified T : Any> createPrefsObjectObservable(
            prefs: SharedPreferences,
            key: String,
            gson: Gson,
            defaultValue: T
    ): ObservableField<T> {
        val changes = BehaviorSubject.create<T>()

        return object : ObservableField<T>() {
            init {
                changes.onNext(field)
            }

            override var field: T
                get() =
                    if (key in prefs) {
                        gson.fromJson(prefs.getString(key, ""), object : TypeToken<T>() {}.type)
                    } else {
                        defaultValue
                    }
                set(value) {
                    synchronized(lock) {
                        prefs.makeEdit { putString(key, gson.toJson(value)) }
                        changes.onNext(value)
                    }
                }

            override val changes: Observable<T> = changes.hide()

        }
    }

}