package com.qwert2603.syncprocessor.inmemory

import com.qwert2603.syncprocessor.entity.Identifiable

internal fun <K, V> Map<K, V>.putOrRemove(key: K, value: V?): Map<K, V> = let {
    if (value != null) {
        this + Pair(key, value)
    } else {
        this - key
    }
}

internal fun <K> Set<K>.addOrRemove(key: K, add: Boolean): Set<K> = let {
    if (add) {
        this + key
    } else {
        this - key
    }
}

internal fun <K> Set<K>.removeKey(key: K?): Set<K> {
    return if (key == null) this
    else this - key
}

internal fun <K, V> Map<K, V>.removeKey(key: K?): Map<K, V> {
    return if (key == null) this
    else this - key
}

internal fun <I, T : Identifiable<I>> List<T>.removeWithId(removingId: I?): List<T> {
    if (removingId == null) return this
    return filter { it.id != removingId }
}

internal fun <I, T : Identifiable<I>> List<T>.addWithId(item: T): List<T> {
    var replaced = false
    return this
            .map {
                if (it.id == item.id) {
                    replaced = true
                    item
                } else it
            }
            .let { if (replaced) it else it + item }
}

internal fun <I, T : Identifiable<I>> List<T>.addWithIds(items: List<T>): List<T> {
    val mutableMap = items.associateBy { it.id }.toMutableMap()
    return this
            .map { mutableMap.remove(it.id) ?: it }
            .let { it + mutableMap.values }
}