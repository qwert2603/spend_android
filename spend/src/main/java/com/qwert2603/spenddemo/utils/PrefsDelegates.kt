package com.qwert2603.spenddemo.utils

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.qwert2603.spenddemo.model.rest.entity.LastChangeInfo
import com.qwert2603.spenddemo.model.sync_processor.IdCounter
import com.qwert2603.spenddemo.model.sync_processor.LastChangeStorage
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

}