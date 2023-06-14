package au.edu.utas.username.nightnight.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import au.edu.utas.username.nightnight.fragments.Activity
import au.edu.utas.username.nightnight.fragments.Feeding
import au.edu.utas.username.nightnight.fragments.Nappy
import au.edu.utas.username.nightnight.fragments.Sleep

internal class ViewPagerAdapter (var context: Context, fm: FragmentManager, var totalTabs: Int): FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when(position){
            0 -> {
                Feeding()
            }

            1 -> {
                Nappy()
            }

            2 -> {
                Sleep()
            }

            3 -> {
                Activity()
            }

            else -> getItem(position)
        }
    }

    override fun getCount(): Int {
        return totalTabs
    }

}