package com.qwert2603.spenddemo.base_mvi.load_refresh

data class LRViewState<out M : InitialModelHolder<*>>(
        val loading: Boolean,
        val loadingError: Throwable?,
        val canRefresh: Boolean,
        val refreshing: Boolean,
        val refreshingError: Throwable?,
        val model: M
) {
    val isModelLoaded = !loading && loadingError == null
}