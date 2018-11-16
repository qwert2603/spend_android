package com.qwert2603.spenddemo.model.entity

//todo
inline class STime(val time: Int) {
    override fun toString() = "${time / 100}:${time % 100}"
}