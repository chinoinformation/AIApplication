package com.example.mitake.aiapplication.battle

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.mitake.aiapplication.battle.data.Battle
import com.example.mitake.aiapplication.battle.data.Place
import com.example.mitake.aiapplication.battle.data.Unit

class ConfirmCanAction(image: ImageView?, place: Place, charMap: Array<Array<Int>>, placeList:  List<List<ImageView>>, boardsize: Int, moveChip: Int, attackChip: Int, context: Context, no: Int) {
    private val boardSize = boardsize
    private val usePlace = place
    private val useCharMap = charMap
    private val useplaceList = placeList
    private val unit: MutableList<Unit> = mutableListOf()
    private val useMoveChip = moveChip
    private val useAttackChip = attackChip

    private var canmoveMap = Array(boardSize, { Array(boardSize, { 0 }) })
    private var attackMap = Array(boardSize, { Array(boardSize, { 0 }) })

    private var res= Array(boardSize, { Array(boardSize, { 0 }) })
    private val ran = Battle.RangeId.Value
    private val attack = Battle.NormalAttackId.Value

    private val img = image
    private val mContext = context
    private val mNo = no

    fun MoveAttackImg(){
        ResetMap(attackMap, canmoveMap, useplaceList).resetList()
        if (img != null) {
            MoveSearch(mContext, canmoveMap, useplaceList, useCharMap, usePlace).getMoveRange(useMoveChip, 1)
            Attack(img, useplaceList, useCharMap, attackMap, usePlace, unit, mContext, mNo).search(usePlace.Y, usePlace.X, useAttackChip)

            val charPlace = Place(usePlace.X, usePlace.Y, usePlace.player)
            for (i in 0..(canmoveMap.lastIndex)) {
                for (j in 0..(canmoveMap.lastIndex)) {
                    if (canmoveMap[i][j] == 1) {
                        charPlace.X = j
                        charPlace.Y = i
                        Attack(img, useplaceList, useCharMap, attackMap, charPlace, unit, mContext, mNo).search(j, i, useAttackChip)
                        for (m in 0..(attackMap.lastIndex)) {
                            for (n in 0..(attackMap.lastIndex)) {
                                if (attackMap[m][n] == 1) {
                                    res[m][n] += 1
                                }
                            }
                        }
                        attackMap = Array(boardSize, { Array(boardSize, { 0 }) })
                    }
                }
            }

            for (i in 0..(canmoveMap.lastIndex)) {
                for (j in 0..(canmoveMap.lastIndex)) {
                    if (canmoveMap[i][j] == 1) {
                        useplaceList[j][i].setImageResource(0)
                        useplaceList[j][i].setImageDrawable(null)
                        Glide.with(mContext).load(ran).into(useplaceList[j][i])
                    }
                    if (res[i][j] > 0) {
                        if (canmoveMap[i][j] == 1) {
                            useplaceList[j][i].setImageResource(0)
                            useplaceList[j][i].setImageDrawable(null)
                            Glide.with(mContext).load(ran).into(useplaceList[j][i])
                        } else {
                            useplaceList[j][i].setImageResource(0)
                            useplaceList[j][i].setImageDrawable(null)
                            Glide.with(mContext).load(attack).into(useplaceList[j][i])
                        }
                    }
                }
            }

            CharSearch(mContext, useCharMap, useplaceList, usePlace).plotPlayer()
        }
    }

}