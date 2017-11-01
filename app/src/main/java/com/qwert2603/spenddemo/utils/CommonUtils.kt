package com.qwert2603.spenddemo.utils

import java.util.*

fun String.hashCodeLong() =
        if (isEmpty()) Random().nextLong()
        else filterIndexed { index, _ -> index % 2 == 0 }.hashCode().toLong() +
                (filterIndexed { index, _ -> index % 2 == 1 }.hashCode().toLong() shl Integer.SIZE)