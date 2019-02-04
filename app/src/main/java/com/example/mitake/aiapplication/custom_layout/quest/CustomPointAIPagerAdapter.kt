package com.example.mitake.aiapplication.custom_layout.quest

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

internal class CustomPointAIPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment? {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return when (position){
            in 0..1 -> {
                // パラメータ設定
                PointAIFragment.newInstance(position)
            }
            else ->{
                // パラメータ設定
                null
            }
        }
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return 2
    }
}