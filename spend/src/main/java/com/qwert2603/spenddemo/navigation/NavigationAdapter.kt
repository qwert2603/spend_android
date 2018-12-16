package com.qwert2603.spenddemo.navigation

import android.view.ViewGroup
import com.qwert2603.andrlib.base.recyclerview.BaseRecyclerViewAdapter
import com.qwert2603.andrlib.model.IdentifiableLong

class NavigationAdapter : BaseRecyclerViewAdapter<NavigationItem>() {
    override fun onCreateViewHolderModel(parent: ViewGroup, viewType: Int) = NavigationItemViewHolder(parent)

    var selectedItemId: Long = IdentifiableLong.NO_ID
        set(value) {
            if (value != field) {
                val prevPos = adapterList.modelList.indexOfFirst { it.id == field }
                val newPos = adapterList.modelList.indexOfFirst { it.id == value }
                field = value
                if (prevPos >= 0) notifyItemChanged(prevPos)
                if (newPos >= 0) notifyItemChanged(newPos)
            }
        }
}