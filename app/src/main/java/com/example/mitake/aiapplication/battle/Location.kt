package com.example.mitake.aiapplication.battle

import android.widget.ImageView

class Location(preImg: ImageView, moveImg: ImageView) {
    private val usePreImg = preImg
    private val useMoveImg = moveImg

    private fun getLocation(img: ImageView): List<Int>{
        val location = IntArray(2)
        img.getLocationOnScreen(location)
        val locationX = location[0] + img.width/2
        val locationY = location[1] + img.height/2
        return listOf(locationX, locationY)
    }

    fun calMoveLocation(): List<Int>{
        val preLocation = getLocation(usePreImg)
        val moveLocation = getLocation(useMoveImg)
        val moveX = moveLocation[0] - preLocation[0]
        val moveY = moveLocation[1] - preLocation[1]
        return listOf(moveX, moveY)
    }
}