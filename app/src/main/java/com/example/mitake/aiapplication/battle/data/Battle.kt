package com.example.mitake.aiapplication.battle.data

import com.example.mitake.aiapplication.R

enum class Battle(val Value: Int) {
    BoardSize(8),   // マップサイズ
    CharNum(4),     // キャラクター数
    RangeId(R.drawable.range),      // 移動範囲画像
    NormalAttackId(R.drawable.normal_attack),   // 通常攻撃範囲画像
    SpecialAttackId(R.drawable.special_attack)  // 特殊攻撃範囲画像
}