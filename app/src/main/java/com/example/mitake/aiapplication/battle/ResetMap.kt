package com.example.mitake.aiapplication.battle


import android.widget.ImageView
import com.example.mitake.aiapplication.battle.data.Battle

class ResetMap(canmoveMap: Array<Array<Int>>, attackMap: Array<Array<Int>>, placeList: List<List<ImageView>>) {
    private val resPlaceList = placeList
    var resCanMoveMap = canmoveMap
    var resAttackMap = attackMap
    private val boardSize = Battle.BoardSize.Value

    fun resetList(){
        for (i in 0 until boardSize) {
            for (j in 0 until boardSize) {
                resCanMoveMap[i][j] = 0
                resAttackMap[i][j] = 0
                resPlaceList[j][i].setImageResource(0)
                resPlaceList[j][i].setImageDrawable(null)
            }
        }
    }
}