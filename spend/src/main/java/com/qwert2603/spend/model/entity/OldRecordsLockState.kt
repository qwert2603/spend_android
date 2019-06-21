package com.qwert2603.spend.model.entity

sealed class OldRecordsLockState {
    val isLocked by lazy { this == Locked }

    object Locked : OldRecordsLockState()
    data class Unlocked(val secondsRemain: Int) : OldRecordsLockState()
}