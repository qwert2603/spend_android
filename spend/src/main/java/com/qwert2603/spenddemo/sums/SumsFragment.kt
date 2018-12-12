package com.qwert2603.spenddemo.sums

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qwert2603.spenddemo.R
import kotlinx.android.synthetic.main.toolbar_default.*

class SumsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_sums, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.setTitle(R.string.fragment_title_sums)
        super.onViewCreated(view, savedInstanceState)
    }
}