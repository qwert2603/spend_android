package com.qwert2603.spenddemo.about

import com.qwert2603.andrlib.base.mvi.ViewAction
import java.io.File

sealed class AboutViewAction : ViewAction {
    data class SendSump(val file: File) : AboutViewAction()
    object DumpError : AboutViewAction()
}