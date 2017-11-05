package com.qwert2603.syncprocessor.entity

interface Identifiable<out I> {
    val id: I
}