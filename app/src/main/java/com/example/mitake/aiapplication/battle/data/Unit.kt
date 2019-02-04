package com.example.mitake.aiapplication.battle.data


class Unit(status: Status) {
    private val MaxHP :Int = status.hp
    var HP :Int = MaxHP
    private val MaxMP: Int = status.mp
    var MP: Int = MaxMP
    val Attack:Double = status.atack
    val Defence:Double = status.defence
    val type: String = status.type
    var SP: Int = 0
    var pinchPower = 1.0

    /** ダメージ計算式 */
    fun damage(unit: Unit, attackPlace: Place, defensePlace: Place, mapType: Array<Array<Int>>): List<Any> {
        // 基本ダメージを計算
        // 攻撃力から得られた値から防御力を引き算
        // ダメージがマイナスにならないよう調整
        // マップの高さも考慮
        val attackRate =
                when (mapType[attackPlace.Y][attackPlace.X]){
                    1 -> 0.20
                    2 -> -0.10
                    3 -> -0.15
                    else -> 0.0
                }
        val defenseRate =
                when (mapType[defensePlace.Y][defensePlace.X]){
                    1 -> -0.20
                    2 -> -0.10
                    3 -> 0.10
                    else -> 0.0
                }
        val baseDamage = Math.max((unit.Attack * (1.0 + attackRate) - Defence * (1.0 + defenseRate)), 0.0)
        // 乱数によるダメージばらつき
        val variance = Math.random() - 0.5
        // 乱数による補正値を算出
        val varianceDamage = (baseDamage / 10) * variance

        // クリティカル(5%) or ミス(5%) or 何もなし(90%)
        // スピード型のみ特殊攻撃後の回避率45%アップ
        val missRate: Double = if (SP == 2) 0.45 else 0.0
        val attackRoll = Math.random()
        var rate = 0.0
        var message = ""
        when {
            attackRoll >= 0.95 -> {
                // クリティカルダメージ
                // ダメージ1.5倍
                rate = 1.5
                message = "CRITICAL!!<br></br>"
            }
            attackRoll <= (0.05+missRate) -> {
                // ミス
                // ダメージ0.1倍
                rate = 0.1
                message = "MISS....<br></br>"
            }
            else -> {
                // 何もなし
                // ダメージ1.0倍
                rate = 1.0
            }
        }
        // 防御型のみ特殊攻撃後のダメージ軽減
        val specialDefense: Double = if (SP == 1) 30.0 else 0.0
        // 最終ダメージ定義
        val damage = Math.max(((baseDamage + varianceDamage) * rate)*unit.pinchPower - specialDefense, 0.0)
        return listOf(damage, message)
    }

    fun useSpecial(){
        MP -= 10
        if (MP < 0) MP = 0
    }
}