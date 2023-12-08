package com.wagdybuild.whatsapp.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.wagdybuild.whatsapp.ui.fragments.GroupsFragment
import com.wagdybuild.whatsapp.ui.fragments.ChatFragment
import com.wagdybuild.whatsapp.ui.fragments.StatusFragment

class MainPagerAdapter(fm: FragmentManager, private val context: Context) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            2 -> GroupsFragment()
            1 -> StatusFragment()
            0 -> ChatFragment()
            else -> ChatFragment()
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence{
        var title = ""
        if (position == 0) {
            title = "Chats"
        }
        if (position == 1) {
            title = "Status"
        }
        if (position == 2) {
            title = "Groups"
        }
        return title
    }
}
