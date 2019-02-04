package com.example.mitake.aiapplication.battle.view

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.battle.data.Player

@Suppress("DEPRECATION")
class RightStatusFragment : Fragment() {

    /** View */
    private var progressBar1: ProgressBar? = null
    private var progressBar2: ProgressBar? = null
    private var player1Text: TextView? = null
    private var player2Text: TextView? = null
    private var hp1Text: TextView? = null
    private var hp2Text: TextView? = null

    /** プレイヤーHP */
    var sumHP1: Int = 0
    var sumHP2: Int = 0
    var maxHP1: Int = 0
    var maxHP2: Int = 0

    /** アニメーション変数 */
    private var objectAnimator: ObjectAnimator? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_right_status, container, false)

        progressBar1 = root.findViewById(R.id.hpProgressBar1)
        progressBar1!!.isDrawingCacheEnabled = false
        progressBar1!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        progressBar2 = root.findViewById(R.id.hpProgressBar2)
        progressBar2!!.isDrawingCacheEnabled = false
        progressBar2!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        player1Text = root.findViewById(R.id.playertext1)
        player2Text = root.findViewById(R.id.playertext2)
        hp1Text = root.findViewById(R.id.player1HP)
        hp2Text = root.findViewById(R.id.player2HP)

        return root
    }

    /** 初期化 */
    fun init(){
        //HPバー設定
        progressBar1!!.max = sumHP1
        progressBar1!!.progress = sumHP1
        progressBar2!!.max = sumHP2
        progressBar2!!.progress = sumHP2
        maxHP1 = sumHP1
        maxHP2 = sumHP2
        sumAllHP()
    }

    /** ステータス画面の更新 */
    fun sumAllHP(){
        // HPテキスト設定
        hp1Text!!.text = Html.fromHtml(resources.getString(R.string.battle_HP, sumHP1, maxHP1))
        hp2Text!!.text = Html.fromHtml(resources.getString(R.string.battle_HP, sumHP2, maxHP2))
    }

    @SuppressLint("ObjectAnimatorBinding")
    /** ステータス画面のアニメーション */
    fun rightStatusAnimation(player: Player, preHP: Int){
        objectAnimator = when (player) {
            Player.Player1 -> ObjectAnimator.ofInt(progressBar2, "progress", preHP, sumHP2)
            else -> ObjectAnimator.ofInt(progressBar1, "progress", preHP, sumHP1)
        }
        objectAnimator!!.interpolator = DecelerateInterpolator()
        objectAnimator!!.duration = 400

        // アニメーション終了した時の処理
        var canceled = false
        objectAnimator!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }
            override fun onAnimationStart(animation: Animator?) {
                canceled = false
            }
            override fun onAnimationCancel(animation: Animator?) {
                canceled = true
            }
            override fun onAnimationEnd(animation: Animator?) {
                if (!canceled) {
                    objectAnimator = null
                    progressBar1!!.destroyDrawingCache()
                    progressBar2!!.destroyDrawingCache()
                }
            }
        })

        objectAnimator!!.start()
        sumAllHP()
    }

    /** 全ての変数を初期化 */
    fun allInit() {
        progressBar1!!.setBackgroundResource(0)
        progressBar1 = null
        progressBar2!!.setBackgroundResource(0)
        progressBar2 = null
        player1Text!!.setBackgroundResource(0)
        player1Text = null
        player2Text!!.setBackgroundResource(0)
        player2Text = null
        hp1Text!!.setBackgroundResource(0)
        hp1Text = null
        hp2Text!!.setBackgroundResource(0)
        hp2Text = null

        sumHP1 = 0
        sumHP2 = 0
        maxHP1 = 0
        maxHP2 = 0

    }

}
