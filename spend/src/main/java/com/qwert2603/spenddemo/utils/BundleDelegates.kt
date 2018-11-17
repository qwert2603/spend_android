package com.qwert2603.spenddemo.utils

import android.os.Bundle
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class BundleBoolean(
        private val key: String,
        argsProvider: () -> Bundle,
        private val defaultValue: Boolean = false
) : ReadWriteProperty<Any, Boolean> {
    private val args by lazy(argsProvider)

    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean = args.getBoolean(key, defaultValue)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        args.putBoolean(key, value)
    }
}

class BundleLong(private val key: String, argsProvider: () -> Bundle) : ReadWriteProperty<Any, Long> {
    private val args by lazy(argsProvider)

    override fun getValue(thisRef: Any, property: KProperty<*>): Long = args.getLong(key)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
        args.putLong(key, value)
    }
}

class BundleLongNullable(private val key: String, argsProvider: () -> Bundle) : ReadWriteProperty<Any, Long?> {
    private val args by lazy(argsProvider)

    override fun getValue(thisRef: Any, property: KProperty<*>): Long? =
            if (args.containsKey(key)) {
                args.getLong(key)
            } else {
                null
            }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Long?) {
        if (value != null) {
            args.putLong(key, value)
        } else {
            args.remove(key)
        }
    }
}