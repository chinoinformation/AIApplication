package com.example.mitake.aiapplication.battle

import android.widget.ImageView
import com.example.mitake.aiapplication.battle.data.Battle

class MapInformation(place: List<List<ImageView>>) {
    private val boardSize = Battle.BoardSize.Value
    val placeList = place

    /** マップチップの横幅を定義 */
    val getWidth: Int
        get() = placeList[0][0].width

    /** マップチップの縦幅を定義 */
    val getHeight: Int
        get() = placeList[0][0].height

    /** マップチップの中心座標を定義 */
    fun getCenter(mapInformation: MapInformation): Array<Array<Array<Int>>>{
        val location = IntArray(2)
        val centerList: Array<Array<Array<Int>>> = Array(boardSize, {Array(boardSize, {Array<Int>(2, {0})})})
        for (i in 0..(boardSize-1)){
            for (j in 0..(boardSize-1)){
                this.placeList[i][j].getLocationInWindow(location)
                val testX = location[0] + getWidth/2
                val testY = location[1] + getHeight/2
                centerList[i][j][0] = testX
                centerList[i][j][1] = testY
            }
        }
        return centerList
    }
}