package au.edu.utas.username.nightnight.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import au.edu.utas.username.nightnight.fragments.*

internal class CategoryListAdapter (var context: Context, fm: FragmentManager, var totalTabs: Int): FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when(position){
            0 -> {
                FeedingList()
            }

            1 -> {
                NappyList()
            }

            2 -> {
                SleepList()
            }

            3 -> {
                ActivityList()
            }

            else -> getItem(position)
        }
    }

    override fun getCount(): Int {
        return totalTabs
    }
}