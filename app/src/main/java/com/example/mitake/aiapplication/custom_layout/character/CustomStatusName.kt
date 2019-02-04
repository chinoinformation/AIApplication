package com.example.mitake.aiapplication.custom_layout.character

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.GridLayout
import android.widget.TextView
import com.example.mitake.aiapplication.R

class CustomStatusName(context: Context, attr: AttributeSet): GridLayout(context, attr) {
    var hp: TextView
    var mp: TextView
    var attack: TextView
    var defense: TextView
    var move: TextView
    var attackRange: TextView

    init{
        val layout = LayoutInflater.from(context).inflate(R.layout.status_name, this)

        hp = layout.findViewById(R.id.text_HP)
        mp = layout.findViewById(R.id.text_MP)
        attack = layout.findViewById(R.id.text_attack)
        defense = layout.findViewById(R.id.text_defense)
        attackRange = layout.findViewById(R.id.text_attack_range)
        move = layout.findViewById(R.id.text_move)
    }
}