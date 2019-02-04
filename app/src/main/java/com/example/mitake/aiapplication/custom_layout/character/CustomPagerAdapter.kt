package com.example.mitake.aiapplication.custom_layout.character

import android.annotation.SuppressLint
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.example.mitake.aiapplication.character.CharOrganizationStatusFragment


@SuppressLint("ValidFragment")
internal class CustomPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment? {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return when (position){
            in 0..4 -> {
                // パラメータ設定
                CharOrganizationStatusFragment.newInstance(position + 1)
            }
            else ->{
                // パラメータ設定
                null
            }
        }
    }

    override fun getCount(): Int {
        // Show 5 total pages.
        return 5
    }
}