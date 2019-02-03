package com.qwert2603.spend.model.repo

import com.qwert2603.spend.model.entity.RecordDraft

interface RecordsDraftsRepo {
    var spendDraft: RecordDraft?
    var profitDraft: RecordDraft?
}