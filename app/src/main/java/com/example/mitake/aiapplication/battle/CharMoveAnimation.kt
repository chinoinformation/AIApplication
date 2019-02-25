package com.example.mitake.aiapplication.battle

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.widget.ImageView
import com.example.mitake.aiapplication.GlideAnim
import com.example.mitake.aiapplication.battle.view.OtherItemsFragment

class CharMoveAnimation(image: ImageView, place: List<List<ImageView>>, fragment: OtherItemsFragment, context: Context, no: Int) {
    private val img = image
    val otherItems = fragment
    private val mContext = context
    private val mNo: Int = no

    private val center = MapInformation(place).getCenter(MapInformation(place))

    private var listX: MutableList<Int> = mutableListOf()
    private var listY: MutableList<Int> = mutableListOf()
    var optimizeMoveList: MutableList<List<MutableList<Int>>> = mutableListOf()

    private var set: AnimatorSet? = null
    private var objectAnimatorX: ObjectAnimator? = null
    private var objectAnimatorY: ObjectAnimator? = null

    /** 最初のキャラの位置を確定 */
    fun init(saX: Float, moveX: Float, saY: Float, moveY: Float, duration: Long){
        set = AnimatorSet()
        var setDuration = duration
        if (Math.abs(saX - moveX) == 0f && Math.abs(saY - moveY) == 0f){
            setDuration = 0
        }
        set!!.duration = setDuration
        val objectAnimatorX = ObjectAnimator.ofFloat(img, "TranslationX", saX, moveX)
        val objectAnimatorY = ObjectAnimator.ofFloat(img, "TranslationY", saY, moveY)
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
            override fun onAnimationEnd(animation: Animator?) { set = null }
        })

        set!!.start()
    }

    /** リストを初期化 */
    private fun initList(){
        listX = mutableListOf()
        listY = mutableListOf()
        optimizeMoveList= mutableListOf()
    }

    /** 求めておいた移動経路のリストを，アニメーションを行うためのリストに変換 */
    private fun convertList(preX: Int, preY: Int, route: MutableList<Int>){
        var x = preX
        var y = preY

        listX.add(x)
        listY.add(y)
        for (i in 0..route.lastIndex) {
            when (route[i]) {
                1 -> x -= 1
                2 -> x += 1
                3 -> y -= 1
                else -> y += 1
            }
            listX.add(x)
            listY.add(y)
        }
        optimizeMoveList.add(listOf(listX, listY))
    }

    /** キャラ移動の際に，異なるプレイヤーのキャラが重なることを防止する関数 */
    fun optimizeCharMove(saX: Int, saY: Int, preX: Int, preY: Int, route: MutableList<Int>, duration: Long): Pair<MutableList<Animator>, Long>{
        initList()
        convertList(preX, preY, route)

        // 現在位置
        var startX = saX
        var startY = saY
        // move座標と現在位置の差分
        var diffX = 0
        var diffY = 0
        // 終点
        var X = 0
        var Y = 0

        // アニメーションリスト
        val animatorList: MutableList<Animator> = mutableListOf()

        // アニメーションリストにキャラ移動のアニメーション変数を格納
        var count = 1
        var s = 0
        var e = s + 1
        if ( optimizeMoveList[0][0].lastIndex > 0) {
            while (e <= optimizeMoveList[0][0].lastIndex) {
                while (e + 1 <= optimizeMoveList[0][0].lastIndex && (optimizeMoveList[0][0][e+1] == optimizeMoveList[0][0][s] || optimizeMoveList[0][1][e+1] == optimizeMoveList[0][1][s])){
                    e += 1
                }
                // move座標と現在位置の差分
                diffX = center[optimizeMoveList[0][0][e]][0][0] - center[optimizeMoveList[0][0][s]][0][0]
                diffY = center[0][optimizeMoveList[0][1][e]][1] - center[0][optimizeMoveList[0][1][s]][1]
                // 終点
                X = startX + diffX
                Y = startY + diffY

                objectAnimatorX = ObjectAnimator.ofFloat(img, "TranslationX", startX.toFloat(), X.toFloat())
                objectAnimatorY = ObjectAnimator.ofFloat(img, "TranslationY", startY.toFloat(), Y.toFloat())

                // キャラ移動のアニメション変数
                val bitmapName: String = "char" + mNo.toString() + "11"
                var drawName = "char" + mNo.toString() + "_"
                drawName += if (diffX != 0) {
                    if (diffX < 0){
                        "4"
                    }else{
                        "2"
                    }
                } else {
                    if (diffY < 0){
                        "3"
                    }else{
                        "1"
                    }
                }

                if (diffX == 0) {
                    objectAnimatorY!!.addListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {}
                        override fun onAnimationStart(animation: Animator?) {
                            GlideAnim().animation(mContext, img, bitmapName, drawName)
                        }
                        override fun onAnimationCancel(animation: Animator?) {}
                        override fun onAnimationEnd(animation: Animator?) {}
                    })
                    animatorList.add(objectAnimatorY!!)
                }else{
                    objectAnimatorX!!.addListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {}
                        override fun onAnimationStart(animation: Animator?) {
                            GlideAnim().animation(mContext, img, bitmapName, drawName)
                        }
                        override fun onAnimationCancel(animation: Animator?) {}
                        override fun onAnimationEnd(animation: Animator?) {}
                    })
                    animatorList.add(objectAnimatorX!!)
                }
                objectAnimatorX = null
                objectAnimatorY = null

                // 変数の更新
                startX += diffX
                startY += diffY
                s = e
                e = s + 1
                count += 1
            }
        }

        // アニメション
        val d = if (optimizeMoveList[0][0].lastIndex == 0) 0 else ((duration / count ) + 100)
        return Pair(animatorList, d)
    }

}