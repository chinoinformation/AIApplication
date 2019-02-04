package com.example.mitake.aiapplication.battle.data

enum class Player {
    Player1,Player2,NONE;

    fun other()=if (this== Player1) Player2 else Player1
}