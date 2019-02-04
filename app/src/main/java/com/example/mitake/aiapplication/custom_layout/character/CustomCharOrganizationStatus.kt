package com.example.mitake.aiapplication.custom_layout.character

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.GridLayout
import android.widget.TextView
import com.example.mitake.aiapplication.R

class CustomCharOrganizationStatus(context: Context, attr: AttributeSet): GridLayout(context, attr) {
    val hp: TextView
    val mp: TextView
    val attack: TextView
    val defense: TextView
    val move: TextView
    val attackRange: TextView

    init{
        val layout = LayoutInflater.from(context).inflate(R.layout.char_organization_status, this)

        hp = layout.findViewById(R.id.char_organization_HP)
        mp = layout.findViewById(R.id.char_organization_MP)
        attack = layout.findViewById(R.id.char_organization_attack)
        defense = layout.findViewById(R.id.char_organization_defense)
        move = layout.findViewById(R.id.char_organization_move)
        attackRange = layout.findViewById(R.id.char_organization_attack_range)
    }
}