package au.edu.utas.username.nightnight.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import au.edu.utas.username.nightnight.fragments.*

internal class SummaryAdapter (var context: Context, fm: FragmentManager, private var totalTabs: Int): FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                FeedingSummary()
            }

            1 -> {
                NappySummary()
            }

            2 -> {
                SleepSummary()
            }

            3 -> {
                ActivitySummary()
            }

            else -> getItem(position)
        }
    }

    override fun getCount(): Int {
        return totalTabs
    }
}