package com.qwert2603.spenddemo.about

import com.qwert2603.andrlib.base.mvi.PartialChange

sealed class AboutPartialChange : PartialChange {
    object MakingDumpStarted : AboutPartialChange()
    object MakingDumpFinished : AboutPartialChange()
}