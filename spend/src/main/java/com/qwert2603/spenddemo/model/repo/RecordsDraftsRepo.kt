package com.qwert2603.spenddemo.model.repo

import com.qwert2603.spenddemo.model.entity.RecordDraft

interface RecordsDraftsRepo {
    var spendDraft: RecordDraft?
    var profitDraft: RecordDraft?
}