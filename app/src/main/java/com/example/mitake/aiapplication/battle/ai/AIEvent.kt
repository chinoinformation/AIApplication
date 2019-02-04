package com.example.mitake.aiapplication.battle.ai

import com.example.mitake.aiapplication.battle.data.Place

interface AIEvent {
    fun computeNext(myPosition: Place, moveChip: Int, attackChip: Int, charMap: Array<Array<Int>>): Place
    fun computeAttack(myPosition: Place): Place
}