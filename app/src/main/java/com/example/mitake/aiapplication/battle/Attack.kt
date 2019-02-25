package com.example.mitake.aiapplication.battle

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.mitake.aiapplication.GlideAnim
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.battle.data.Battle
import com.example.mitake.aiapplication.battle.data.Place
import com.example.mitake.aiapplication.battle.data.Unit
import com.example.mitake.aiapplication.custom_layout.WaveAnimationLayout

class Attack(image: ImageView, placeList: List<List<ImageView>>, charMap: Array<Array<Int>>, attackMap: Array<Array<Int>>, player: Place, unit: MutableList<Unit>, context: Context, no: Int) {
    private val resPlaceList = placeList
    private var resAttackMap = attackMap
    private var resUnit = unit
    private val useCharMap = charMap
    private val boardSize = Battle.BoardSize.Value
    private val normalAttack = Battle.NormalAttackId.Value
    private val specialAttack = Battle.SpecialAttackId.Value
    private val tmp = player
    private val img: ImageView = image
    private val mContext = context
    private val mNo: Int = no

    private var set: AnimatorSet? = null
    private var objectAnimatorX: ObjectAnimator? = null
    private var objectAnimatorY: ObjectAnimator? = null

    fun search(x: Int, y: Int, chip: Int) {
        when (x) {
            -1, boardSize -> return
        }
        when (y) {
            -1, boardSize -> return
        }
        when (chip) {
            0 -> return
        }
        if (y != tmp.Y && x != tmp.X){
            return
        }

        if (useCharMap[y][x] != 0){
            if (y != tmp.Y || x != tmp.X) {
                if (y == tmp.Y || x == tmp.X) {
                    resAttackMap[y][x] = 1
                }
                return
            }
        }

        if (y != tmp.Y || x != tmp.X) {
            if (y == tmp.Y || x == tmp.X) {
                resAttackMap[y][x] = 1
            }
        }
        search(x - 1, y, chip - 1)
        search(x + 1, y, chip - 1)
        search(x, y - 1, chip - 1)
        search(x, y + 1, chip - 1)
    }

    private fun attackImg(attackType: Int){
        for (i in 0..(boardSize-1)){
            for (j in 0..(boardSize-1)){
                if (resAttackMap[j][i] == 1){
                    when (attackType) {
                        0 -> {
                            resPlaceList[i][j].setImageDrawable(null)
                            Glide.with(mContext).load(normalAttack).into(resPlaceList[i][j])
                        }
                        else -> {
                            resPlaceList[i][j].setImageDrawable(null)
                            Glide.with(mContext).load(specialAttack).into(resPlaceList[i][j])
                        }
                    }
                }
            }
        }
    }

    fun getAttackRange(chip: Int, attackType: Int){
        search(tmp.X, tmp.Y, chip)
        attackImg(attackType)
    }

    private fun attackDamage(x: Int, y: Int, charPlaceList: MutableList<Place>, mapType: Array<Array<Int>>, tmpUnit: Unit): Pair<List<Int>, String>{
        var count = 0
        var num = 0
        var preHP = 0
        var preMP = 0
        var damage = 0.0
        var message = ""
        // 攻撃対象の確認
        for (i in (0..charPlaceList.lastIndex)){
            if (charPlaceList[i].X == x && charPlaceList[i].Y == y && charPlaceList[i].player == tmp.player.other()){
                preHP = resUnit[i].HP
                preMP = resUnit[i].MP
                val damageList = resUnit[i].damage(tmpUnit, tmp, charPlaceList[i], mapType)
                damage = damageList.first
                message = damageList.second
                num = i
                count = 1
                break
            }
        }
        // damageはDouble型のため，Int型に直す
        // 小数点以下は切り上げる
        damage = resDamage(damage)
        return Pair(listOf(count, num, preHP, preMP, damage.toInt()), message)
    }

    /** 小数点以下切り上げ */
    private fun resDamage(damage: Double): Double{
        var res = damage
        val sa = damage - damage.toInt()
        if (sa > 0.0){
            res += 1
        }
        return res
    }

    /** 通常攻撃 */
    fun normalAttack(x: Int, y: Int, charPlaceList: MutableList<Place>, mapHeight: Array<Array<Int>>, tmpUnit: Unit): Pair<List<Int>, String>{
        val attackList = attackDamage(x, y, charPlaceList, mapHeight, tmpUnit)
        val res = resUnit[attackList.first[1]].HP.toDouble() - attackList.first[4].toDouble()
        resUnit[attackList.first[1]].HP = if( res > 0.0 ) res.toInt() else 0
        return attackList
    }

