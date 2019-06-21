package com.qwert2603.spend.model.entity

sealed class OldRecordsLockState {
    object Locked : OldRecordsLockState()
    data class Unlocked(val secondsRemain: Int) : OldRecordsLockState()
}