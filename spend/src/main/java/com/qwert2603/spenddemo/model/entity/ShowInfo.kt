package com.qwert2603.spenddemo.model.entity

data class ShowInfo(
        val showSpends: Boolean,
        val showProfits: Boolean,
        val showSums: Boolean,
        val showChangeKinds: Boolean,
        val showTimes: Boolean
) {
    companion object {
        val DEFAULT = ShowInfo(true, true, true, true, true)
    }

    fun showSpendsEnable() = showProfits
    fun showProfitsEnable() = showSpends
    fun showFloatingDate() = showSums && (showSpends || showProfits)
    fun showDeleted() = showChangeKinds
}