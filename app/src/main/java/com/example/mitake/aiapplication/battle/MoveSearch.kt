package com.example.mitake.aiapplication.battle

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.mitake.aiapplication.battle.data.Battle
import com.example.mitake.aiapplication.battle.data.Place
import com.example.mitake.aiapplication.battle.data.Player

class MoveSearch(context: Context, canmoveMap: Array<Array<Int>>, placeList: List<List<ImageView>>, charMap: Array<Array<Int>>, player: Place) {
    private val resPlaceList = placeList
    var resCanMoveMap = canmoveMap
    private val resMoveRoute: MutableList<Map<Int, MutableList<Int>>> = mutableListOf()
    private val useCharMap = charMap
    private val tmp = player
    private val boardSize = Battle.BoardSize.Value
    private val ran = Battle.RangeId.Value
    private val mContext = context

    /** リストを初期化 */
    private fun initList(){
        for (i in 0..(boardSize-1)){
            for (j in 0..(boardSize-1)){
                val label = i*boardSize + j
                val initMap: Map<Int, MutableList<Int>> = mapOf(label to mutableListOf())
                resMoveRoute.add(initMap)
            }
        }
    }

    /** 移動方向を取得 */
    private fun search4(routeAction: Int): List<Any>{
        return when (routeAction){
            1 -> listOf("side", -1)
            2 -> listOf("side", 1)
            3 -> listOf("updown", -1)
            else -> listOf("updown", 1)
        }
    }

    /** 移動可能範囲と移動経路の算出 */
    private fun search(x: Int, y: Int, chip: Int, routeAction: Int, routeTmp: MutableList<Int>) {
        // 例外処理
        when (x) {
            -1, boardSize -> return
        }
        when (y) {
            -1, boardSize -> return
        }
        when (chip) {
            0 -> return
        }
        when (useCharMap[y][x]) {
            2 -> if (tmp.player == Player.Player1) {
                return
            }
            1 -> if (tmp.player == Player.Player2) {
                return
            }
        }

        // 基本処理
        val label = y*boardSize + x
        val action = search4(routeAction)
        val preLabel = if(action[0] == "side") label-action[1].toString().toInt() else label-action[1].toString().toInt()*boardSize
        val preList: List<Int> = if (preLabel >= 0 && preLabel <= resMoveRoute.lastIndex) resMoveRoute[preLabel].getValue(preLabel) else resMoveRoute[0].getValue(0)
        var k = preList
        k = k.toMutableList()
        resCanMoveMap[tmp.Y][tmp.X] = 1
        when (resCanMoveMap[y][x]) {
            0 -> {
                // 移動範囲算出
                resCanMoveMap[y][x] = 1

                // 移動経路算出
                k.add(routeAction)
                for (i in 0..k.lastIndex) {
                    resMoveRoute[label].getValue(label).add(k[i])
                }
            }
            else -> {
                k = routeTmp
            }
        }

        search(x - 1, y, chip - 1, 1, k)
        search(x + 1, y, chip - 1, 2, k)
        search(x, y - 1, chip - 1, 3, k)
        search(x, y + 1, chip - 1, 4, k)
    }

    /** キャラ位置が重なることを防ぐ */
    private fun notCrash(x: Int, y: Int){
        for (i in 0..(boardSize-1)){
            for (j in 0..(boardSize-1)){
                if (useCharMap[j][i] != 0){
                    if (j != y || i != x){
                        resCanMoveMap[j][i] = 0
                    }
                }
            }
        }
    }

    /** 移動可能範囲をプロット */
    private fun canMoveImg(moveFlag: Int){
        for (i in 0 until boardSize) {
            for (j in 0 until boardSize) {
                if (resCanMoveMap[i][j] > 0){
                    if (moveFlag == 0){
                        resPlaceList[j][i].setImageResource(0)
                        resPlaceList[j][i].setImageDrawable(null)
                        Glide.with(mContext).load(ran).into(resPlaceList[j][i])
                    }
                }
            }
        }
    }

    /** 移動可能範囲と移動経路を取得 */
    fun getMoveRange(chip: Int, moveFlag: Int): MutableList<Map<Int, MutableList<Int>>>{
        initList()
        search(tmp.X, tmp.Y, chip, 0, mutableListOf())
        notCrash(tmp.X, tmp.Y)
        canMoveImg(moveFlag)
        return resMoveRoute
    }

}