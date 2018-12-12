package com.qwert2603.spenddemo.navigation

import android.support.v4.app.Fragment
import com.qwert2603.spenddemo.about.AboutFragment
import com.qwert2603.spenddemo.records_list.RecordsListFragment
import com.qwert2603.spenddemo.sums.by_days.SumsByDaysFragment
import ru.terrakok.cicerone.android.support.SupportAppScreen
import java.io.Serializable

sealed class SpendScreen(
        private val fragmentCreator: () -> Fragment = { null!! }
) : SupportAppScreen(), Serializable {

    override fun getFragment(): Fragment = fragmentCreator().also { it.setScreen(this) }

    object RecordsList : SpendScreen({ RecordsListFragment() })
    object SumsByDays : SpendScreen({ SumsByDaysFragment() })
    object SumsByMonths : SpendScreen({ TODO() })
    object SumsByYears : SpendScreen({ TODO() })
    object About : SpendScreen({ AboutFragment() })
}