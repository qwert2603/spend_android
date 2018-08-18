package com.qwert2603.spenddemo.utils

import android.content.SharedPreferences
import com.qwert2603.spenddemo.model.entity.CreatingSpend
import com.qwert2603.spenddemo.model.entity.ServerInfo
import com.qwert2603.spenddemo.model.sync_processor.IdCounter
import com.qwert2603.spenddemo.model.sync_processor.LastFullSyncStorage
import com.qwert2603.spenddemo.model.sync_processor.LastUpdateInfo
import com.qwert2603.spenddemo.model.sync_processor.LastUpdateStorage
import java.sql.Timestamp
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private inline fun SharedPreferences.makeEdit(crossinline editAction: SharedPreferences.Editor.() -> Unit) {
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
        prefs.makeEdit {
            putLong(keyMillis, value.time)
            putInt(keyNanos, value.nanos)
        }
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
        prefs.makeEdit { putLong(key, next) }
        return next
    }
}

class PrefsLastFullSyncStorage(
        prefs: SharedPreferences,
        key: String
) : LastFullSyncStorage {
    override var millis: Long? by PrefsLongNullable(prefs, key)
}

class PrefsServerInfo(
        private val prefs: SharedPreferences,
        key: String,
        private val defaultValue: ServerInfo,
        private val onChange: (ServerInfo) -> Unit
) : ReadWriteProperty<Any, ServerInfo> {
    private val keyUrl = "$key url"
    private val keyUser = "$key user"
    private val keyPassword = "$key password"

    override fun getValue(thisRef: Any, property: KProperty<*>): ServerInfo = when {
        prefs.contains(keyUrl) -> ServerInfo(
                url = prefs.getString(keyUrl, ""),
                user = prefs.getString(keyUser, ""),
                password = prefs.getString(keyPassword, "")
        )
        else -> defaultValue
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: ServerInfo) {
        prefs.makeEdit {
            putString(keyUrl, value.url)
            putString(keyUser, value.user)
            putString(keyPassword, value.password)
        }
        onChange(value)
    }
}

class PrefsCreatingSpend(
        private val prefs: SharedPreferences,
        key: String,
        private val defaultValue: CreatingSpend
) : ReadWriteProperty<Any, CreatingSpend> {
    private val keyDate = "$key date"
    private val keyTime = "$key time"
    private val keyKind = "$key kind"
    private val keyValue = "$key value"

    private var dateMillis by PrefsLongNullable(prefs, keyDate)
    private var timeMillis by PrefsLongNullable(prefs, keyTime)

    override fun getValue(thisRef: Any, property: KProperty<*>): CreatingSpend = when {
        prefs.contains(keyKind) -> CreatingSpend(
                kind = prefs.getString(keyKind, ""),
                value = prefs.getInt(keyValue, 0),
                date = dateMillis?.let { Date(it) },
                time = timeMillis?.let { Date(it) }
        )
        else -> defaultValue
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: CreatingSpend) {
        prefs.makeEdit {
            dateMillis = value.date?.time
            timeMillis = value.time?.time
            putString(keyKind, value.kind)
            putInt(keyValue, value.value)
        }
    }
}