    /**
     * 特殊攻撃
     * 消費MPは10
     */
    fun specialAttack(x: Int, y: Int, charPlaceList: MutableList<Place>, mapType: Array<Array<Int>>, tmpUnit: Unit, type: String, charturn: Int): Pair<List<Int>, String>{
        // MPを消費
        tmpUnit.useSpecial()
        val attackList = attackDamage(x, y, charPlaceList, mapType, tmpUnit)
        when (type){
            "バランス型" -> {
                // 90の固定ダメージ
                var specialDamage = 90.0
                val res = resUnit[attackList.first[1]].HP.toDouble() - specialDamage
                specialDamage = resDamage(specialDamage)
                resUnit[attackList.first[1]].HP = if( res > 0.0 ) res.toInt() else 0
                return Pair(
                        listOf(attackList.first[0],
                        attackList.first[1],
                        attackList.first[2],
                        attackList.first[3],
                        specialDamage.toInt()),
                        "")
            }
            "攻撃型" -> {
                // normalAttack時の値の1.7倍のダメージ
                val attackRate = 1.7
                var specialDamage = attackList.first[4]*attackRate
                val res = resUnit[attackList.first[1]].HP.toDouble() - specialDamage
                specialDamage = resDamage(specialDamage)
                resUnit[attackList.first[1]].HP = if( res > 0.0 ) res.toInt() else 0
                return Pair(
                        listOf(attackList.first[0],
                        attackList.first[1],
                        attackList.first[2],
                        attackList.first[3],
                        specialDamage.toInt()),
                        attackList.second)
            }
            "防御型" -> {
                // 固定ダメージ軽減
                val res = resUnit[attackList.first[1]].HP.toDouble() - attackList.first[4].toDouble()
                resUnit[attackList.first[1]].HP = if( res > 0.0 ) res.toInt() else 0
                resUnit[charturn].SP = 1
                return attackList
            }
            else -> {
                // 回避率アップ
                // normalAttack時の値の1.2倍のダメージ
                val attackRate = 1.2
                var specialDamage = attackList.first[4]*attackRate
                val res = resUnit[attackList.first[1]].HP.toDouble() - specialDamage
                specialDamage = resDamage(specialDamage)
                resUnit[attackList.first[1]].HP = if( res > 0.0 ) res.toInt() else 0
                resUnit[charturn].SP = 2
                return Pair(
                        listOf(attackList.first[0],
                        attackList.first[1],
                        attackList.first[2],
                        attackList.first[3],
                        specialDamage.toInt()),
                        attackList.second)
            }
        }
    }

    /** キャラ攻撃のアニメション変数 */
    fun charAttackAnim(x: Int, y: Int): Int{
        var bitmapName: String = "char" + mNo.toString()
        var drawName = "char" + mNo.toString() + "_"
        val diffX = x - tmp.X
        val diffY = y - tmp.Y
        val dir = if (diffX != 0) {
            if (diffX < 0){
                4
            }else{
                2
            }
        } else {
            if (diffY < 0){
                3
            }else{
                1
            }
        }
        bitmapName += dir.toString() + "1"
        drawName += dir.toString()
        GlideAnim().animation(mContext, img, bitmapName, drawName)
        return dir
    }

