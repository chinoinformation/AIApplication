package com.example.mitake.aiapplication.battle.ai

import android.content.Context
import android.widget.ImageView
import com.example.mitake.aiapplication.battle.Attack
import com.example.mitake.aiapplication.battle.CharSearch
import com.example.mitake.aiapplication.battle.MoveSearch
import com.example.mitake.aiapplication.battle.data.Battle
import com.example.mitake.aiapplication.battle.data.Place
import com.example.mitake.aiapplication.battle.data.Player
import com.example.mitake.aiapplication.battle.data.Unit
import java.lang.Math.abs
import java.util.*

class WeakAI(context: Context, placeList: List<List<ImageView>>): AIEvent {
    private var boardSize = Battle.BoardSize.Value
    private val mContext = context
    private var mAttackMap = Array(boardSize, { Array(boardSize, { 0 }) })
    private var mCanMoveMap = Array(boardSize, { Array(boardSize, { 0 }) })
    private val mPlaceList = placeList
    private var unit: MutableList<Unit> = mutableListOf()
    var canAttack = false
    private var attackOption: MutableList<MutableList<List<Int>>> = mutableListOf()
    private var optimizeAttack: MutableList<List<Int>> = mutableListOf()

    override fun computeNext(myPosition: Place, moveChip: Int, attackChip: Int, charMap: Array<Array<Int>>): Pair<Place, MutableList<Map<Int, MutableList<Int>>>> {
        // 移動可能範囲算出
        val route = MoveSearch(mContext, mCanMoveMap, mPlaceList, charMap, myPosition).getMoveRange(moveChip, 0)
        val charPlace = Place(myPosition.X, myPosition.Y, myPosition.player)
        val positionList: MutableList<List<Int>> = mutableListOf()
        val moveEvaluation: MutableList<Int> = mutableListOf()
        val attackEvaluation: MutableList<Int> = mutableListOf()
        // 評価値算出
        for (i in 0 until boardSize){
            for (j in 0 until boardSize){
                if (mCanMoveMap[i][j] == 1){
                    charPlace.X = j
                    charPlace.Y = i
                    positionList.add(listOf(j, i))
                    moveEvaluation.add(calMoveEvaluation(charPlace, charMap))
                    Attack(mPlaceList[0][0], mPlaceList, charMap, mAttackMap, charPlace, unit, mContext, 0).search(j, i, attackChip)
                    attackEvaluation.add(calAttackEvaluation(charPlace, charMap))
                }
                mAttackMap = Array(boardSize, { Array(boardSize, { 0 }) })
            }
        }
        // 評価値から最適解を算出
        val resIndex = optimizeAct(moveEvaluation, attackEvaluation)
        optimizeAttack = attackOption[resIndex]
        return Pair(Place(positionList[resIndex][0], positionList[resIndex][1], myPosition.player), route)
    }

    override fun computeAttack(myPosition: Place): Place {
        return if (canAttack) {
            val random = Random().nextInt(optimizeAttack.size)%optimizeAttack.size
            Place(optimizeAttack[random][0], optimizeAttack[random][1], myPosition.player)
        } else {
            Place(boardSize, boardSize, myPosition.player)
        }
    }

    private fun calMoveEvaluation(position: Place, charMap: Array<Array<Int>>): Int{
        val moveEvaluation: MutableList<Int> = mutableListOf()
        for (i in 0 until charMap.size){
            for (j in 0 until charMap[0].size){
                if (charMap[i][j] != 0) {
                    if ((charMap[i][j] == 1 && position.player == Player.Player2) || (charMap[i][j] == 2 && position.player == Player.Player1)) {
                        val x = abs(position.X - j)
                        val y = abs(position.Y - i)
                        moveEvaluation.add(x+y)
                    }
                }
            }
        }
        moveEvaluation.sort()
        val minIndex =  moveEvaluation.indices.minBy { moveEvaluation[it] } ?: -1
        return when {
            moveEvaluation.count{it == moveEvaluation[minIndex]} != 1 -> {
                val count = moveEvaluation.count{it == moveEvaluation[minIndex]}
                moveEvaluation[Random().nextInt(count)%count]
            }
            else -> moveEvaluation[minIndex]
        }
    }

    private fun calAttackEvaluation(position: Place, charMap: Array<Array<Int>>): Int{
        var attackEvaluation = 0
        val attackOption: MutableList<List<Int>> = mutableListOf()
        for (i in 0 until mAttackMap.size){
            for (j in 0 until mAttackMap[0].size){
                if (mAttackMap[i][j] != 0) {
                    if ((charMap[i][j] == 1 && position.player == Player.Player2) || (charMap[i][j] == 2 && position.player == Player.Player1)) {
                        attackEvaluation += 1
                        attackOption.add(listOf(j, i))
                    }
                }
            }
        }
        this.attackOption.add(attackOption)
        return attackEvaluation
    }

    private fun optimizeAct(moveEvaluation: MutableList<Int>, attackEvaluation: MutableList<Int>): Int{
        var resIndex = 0
        canAttack = attackEvaluation[0] > 0
        for (i in 1 until moveEvaluation.size){
            if (attackEvaluation.count{it == 0} == attackEvaluation.size){
                canAttack = false
                if (moveEvaluation[resIndex] > moveEvaluation[i]) resIndex = i
            } else {
                if (attackEvaluation[resIndex] == 0) {
                    if (attackEvaluation[i] > 0){
                        canAttack = true
                        resIndex = i
                    }
                } else {
                    if (attackEvaluation[i] > 0){
                        canAttack = true
                        if (moveEvaluation[resIndex] < moveEvaluation[i]){
                            resIndex = i
                        } else if (moveEvaluation[resIndex] == moveEvaluation[i]){
                            if (Random().nextInt(2) % 2 == 0) {
                                resIndex = i
                            }
                        }
                    }
                }
            }
        }
        return resIndex
    }

    fun resetMap(){
        mCanMoveMap = Array(boardSize, { Array(boardSize, { 0 }) })
        mAttackMap = Array(boardSize, { Array(boardSize, { 0 }) })
        attackOption = mutableListOf()
        optimizeAttack = mutableListOf()
    }

    fun init(){
        boardSize = 0
        mCanMoveMap = arrayOf()
        mAttackMap = arrayOf()
        unit = mutableListOf()
        attackOption = mutableListOf()
        optimizeAttack = mutableListOf()
    }

}