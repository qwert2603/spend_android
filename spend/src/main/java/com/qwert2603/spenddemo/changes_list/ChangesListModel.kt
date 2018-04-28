package com.qwert2603.spenddemo.changes_list

import com.qwert2603.andrlib.base.mvi.load_refresh.LRModel
import com.qwert2603.andrlib.base.mvi.load_refresh.LRViewState
import com.qwert2603.andrlib.base.mvi.load_refresh.list.ListModel
import com.qwert2603.andrlib.base.mvi.load_refresh.list.ListViewState
import com.qwert2603.andrlib.generator.GenerateLRChanger
import com.qwert2603.andrlib.generator.GenerateListChanger
import com.qwert2603.spenddemo.model.entity.Change

@GenerateLRChanger
@GenerateListChanger
data class ChangesListModel(
        override val lrModel: LRModel,
        override val listModel: ListModel,
        override val showingList: List<Change>
) : LRViewState, ListViewState<Change>