    /** ダメージ位置を確定 */
    fun init(img: WaveAnimationLayout, saX: Float, moveX: Float, saY: Float, moveY: Float, duration: Long){
        set = AnimatorSet()
        var setDuration = duration
        if (Math.abs(saX - moveX) == 0f && Math.abs(saY - moveY) == 0f){
            setDuration = 0
        }
        set!!.duration = setDuration
        objectAnimatorX = ObjectAnimator.ofFloat(img, "TranslationX", saX, moveX)
        objectAnimatorY = ObjectAnimator.ofFloat(img, "TranslationY", saY, moveY)
        if (Math.abs(saX - moveX) == 0f) {
            set!!.play(objectAnimatorX).after(objectAnimatorY)
        }else{
            set!!.play(objectAnimatorY).after(objectAnimatorX)
        }

        // アニメーション終了した時の処理
        set!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                set = null
                objectAnimatorX = null
                objectAnimatorY = null
            }
        })

        set!!.start()
    }

    /** キャラ攻撃時の振動アニメーション */
    fun attackAnim(attack: ImageView, defense: ImageView, waveAnimationView: WaveAnimationLayout, sa1: Array<Float>, sa2: Array<Float>, dir: Int){
        val defenseLocation = IntArray(2)
        defense.getLocationOnScreen(defenseLocation)
        val waveLocation = IntArray(2)
        waveAnimationView.getLocationOnScreen(waveLocation)
        val width = waveAnimationView.width
        init(waveAnimationView, 0f, defenseLocation[0].toFloat()-(waveLocation[0]+width/2-100).toFloat(), 0f, (defenseLocation[1]-50-waveLocation[1]).toFloat(), 0)
        Anim(attack, defense, sa1, sa2, dir, 300)
    }

    /** ペイン床の追加ダメージアニメーション */
    fun painAnim(waveAnimationView: WaveAnimationLayout, saX: Float, saY: Float){
        val position = IntArray(2)
        img.getLocationOnScreen(position)
        val waveLocation = IntArray(2)
        waveAnimationView.getLocationOnScreen(waveLocation)
        val width = waveAnimationView.width
        init(waveAnimationView, 0f, position[0].toFloat()-(waveLocation[0]+width/2-100).toFloat(), 0f, (position[1]-50-waveLocation[1]).toFloat(), 0)
        set = attackAnimSet(img, saX, saX+10f, saY, saY, 150, 1)

        // アニメーション終了した時の処理
        set!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                set = null
                objectAnimatorX = null
                objectAnimatorY = null
            }
        })

        set!!.start()
    }

    /** ダメージ表示のアニメーション */
    private fun Anim(img1: ImageView, img2: ImageView, sa1: Array<Float>, sa2: Array<Float>, dir: Int, duration: Long){
        val set1 = when (dir){
            1 -> attackAnimSet(img1, sa1[0], sa1[0], sa1[2], sa1[3]+30, duration, 0)
            2 -> attackAnimSet(img1, sa1[0], sa1[1]+30, sa1[2], sa1[2], duration, 0)
            3 -> attackAnimSet(img1, sa1[0], sa1[0], sa1[2], sa1[3]-70, duration, 0)
            else -> attackAnimSet(img1, sa1[0], sa1[1]-70, sa1[2], sa1[2], duration, 0)
        }
        val set2 = when (dir){
            1 -> attackAnimSet(img2, sa2[0], sa2[0], sa2[2], sa2[3], duration/2, 1)
            2 -> attackAnimSet(img2, sa2[0], sa2[1], sa2[2], sa2[2], duration/2, 1)
            3 -> attackAnimSet(img2, sa2[0], sa2[0], sa2[2], sa2[3]-40, duration/2, 1)
            else -> attackAnimSet(img2, sa2[0], sa2[1]-40, sa2[2], sa2[2], duration/2, 1)
        }
        set = AnimatorSet()
        set!!.play(set2).after(set1)

        // アニメーション終了した時の処理
        set!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                set = null
                objectAnimatorX = null
                objectAnimatorY = null
            }
        })

        set!!.start()
    }

    /** 攻撃アニメーションセット */
    private fun attackAnimSet(img: ImageView, saX: Float, moveX: Float, saY: Float, moveY: Float, duration: Long, mode: Int): AnimatorSet {
        val set1 = AnimatorSet()
        val set2 = AnimatorSet()
        var setDuration = duration
        if (Math.abs(saX - moveX) == 0f && Math.abs(saY - moveY) == 0f){
            setDuration = 0
        }
        set1.duration = setDuration
        objectAnimatorX = ObjectAnimator.ofFloat(img, "TranslationX", saX, moveX)
        objectAnimatorY = ObjectAnimator.ofFloat(img, "TranslationY", saY, moveY)
        set1.playTogether(objectAnimatorX, objectAnimatorY)
        set2.duration = setDuration
        objectAnimatorX = ObjectAnimator.ofFloat(img, "TranslationX", moveX, saX)
        objectAnimatorY = ObjectAnimator.ofFloat(img, "TranslationY", moveY, saY)
        set2.playTogether(objectAnimatorX, objectAnimatorY)

        set = AnimatorSet()
        set!!.play(set2).after(set1)

        if (mode == 1) {
            set!!.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationStart(animation: Animator?) {
                    img.setColorFilter(ContextCompat.getColor(mContext, R.color.char_disappear_color))
                }
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {
                    img.setColorFilter(android.R.color.transparent)
                    set = null
                }
            })
        }

        return set!!
    }

}