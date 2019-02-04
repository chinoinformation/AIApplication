package com.example.mitake.aiapplication.battle

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.battle.data.Place
import com.example.mitake.aiapplication.battle.data.Player

class CharSearch(context: Context, charMap: Array<Array<Int>>, placeList: List<List<ImageView>>, presentPlace: Place) {
    private val backgroundMyGroup: Int = R.drawable.background_my_group
    private val presentPlayer: Int = R.drawable.present_location
    private val mPresentPlace = presentPlace
    private val mCharMap = charMap
    private val mPlaceList = placeList
    private val mContext = context

    fun plotPlayer(){
        for (i in (0 until mCharMap.size)){
            for (j in (0 until mCharMap.size)){
                if (mPresentPlace.player == Player.Player1) {
                    if (mCharMap[j][i] == 1) {
                        Glide.with(mContext).load(backgroundMyGroup).into(mPlaceList[i][j])
                    }
                } else {
                    if (mCharMap[j][i] == 2) {
                        Glide.with(mContext).load(backgroundMyGroup).into(mPlaceList[i][j])
                    }
                }
            }
        }
        Glide.with(mContext).load(presentPlayer).into(mPlaceList[mPresentPlace.X][mPresentPlace.Y])
    }
}