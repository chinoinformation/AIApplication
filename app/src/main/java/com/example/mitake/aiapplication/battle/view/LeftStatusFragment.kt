package com.example.mitake.aiapplication.battle.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.battle.BattleActivity
import com.example.mitake.aiapplication.battle.StatusChange
import com.example.mitake.aiapplication.battle.data.*
import com.example.mitake.aiapplication.battle.data.Unit
import com.example.mitake.aiapplication.battle.menu.CharStatusDialogFragment

@Suppress("DEPRECATION")
class LeftStatusFragment : Fragment() {

    /** View */
    private var progressBar1: ProgressBar? = null
    private var progressBar2: ProgressBar? = null
    private var charText: TextView? = null
    private var hpText: TextView? = null
    private var mpText: TextView? = null
    var charImg: ImageView? = null
    var attackState: ImageView? = null
    var defenseState: ImageView? = null
    var spState: ImageView? = null
    var otherState: ImageView? = null
    var dummyLayout: LinearLayout? = null

    /** private定数 */
    private var boardSize = Battle.BoardSize.Value

    /** ユニット詳細変数 */
    var presentUnit: Unit? = Unit(Status(0, 0, 0.0, 0.0, ""))          // 空ユニットステータス
    var presentPlace: Place? = Place(boardSize, boardSize, Player.NONE)

    /** ダメージフラグ */
    var damageFlag: Int = 0

    /** クラス */
    private var mainActivity: BattleActivity? = null
    private var statusChange: StatusChange? = null

