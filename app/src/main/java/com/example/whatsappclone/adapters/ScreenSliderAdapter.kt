package com.example.whatsappclone.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.whatsappclone.fragments.ChatsFragment
import com.example.whatsappclone.fragments.PeopleFragment

class ScreenSliderAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount() : Int = 2

    override fun createFragment(position: Int): Fragment = when(position) {
        0 -> ChatsFragment()
        else -> PeopleFragment()
    }

}
