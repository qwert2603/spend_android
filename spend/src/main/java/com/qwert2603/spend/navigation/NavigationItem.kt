package com.qwert2603.spend.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.qwert2603.andrlib.model.IdentifiableLong

data class NavigationItem(
        @DrawableRes val iconRes: Int,
        @StringRes val titleRes: Int,
        val screen: SpendScreen,
        override val id: Long = nextId++
) : IdentifiableLong {
    companion object {
        private var nextId = 1L
    }
}
