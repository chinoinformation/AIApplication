package com.example.mitake.aiapplication.battle.view

import android.animation.*
import android.content.Context
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.GradientDrawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.battle.BattleActivity
import com.example.mitake.aiapplication.battle.ConfirmCanAction
import com.example.mitake.aiapplication.battle.ResetMap
import com.example.mitake.aiapplication.battle.StatusChange
import com.example.mitake.aiapplication.battle.data.Battle
import com.example.mitake.aiapplication.battle.data.Place
import com.example.mitake.aiapplication.battle.data.Player
import com.example.mitake.aiapplication.bgm.EffectList
import com.example.mitake.aiapplication.data.DataManagement
import kotlinx.android.synthetic.main.fragment_change_images.*

@Suppress("DEPRECATION")
class ChangeImagesFragment : Fragment() {
    /** View */
    var allPlayerImageList: MutableList<ImageView> = mutableListOf()
    private var map: ImageView? = null
    private var turnText: TextView? = null
    var drawList: MutableList<Int> = mutableListOf()

    /** private定数 */
    private var boardSize = Battle.BoardSize.Value
    private var charNum = Battle.CharNum.Value

    /** colorアニメーション */
    var density = 0f
    // ofArgbはApi21以降だけなのでofObjectを使う。Evaluatorを適切なものに設定すればいい
    private var fromColor = 0
    private var toColor = 0
    var animator: ValueAnimator? = null
    private var drawable: GradientDrawable? = null

    /** スクロールアニメーション */
    private var set: AnimatorSet? = null
    private var objectAnimatorV1: ObjectAnimator? = null
    private var objectAnimatorV2: ObjectAnimator? = null
    private var objectAnimatorH1: ObjectAnimator? = null
    private var objectAnimatorH2: ObjectAnimator? = null
    var scrolling = false

    /** ターン数 */
    var turn: Int = 1
    val maxTurn: Int = 10
    var charturn = 0
    private var playerturn = 0
    private var charturn1 = 0
    private var charturn2 = 0

    /** マップフラグ */
    var mapFlag: Int = 0

    /** 現在ステータス情報を取得しているキャラのインデックス */
    var index = 0

    /** 残キャラ数 */
    var maxPlayerCharNum1 = 0
    var maxPlayerCharNum2 = 0
    var playerCharNum1 = 0
    var playerCharNum2 = 0

    /** クラス */
    private var mainActivity: BattleActivity? = null
    private var statusChange: StatusChange? = null
    private var data: DataManagement? = null