    /** アニメーション変数 */
    private var set: AnimatorSet? = null
    private var objectAnimator1: ObjectAnimator? = null
    private var objectAnimator2: ObjectAnimator? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_left_status, container, false)

        progressBar1 = root.findViewById(R.id.hpProgressBar)
        progressBar1!!.isDrawingCacheEnabled = false
        progressBar1!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        progressBar2 = root.findViewById(R.id.mpProgressBar)
        progressBar2!!.isDrawingCacheEnabled = false
        progressBar2!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        charText = root.findViewById(R.id.present_player)
        hpText = root.findViewById(R.id.HP)
        mpText = root.findViewById(R.id.MP)
        charImg = root.findViewById(R.id.Char)
        attackState = root.findViewById(R.id.attack_status)
        defenseState = root.findViewById(R.id.defense_status)
        spState = root.findViewById(R.id.sp_status)
        otherState = root.findViewById(R.id.other_status)
        dummyLayout = root.findViewById(R.id.dummy_layout_left_status)

        return root
    }

    /** 現在ステータス画面に表示されているキャラ変数の取得 */
    fun getUnitStatus(place: Place, unit: Unit){
        presentPlace = place
        presentUnit = unit
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity = (activity as BattleActivity)
        statusChange = StatusChange()

        // 左ステータス画面の画像のクリックイベント
        charImg!!.setOnClickListener {
            if (!mainActivity!!.applyAI[mainActivity!!.changeImagesFragment!!.charturn]) {
                charImg!!.isEnabled = false
                watchAbnormalState(presentPlace!!, presentUnit!!)
                Handler().postDelayed({
                    charImg!!.isEnabled = true
                }, 750)
            }
        }
    }

    fun leftWatchStatus(status: Status, unit: Unit, draw: Int, name: String, place: Place){
        charImg!!.setImageDrawable(null)
        Glide.with(activity!!.applicationContext).load(draw).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(charImg!!)
        charText!!.text = name
        StatusChange().getStateRes(context!!, attackState!!, defenseState!!, spState!!, otherState!!, place, unit, mainActivity!!.mapType)

        // HPバー設定
        progressBar1!!.max = status.hp
        progressBar1!!.progress = unit.HP
        hpText!!.text = Html.fromHtml(resources.getString(R.string.battle_HP, unit.HP, status.hp))

        // MPバー設定
        progressBar2!!.max = status.mp
        progressBar2!!.progress = unit.MP
        mpText!!.text = Html.fromHtml(resources.getString(R.string.battle_MP, unit.MP, status.mp))
    }

    @SuppressLint("ObjectAnimatorBinding")
    /** 攻撃時のステータス変更アニメーション */
    fun leftStatusAnimation(status: Status, unit: Unit, draw: Int, preHPValue: Int, preMPValue: Int, name: String, place: Place){
        leftWatchStatus(status, unit, draw, name, place)

        set = AnimatorSet()
        objectAnimator1 = ObjectAnimator.ofInt(progressBar1, "progress", preHPValue, unit.HP)
        objectAnimator2 = ObjectAnimator.ofInt(progressBar2, "progress", preMPValue, unit.MP)
        objectAnimator1!!.interpolator = DecelerateInterpolator()
        objectAnimator2!!.interpolator = DecelerateInterpolator()
        set!!.duration = 400
        set!!.playTogether(objectAnimator1, objectAnimator2)

        var canceled = false
        // アニメーション終了した時の処理
        if (damageFlag == 1) {
            set!!.addListener(object : Animator.AnimatorListener {
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
                        set = null
                        damageFlag = 0
                        objectAnimator1 = null
                        objectAnimator2 = null
                        progressBar1!!.destroyDrawingCache()
                        progressBar2!!.destroyDrawingCache()
                    }
                }
            })
        }
        set!!.start()
    }

    private fun watchAbnormalState(place: Place, unit: Unit){
        val spMessage = "（次の自身のターン開始まで）"
        val SP = when (unit.SP){
            1 -> "固定ダメージ軽減$spMessage"
            2 -> "回避率アップ$spMessage"
            else -> "変化なし"
        }
        val abnormalStateList: List<String> = when (mainActivity!!.mapType[place.Y][place.X]){
            1 -> listOf("山脈", "20% UP", "20% DOWN", "変化なし")
            2 -> listOf("毒沼", "10% DOWN", "10% DOWN", "毒（ターン終了時に固定ダメージ）")
            3 -> listOf("池", "15% DOWN", "10% UP", "変化なし")
            else -> listOf("草原", "変化なし", "変化なし", "変化なし")
        }
        val newFragment = CharStatusDialogFragment.newInstance(
                charText!!.text.toString(),
                abnormalStateList[0],
                mainActivity!!.parser!!.objects[mainActivity!!.numberList[mainActivity!!.changeImagesFragment!!.index]].type,
                hpText!!.text.toString().substring(3),
                mpText!!.text.toString().substring(3),
                abnormalStateList[1],
                abnormalStateList[2],
                SP,
                abnormalStateList[3])
        newFragment.isCancelable = false
        newFragment.show(activity!!.fragmentManager, "dialog")
    }

    /** 全ステータスアニメーション停止 */
    fun allStateStop(){
        statusChange!!.stateChangeStop(attackState!!)
        statusChange!!.stateChangeStop(defenseState!!)
        statusChange!!.stateChangeStop(spState!!)
        statusChange!!.stateChangeStop(otherState!!)
    }

    /** 全ての変数を初期化 */
    fun allInit(){
        progressBar1!!.setBackgroundResource(0)
        progressBar1 = null
        progressBar2!!.setBackgroundResource(0)
        progressBar2 = null
        charText!!.setBackgroundResource(0)
        charText = null
        hpText!!.setBackgroundResource(0)
        hpText = null
        mpText!!.setBackgroundResource(0)
        mpText = null
        charImg!!.setOnClickListener(null)
        charImg!!.setImageResource(0)
        charImg!!.setImageDrawable(null)
        charImg = null
        attackState!!.setImageResource(0)
        attackState!!.setImageDrawable(null)
        attackState = null
        defenseState!!.setImageResource(0)
        defenseState!!.setImageDrawable(null)
        defenseState = null
        spState!!.setImageResource(0)
        spState!!.setImageDrawable(null)
        spState = null
        otherState!!.setImageResource(0)
        otherState!!.setImageDrawable(null)
        otherState = null
        dummyLayout!!.setBackgroundResource(0)
        dummyLayout = null

        presentUnit = null
        presentPlace = null

        boardSize = 0
        damageFlag = 0

        statusChange = null
    }

}
