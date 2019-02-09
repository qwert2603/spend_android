package com.qwert2603.spend.navigation

import android.support.v4.app.Fragment
import com.qwert2603.spend.about.AboutFragment
import com.qwert2603.spend.records_list.RecordsListFragmentBuilder
import com.qwert2603.spend.records_list.RecordsListKey
import com.qwert2603.spend.sums.SumsFragment
import ru.terrakok.cicerone.android.support.SupportAppScreen
import java.io.Serializable

// don't use objects because of serialization.
sealed class SpendScreen(
        private val fragmentCreator: () -> Fragment = { null!! }
) : SupportAppScreen(), Serializable {

    override fun getFragment(): Fragment = fragmentCreator().also { it.setScreen(this) }

    data class RecordsList(val recordsListKey: RecordsListKey) : SpendScreen({ RecordsListFragmentBuilder.newRecordsListFragment(recordsListKey) })
    data class Sums(@Transient private val ignored: Unit? = null) : SpendScreen({ SumsFragment() })
    data class About(@Transient private val ignored: Unit? = null) : SpendScreen({ AboutFragment() })
}