    /** BGM再生 */
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_change_images, container, false)

        // プリファレンスの呼び出し
        data = DataManagement(context!!)

        // AudioManagerを取得する
        val am = activity!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // 最大音量値を取得
        val mVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
        // 現在の音量を取得する
        val ringVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
        // SoundPool の設定
        audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        soundPool = SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(2)
                .build()
        effectBgm = EffectList(activity!!, soundPool)
        effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
        effectBgm!!.getList("battle_start")

        turnText = root.findViewById(R.id.turn)
        getTurn()
        map = root.findViewById(R.id.map)
        map!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        for (i in 0 until charNum){
            val char1Id = resources.getIdentifier("char1_" + (i+1).toString(), "id", activity!!.packageName)
            allPlayerImageList.add(root.findViewById(char1Id))
            drawList.add(R.drawable.background_none_player)
        }
        for (i in 0 until charNum){
            val char2Id = resources.getIdentifier("char2_" + (i+1).toString(), "id", activity!!.packageName)
            allPlayerImageList.add(root.findViewById(char2Id))
            drawList.add(R.drawable.background_none_player)
        }
        for (i in 0 until allPlayerImageList.size){
            allPlayerImageList[i].setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity = (activity as BattleActivity)
        statusChange = StatusChange()

        fromColor = android.graphics.Color.parseColor("#90EE90")
        toColor = android.graphics.Color.parseColor("#3CB371")
        animator = ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor)
        animator!!.duration = 1500
        animator!!.repeatCount = -1

        map!!.setOnClickListener { mapClick() }

        // 地図アイコンやキャラアイコンなどのクリック処理
        for (i in 0 until allPlayerImageList.size) {
            charClickEvent(i)
        }
    }

    private fun charClickEvent(i: Int){
        // キャラアイコンのクリックイベント
        // 地図アイコンクリック時は機能追加
        allPlayerImageList[i].setOnClickListener {
            if (!mainActivity!!.applyAI[charturn]) {
                if (i != index) {
                    if (mainActivity!!.imgList[i] != null) {
                        focusChar(i)
                        index = i
                        mainActivity!!.leftStatusFragment!!.getUnitStatus(mainActivity!!.charPlaceList[i], mainActivity!!.unitList[i])
                        mainActivity!!.leftStatusFragment!!.leftWatchStatus(
                                mainActivity!!.statusList[i],
                                mainActivity!!.unitList[i],
                                drawList[i],
                                mainActivity!!.parser!!.objects[mainActivity!!.numberList[i]].name,
                                mainActivity!!.charPlaceList[i]
                        )
                    }
                }
                if (mapFlag == 1 && mainActivity!!.imgList[i] != null) {
                    focusChar(i)
                    ConfirmCanAction(
                            mainActivity!!.imgList[i],
                            mainActivity!!.charPlaceList[i],
                            mainActivity!!.charMap,
                            mainActivity!!.placeList,
                            boardSize,
                            mainActivity!!.parser!!.objects[mainActivity!!.numberList[i]].move.toInt() + 1,
                            mainActivity!!.parser!!.objects[mainActivity!!.numberList[i]].attackRange.toInt() + 1,
                            context!!,
                            mainActivity!!.numberList[i]
                    ).MoveAttackImg()
                }
            }
        }
    }

    /** キャラクター画像を動的に追加 */
    fun calIndex(player: Player): Int{
        return if (player == Player.Player1){
            maxPlayerCharNum1
        } else {
            allPlayerImageList.lastIndex + 1
        }
    }

    fun addCharImg(place: Place, charId: Int, index: Int){
        val layout = change_images_layout
        val metrics = context!!.resources.displayMetrics
        val layoutParams = LinearLayout.LayoutParams((50*metrics.density+0.5f).toInt(), (40*metrics.density+0.5f).toInt())
        layoutParams.setMargins((4*metrics.density+0.5f).toInt(), (4*metrics.density+0.5f).toInt(), (4*metrics.density+0.5f).toInt(), (4*metrics.density+0.5f).toInt())
        layoutParams.weight = 1f
        val charImg = ImageView(context!!)
        charImg.setPadding((2*metrics.density+0.5f).toInt(), (2*metrics.density+0.5f).toInt(), (2*metrics.density+0.5f).toInt(), (2*metrics.density+0.5f).toInt())
        charImg.layoutParams = layoutParams

        when (place.player) {
            Player.Player1 -> {
                charImg.setBackgroundResource(R.drawable.background_player1)
                mainActivity!!.charPlaceList.add(index, place)
                mainActivity!!.charMap[mainActivity!!.charPlaceList[mainActivity!!.charPlaceList.lastIndex].Y][mainActivity!!.charPlaceList[mainActivity!!.charPlaceList.lastIndex].X] = 1
                allPlayerImageList.add(index, charImg)
                maxPlayerCharNum1 += 1
                playerCharNum1 += 1
            }
            Player.Player2 -> {
                charImg.setBackgroundResource(R.drawable.background_player2)
                mainActivity!!.charPlaceList.add(place)
                mainActivity!!.charMap[mainActivity!!.charPlaceList[mainActivity!!.charPlaceList.lastIndex].Y][mainActivity!!.charPlaceList[mainActivity!!.charPlaceList.lastIndex].X] = 2
                allPlayerImageList.add(charImg)
                maxPlayerCharNum2 += 1
                playerCharNum2 += 1
            }
            else -> charImg.setBackgroundResource(R.drawable.background_none_player)
        }
        val charName = "char" + charId.toString() + "12"
        drawList.add(resources.getIdentifier(charName, "drawable", activity!!.packageName))
        Glide.with(activity!!.applicationContext).load(resources.getIdentifier(charName, "drawable", activity!!.packageName)).into(charImg)
        // 地図アイコンやキャラアイコンなどのクリック処理
        for (i in 0 until allPlayerImageList.size) {
            allPlayerImageList[i].setOnClickListener(null)
            charClickEvent(i)
        }
        layout.addView(charImg, index)
    }

    /** スクロール位置を計算 */
    private fun calFocusCharPosition(index: Int): List<Int>{
        val centerX = mainActivity!!.coordinateScrollView!![0] + (mainActivity!!.scrollWidth / 2)
        val centerY = mainActivity!!.coordinateScrollView!![1] + (mainActivity!!.scrollHeight / 2)
        val imgLocation = IntArray(2)
        mainActivity!!.imgList[index]!!.getLocationOnScreen(imgLocation)
        return listOf(
                imgLocation[0]+mainActivity!!.hScroll!!.scrollX+mainActivity!!.imgList[index]!!.width-centerX,
                imgLocation[1]+mainActivity!!.vScroll!!.scrollY+mainActivity!!.imgList[index]!!.height-centerY
        )
    }

    /** スクロール位置を変更 */
    private fun focusChar(index: Int){
        val position = calFocusCharPosition(index)
        focusAnim(position, 0, false)
    }

    /** スクロールアニメーション */
    private fun focusAnim(position: List<Int>, duration: Long, damageEnd: Boolean){
        mainActivity!!.canScroll = false
        scrolling = true
        set = AnimatorSet()
        set!!.duration = duration
        objectAnimatorV1 = ObjectAnimator.ofInt(mainActivity!!.vScroll!!, "scrollX", mainActivity!!.vScroll!!.scrollX, position[0])
        objectAnimatorV2 = ObjectAnimator.ofInt(mainActivity!!.vScroll!!, "scrollY", mainActivity!!.vScroll!!.scrollY, position[1])
        objectAnimatorH1 = ObjectAnimator.ofInt(mainActivity!!.hScroll!!, "scrollX", mainActivity!!.hScroll!!.scrollX, position[0])
        objectAnimatorH2 = ObjectAnimator.ofInt(mainActivity!!.hScroll!!, "scrollY", mainActivity!!.hScroll!!.scrollY, position[1])
        set!!.playTogether(objectAnimatorV1!!, objectAnimatorV2!!, objectAnimatorH1!!, objectAnimatorH2!!)
        set!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                set = null
                objectAnimatorV1 = null
                objectAnimatorV2 = null
                objectAnimatorH1 = null
                objectAnimatorH2 = null
                scrolling = false
                if (mainActivity!!.initialCount == 1) {
                    // AudioManagerを取得する
                    val am = activity!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    // 最大音量値を取得
                    val mVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
                    // 現在の音量を取得する
                    val ringVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
                    effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
                    // バトル開始のアニメーション
                    effectBgm!!.play("battle_start")
                    mainActivity!!.otherImagesFragment!!.battleStartAnimation1()
                }
                if (damageEnd) mainActivity!!.damageEnd()
            }
        })
        set!!.start()
    }

    /** 戦闘開始時のスクロールアニメーション */
    fun focusCharAnim0(X: Int, Y: Int){
        val position = listOf(X, Y)
        focusAnim(position, 1500, false)
    }

    /** ターン開始時・攻撃時のスクロールアニメーション */
    fun focusCharAnim(index: Int, duration: Long, damageEnd: Boolean){
        val position = calFocusCharPosition(index)
        focusAnim(position, duration, damageEnd)
    }

    /** 移動時のスクロールアニメーション */
    fun focusCharMoveAnim(X: Int, Y: Int){
        val centerX = mainActivity!!.coordinateScrollView!![0] + (mainActivity!!.scrollWidth / 2)
        val centerY = mainActivity!!.coordinateScrollView!![1] + (mainActivity!!.scrollHeight / 2)
        val position = listOf(
                X+mainActivity!!.hScroll!!.scrollX-(mainActivity!!.imgList[mainActivity!!.changeImagesFragment!!.charturn]!!.width/2)-centerX,
                Y+mainActivity!!.vScroll!!.scrollY-(mainActivity!!.imgList[mainActivity!!.changeImagesFragment!!.charturn]!!.height/2)-centerY
        )
        focusAnim(position, 1000, false)
    }

    /** 戦闘不能時のキャラ画像をグレースケールに変更 */
    fun grayScale(num: Int) {
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)
        val filter = ColorMatrixColorFilter(matrix)
        allPlayerImageList[num].colorFilter = filter
        val drawable = GradientDrawable()
        drawable.setColor(Color.parseColor("#90000000"))
        allPlayerImageList[num].background = drawable
    }

    /** 地図アイコンのクリックイベント */
    private fun mapClick(){
        if (!mainActivity!!.applyAI[charturn]) {
            if (mapFlag == 0) {
                statusChange!!.ButtonNotEnabled(mainActivity!!.otherItemsFragment!!.attack!!)
                statusChange!!.ButtonNotEnabled(mainActivity!!.otherItemsFragment!!.move!!)
                statusChange!!.ButtonNotEnabled(mainActivity!!.otherItemsFragment!!.end!!)
                statusChange!!.ButtonNotEnabled(mainActivity!!.otherItemsFragment!!.normalAttack!!)
                statusChange!!.ButtonNotEnabled(mainActivity!!.otherItemsFragment!!.specialAttack!!)
                statusChange!!.ButtonNotEnabled(mainActivity!!.otherItemsFragment!!.attackMove!!)
                statusChange!!.ButtonNotEnabled(mainActivity!!.otherItemsFragment!!.attackEnd!!)
            } else {
                if (mainActivity!!.otherItemsFragment!!.attackFlag == 0) {
                    statusChange!!.ButtonEnabled(mainActivity!!.otherItemsFragment!!.attack!!)
                    statusChange!!.ButtonEnabled(mainActivity!!.otherItemsFragment!!.normalAttack!!)
                    if (mainActivity!!.unitList[charturn].MP > 0) statusChange!!.ButtonEnabled(mainActivity!!.otherItemsFragment!!.specialAttack!!)
                    if (mainActivity!!.otherItemsFragment!!.moveFlag == 0) statusChange!!.ButtonEnabled(mainActivity!!.otherItemsFragment!!.move!!)
                }
                statusChange!!.ButtonEnabled(mainActivity!!.otherItemsFragment!!.end!!)
                statusChange!!.ButtonEnabled(mainActivity!!.otherItemsFragment!!.attackMove!!)
                statusChange!!.ButtonEnabled(mainActivity!!.otherItemsFragment!!.attackEnd!!)
            }
            ResetMap(mainActivity!!.canmoveMap, mainActivity!!.attackMap, mainActivity!!.placeList).resetList()
            updateFlag()
        }
    }

    /** マップフラグ更新 */
    private fun updateFlag(){
        mapFlag += 1
        mapFlag = if (mapFlag > 1) 0 else mapFlag
    }

    /** ターン数取得 */
    fun getTurn(){
        turnText!!.text = Html.fromHtml(getString(R.string.turn, turn))
    }

    /** ターン数更新 */
    fun updateTurn(){
        turn += 1
        turn = Math.min(turn, maxTurn + 1)
    }

    /**
     * turn が maxTurnより大きいかどうかの判定
     * true -> ターン数を更新しバトル継続
     * false -> ターン数を更新せずバトル終了
     */
    fun judgeTurnEnd(): Boolean{
        return (turn <= maxTurn)
    }

    /*
     * 残キャラ数更新
     * mode == 0 -> 残キャラ数増加．初期化時に使用
     * mode == 1 -> 残キャラ数減少．バトル時に使用
     * 残キャラ数によってキャラの攻撃力が変化
     * 残キャラ数差 -> 1: 1.5倍，2: 2.0倍，3: 2.5倍
     */
    fun updatePlayerCharNum(index: Int, mode: Int){
        when (mode) {
            0 -> {
                when {
                    index < charNum -> playerCharNum1 += 1
                    else -> playerCharNum2 += 1
                }
            }
            else -> {
                when {
                    index < charNum -> playerCharNum1 -= 1
                    else -> playerCharNum2 -= 1
                }
                val sa = playerCharNum1 - playerCharNum2
                for (i in 0 until 2*charNum){
                    if (i < charNum){
                        if (sa < 0){
                            mainActivity!!.unitList[i].pinchPower = 1.0 - 0.5 * sa
                        } else if (sa >= 0){
                            mainActivity!!.unitList[i].pinchPower = 1.0
                        }
                    }
                    else {
                        if (sa > 0){
                            mainActivity!!.unitList[i].pinchPower = 1.0 + 0.5 * sa
                        } else if (sa <= 0){
                            mainActivity!!.unitList[i].pinchPower = 1.0
                        }
                    }
                }
            }
        }
    }

    /** 最大キャラ数計算 */
    fun calMaxCharNum(){
        maxPlayerCharNum1 = playerCharNum1
        maxPlayerCharNum2 = playerCharNum2
    }

    /**
     * 残キャラ数によってバトル終了かどうかの判定
     * true -> バトル継続
     * false -> バトル終了
     */
    fun judgeBattleEnd(): Boolean{
        return (playerCharNum1 > 0 && playerCharNum2 > 0)
    }

    /** colorアニメーション */
    fun colorAnimation(){
        val color = if(charturn < 4) resources.getColor(R.color.player1_color2) else resources.getColor(R.color.player2_color2)
        drawable = GradientDrawable()
        drawable!!.setColor(color)
        animator!!.addUpdateListener { valueAnimator ->
            drawable!!.setStroke((3.0f * density + 0.5f).toInt(), (valueAnimator.animatedValue as Int)) // 3dp
        }
        allPlayerImageList[charturn].background = drawable
        animator!!.start()
    }

    /** colorアニメーションキャンセル */
    fun colorAnimationCancel(){
        animator!!.cancel()
        drawable = null
        val color = if(charturn < 4) resources.getColor(R.color.player1_color2) else resources.getColor(R.color.player2_color2)
        allPlayerImageList[charturn].setBackgroundColor(color)
    }

    /** colorアニメーションリセット */
    private fun colorAnimationReset(){
        animator!!.cancel()
        drawable = null
        animator = null
    }

    /** playerTurn更新 */
    private fun updatePlayerTurn(){
        playerturn = when (playerturn) {
            0 -> 1
            else -> 0
        }
    }

    /** charTrun計算 */
    private fun calCharTurn(){
        updatePlayerTurn()
        if (playerturn == 0){
            charturn1 += 1
            if (charturn1 >= maxPlayerCharNum1) charturn1 = 0
            charturn = charturn1
        } else {
            if (turn > 1 || charturn1 > 0) charturn2 += 1
            if (charturn2 >= maxPlayerCharNum2) charturn2 = 0
            charturn = maxPlayerCharNum1 + charturn2
        }
    }

    /** charTurn更新 */
    fun updateCharTurn(imgList: MutableList<ImageView?>){
        calCharTurn()
        while (imgList[charturn] == null) {
            calCharTurn()
        }
        index = charturn
    }

    /** 全ての変数を初期化 */
    fun allInit(){
        colorAnimationReset()
        for (i in 0 until allPlayerImageList.size){
            allPlayerImageList[i].setOnClickListener(null)
            allPlayerImageList[i].setImageResource(0)
            allPlayerImageList[i].setImageDrawable(null)
        }
        map!!.setOnClickListener(null)
        map!!.setImageResource(0)
        map!!.setImageDrawable(null)
        map = null
        turnText!!.setBackgroundResource(0)
        turnText = null

        boardSize = 0
        charNum = 0
        charturn = 0
        density = 0f
        fromColor = 0
        toColor = 0

        statusChange = null
        data = null
        effectBgm!!.release()
    }

}
