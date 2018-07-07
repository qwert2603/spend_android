package com.qwert2603.spenddemo.utils

import android.content.SharedPreferences
import com.qwert2603.spenddemo.model.sync_processor.IdCounter
import com.qwert2603.spenddemo.model.sync_processor.LastUpdateInfo
import com.qwert2603.spenddemo.model.sync_processor.LastUpdateStorage
import java.sql.Timestamp
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PrefsBoolean(
        private val prefs: SharedPreferences,
        private val key: String,
        private val defaultValue: Boolean = false
) : ReadWriteProperty<Any, Boolean> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean = prefs.getBoolean(key, defaultValue)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
}

class PrefsInt(
        private val prefs: SharedPreferences,
        private val key: String,
        private val defaultValue: Int = 0
) : ReadWriteProperty<Any, Int> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Int = prefs.getInt(key, defaultValue)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }
}

class PrefsLong(
        private val prefs: SharedPreferences,
        private val key: String,
        private val defaultValue: Long = 0L
) : ReadWriteProperty<Any, Long> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Long = prefs.getLong(key, defaultValue)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }
}

class PrefsTimestamp(
        private val prefs: SharedPreferences,
        key: String,
        private val defaultValue: Timestamp = Timestamp(0)
) : ReadWriteProperty<Any, Timestamp> {
    private val keyMillis = "$key millis"
    private val keyNanos = "$key nanos"

    override fun getValue(thisRef: Any, property: KProperty<*>): Timestamp = Timestamp(prefs.getLong(keyMillis, defaultValue.time))
            .also { it.nanos = prefs.getInt(keyNanos, defaultValue.nanos) }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Timestamp) {
        prefs.edit().putLong(keyMillis, value.time).apply()
        prefs.edit().putInt(keyNanos, value.nanos).apply()
    }
}

class PrefsLastUpdateStorage(prefs: SharedPreferences, key: String) : LastUpdateStorage {
    private var timestamp by PrefsTimestamp(prefs, "$key timestamp")
    private var id by PrefsLong(prefs, "$key id")

    override var lastUpdateInfo: LastUpdateInfo
        get() = LastUpdateInfo(timestamp, id)
        set(value) {
            timestamp = value.lastUpdateTimestamp
            id = value.lastUpdatedId
        }
}

class PrefsCounter(
        private val prefs: SharedPreferences,
        private val key: String,
        private val defaultValue: Long = 0
) : IdCounter {

    override fun getNext(): Long {
        val next = prefs.getLong(key, defaultValue) + 1
        prefs.edit().putLong(key, next).apply()
        return next
    }
}