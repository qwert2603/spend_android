package com.qwert2603.spenddemo.base_mvi.load_refresh

import com.qwert2603.spenddemo.base_mvi.PartialChange

/**
 * Partial changes for [LRViewState].
 */
sealed class LRPartialChange : PartialChange {
    data class LoadingStarted(private val ignored: Unit = Unit) : LRPartialChange()
    data class LoadingError(val t: Throwable) : LRPartialChange()
    data class RefreshStarted(private val ignored: Unit = Unit) : LRPartialChange()
    data class RefreshError(val t: Throwable) : LRPartialChange()
    data class RefreshCancelled(private val ignored: Unit = Unit) : LRPartialChange()
    data class InitialModelLoaded<out I>(val i: I) : LRPartialChange()
}