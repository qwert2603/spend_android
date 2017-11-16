package com.qwert2603.spenddemo.utils

import android.content.res.Resources
import android.support.annotation.ColorRes
import android.support.v4.content.res.ResourcesCompat

fun Resources.color(@ColorRes colorId: Int, theme: Resources.Theme? = null) = ResourcesCompat.getColor(this, colorId, theme)