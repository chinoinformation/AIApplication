package com.example.mitake.aiapplication.battle.ai

import com.example.mitake.aiapplication.battle.data.Place

interface AIEvent {
    fun computeNext(myPosition: Place, moveChip: Int, attackChip: Int, charMap: Array<Array<Int>>): Pair<Place, MutableList<Map<Int, MutableList<Int>>>>
    fun computeAttack(myPosition: Place): Place
}