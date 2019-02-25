package com.example.mitake.aiapplication.custom_layout.battle

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class CustomBattleTutorialAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment? {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return when (position){
            in 0..3 -> {
                // パラメータ設定
                BattleTutorialPageFragment.newInstance(position+1)
            }
            else ->{
                // パラメータ設定
                null
            }
        }
    }

    override fun getCount(): Int {
        // Show 4 total pages.
        return 4
    }
}