package com.example.mitake.aiapplication.battle

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.battle.ai.WeakAI
import com.example.mitake.aiapplication.battle.data.*
import com.example.mitake.aiapplication.battle.data.Unit
import com.example.mitake.aiapplication.battle.view.*
import com.example.mitake.aiapplication.bgm.EffectList
import com.example.mitake.aiapplication.bgm.MyService
import com.example.mitake.aiapplication.custom_layout.CsvReader
import com.example.mitake.aiapplication.custom_layout.WaveAnimationLayout
import com.example.mitake.aiapplication.data.DataManagement
import kotlinx.android.synthetic.main.activity_battle.*
import java.util.*
import com.example.mitake.aiapplication.battle.record.DataLog
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import org.json.JSONObject
import java.lang.ref.WeakReference


@Suppress("DEPRECATION")
class BattleActivity : AppCompatActivity(){

    /** BGM再生 */
    var bgmId = R.raw.bgm_battle
    private var bgmFlag = 0
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null
    private var am: AudioManager? = null
    private var mVol: Float = 0f
    private var ringVolume: Float = 0f

    private val mHandler = Handler()

    /** 初期化変数 */
    var initialCount = 0

    /** デフォルトデータ */
    private var boardSize: Int = Battle.BoardSize.Value
    var charNum: Int = Battle.CharNum.Value

    /** カスタムビュー */
    var changeImagesFragment: ChangeImagesFragment? = null
    var leftStatusFragment: LeftStatusFragment? = null
    var otherItemsFragment: OtherItemsFragment? = null
    private var rightStatusFragment: RightStatusFragment? = null
    private var scrollMapFragment: ScrollMapFragment? = null
    var otherImagesFragment: OtherImagesFragment? = null

    /** マップチップ変数 */
    lateinit var placeList: List<List<ImageView>>
    var mapType = Array(boardSize, { Array(boardSize, { 0 }) })
    var attackMap = Array(boardSize, { Array(boardSize, { 0 }) })
    var charMap = Array(boardSize, { Array(boardSize, { 0 }) })
    var canmoveMap = Array(boardSize, { Array(boardSize, { 0 }) })
    var route: MutableList<Map<Int, MutableList<Int>>> = mutableListOf()

    /** キャラチップ変数 */
    private var partyNum: Int = 0
    private var charId: MutableList<Int> = mutableListOf(1,5,9,13)
    var numberList: MutableList<Int> = mutableListOf()
    var imgList: MutableList<ImageView?> = mutableListOf()
    private var bmpList: MutableList<MutableList<Int>> = mutableListOf()
    var density: Float = 0f

    /** ユニットのステータス */
    private val notCharStatus = Status(0, 0, 0.0, 0.0, "")      // 空ステータス
    var statusList: MutableList<Status> = mutableListOf()
    var unitList: MutableList<Unit> = mutableListOf()

    /** ユニットの場所管理変数 */
    val notCharPlace = Place(boardSize, boardSize, Player.NONE)     // 空位置リスト
    var startXList: MutableList<Int> = mutableListOf()
    var startYList: MutableList<Int> = mutableListOf()
    private val char11Place = Place(1,7,Player.Player1)
    private val char12Place = Place(2,5,Player.Player1)
    private val char13Place = Place(5,5,Player.Player1)
    private val char14Place = Place(6,7,Player.Player1)
    private val char21Place = Place(1,0,Player.Player2)
    private val char22Place = Place(2,2,Player.Player2)
    private val char23Place = Place(5,2,Player.Player2)
    private val char24Place = Place(6,0,Player.Player2)
    var charPlaceList: MutableList<Place>
            = mutableListOf(char11Place, char12Place, char13Place, char14Place,
                            char21Place, char22Place, char23Place, char24Place)

    /** AI */
    private var enemyAIType: String = "DeepAI"
    var aiMovePlace: Place? = null
    var AIModel: WeakAI? = null
    var applyAI: MutableList<Boolean>
            = mutableListOf(false, false, false, false,
                            true, true, true, true)

    /** ダメージ表示のテキスト */
    private var damageTextMessage: TextView? = null

    /** move座標計算変数 */
    var flagList: MutableList<Int> = mutableListOf()
    /** 攻撃変数 */
    private var attackList: Pair<List<Int>, String> = Pair(listOf(), "")

    /** 通常攻撃 or 特殊攻撃 */
    var attackType: Int = 0

    /** ターン */
    var firstTurnCharNum = 0

    /** 斜めスクロールビュー */
    private var mx: Float = 0f
    private var my: Float = 0f
    var vScroll: ScrollView? = null
    var hScroll: HorizontalScrollView? = null
    var canScroll = false
    var scrollWidth = 0
    var scrollHeight = 0
    var coordinateScrollView: IntArray? = IntArray(2)

    /** クラス */
    private var data: DataManagement? = null
    private var statusChange: StatusChange? = null
    var parser: CsvReader? = null
    var dataLog: DataLog? = null
    private var charMoveAnimation: CharMoveAnimation? = null

    @SuppressLint("ResourceType", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battle)

        statusChange = StatusChange()
        parser = CsvReader()
        dataLog = DataLog()
        // プリファレンス
        data = DataManagement(this)
        // csvデータを読み込み
        parser!!.reader(this)

        // 敵AIの種類
        //enemyAIType = intent.getStringExtra("AI_type")

        // カスタムビュー更新
        leftStatusFragment = left_status as LeftStatusFragment
        rightStatusFragment = right_status as RightStatusFragment
        changeImagesFragment = char_images as ChangeImagesFragment
        otherItemsFragment = other_images as OtherItemsFragment
        scrollMapFragment = scroll_map as ScrollMapFragment
        otherImagesFragment = others as OtherImagesFragment

        // マップ初期化
        placeList = scrollMapFragment!!.initMap()
        vScroll = findViewById(R.id.vScroll)
        hScroll = findViewById(R.id.hScroll)

        // データ初期化
        imgList = scrollMapFragment!!.charImgList
        // キャラデータ読み込み
        partyNum = data!!.readData("organization_page", "0")[0].toInt()
        val key = "char_organization_status" + (partyNum+1).toString()
        var values = ""
        for (i in 0 until charId.size){
            values += when {
                i < charId.size-1 -> charId[i].toString() + ","
                else -> charId[i].toString()
            }
        }
        for (i in 0 until (2*charNum)){
            if (imgList[i] != null) {
                // ここはデータ保存の部分ができたら直します
                var no = if (i < 4) data!!.readData(key, values)[i].toInt() else 0
                if (i >= 4) {
                    val enemyNumList = mutableListOf(4 * (i-4) + 1, 4 * (i-4) + 2, 4 * (i-4) + 3, 4 * (i-4) + 4)
                    enemyNumList.remove(numberList[i-4])
                    enemyNumList.shuffle()
                    no = enemyNumList[0]
                }
                initStatus(no, 0)
            }else{
                initStatus(0, 1)
            }
        }
        // 最初のターンのキャラの場所とステータスを指定
        leftStatusFragment!!.getUnitStatus(charPlaceList[0], unitList[0])

        // charMapの更新
        for (i in (0 until charPlaceList.size)){
            when {
                i < 4 -> charMap[charPlaceList[i].Y][charPlaceList[i].X] = 1
                else -> charMap[charPlaceList[i].Y][charPlaceList[i].X] = 2
            }
        }

        // ボタン
        statusChange!!.ButtonNotEnabled(otherItemsFragment!!.attack!!)
        statusChange!!.ButtonNotEnabled(otherItemsFragment!!.move!!)
        statusChange!!.ButtonNotEnabled(otherItemsFragment!!.end!!)

        // 状態異常の更新
        statusChange!!.getStateRes(applicationContext, leftStatusFragment!!.attackState!!, leftStatusFragment!!.defenseState!!, leftStatusFragment!!.spState!!, leftStatusFragment!!.otherState!!, charPlaceList[0], unitList[0], mapType)

        // ダメージ表示のテキスト
        damageTextMessage = findViewById(R.id.text_message)
        val damageText = getString(R.string.battle_character, "#7000bfff", parser!!.objects[numberList[0]].name) + getString(R.string.turn_message)
        damageTextMessage!!.text = Html.fromHtml(damageText)
        dataLog!!.addLog(damageText)

        // 戦闘開始アニメーション
        otherImagesFragment!!.layoutStartFinish!!.setOnTouchListener { _, event ->
            when (initialCount) {
                2 -> {
                    if (event.action != MotionEvent.ACTION_UP) return@setOnTouchListener true
                    battleStartEvent()
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }
    }

    /** キャラステータス初期化 */
    fun initStatus(no: Int, mode: Int){
        if (mode == 0) {
            val charName: String = "char" + parser!!.objects[no].id
            numberList.add(parser!!.objects[no].id.toInt())
            val status = Status(
                    parser!!.objects[no].HP.toInt(),
                    parser!!.objects[no].MP.toInt(),
                    parser!!.objects[no].attack.toDouble(),
                    parser!!.objects[no].defense.toDouble(),
                    parser!!.objects[no].type
            )
            val bmpIdList: MutableList<Int> = mutableListOf()
            for (j in 1..4) {
                val bmpId: Int = resources.getIdentifier(charName + j.toString() + "2", "drawable", packageName)
                bmpIdList.add(bmpId)
            }
            // drawable Id
            bmpList.add(bmpIdList)
            // ステータスデータ
            statusList.add(status)
            // ユニットデータ
            unitList.add(Unit(status))
        } else {
            val bmpIdList: MutableList<Int> = mutableListOf()
            for (j in 0..3) {
                val bmpId: Int = R.drawable.background_none_player
                bmpIdList.add(bmpId)
            }
            numberList.add(1)
            // drawable Id
            bmpList.add(bmpIdList)
            // ステータスデータ
            statusList.add(notCharStatus)
            unitList.add(Unit(notCharStatus))
        }
    }

    /** キャラ戦闘開始位置の設定 */
    private fun place(){
        for (i in 0 until imgList.size) {
            if (imgList[i] != null) {
                // 戦闘開始位置に移動
                val imgLocation = Location(imgList[i]!!, placeList[charPlaceList[i].X][charPlaceList[i].Y])
                val imgMoveLocationList = imgLocation.calMoveLocation()
                CharMoveAnimation(imgList[i]!!, placeList, otherItemsFragment!!, applicationContext, numberList[changeImagesFragment!!.charturn]).init(0f, imgMoveLocationList[0].toFloat(), 0f, imgMoveLocationList[1].toFloat(), 0)
                startXList.add(imgMoveLocationList[0])
                startYList.add(imgMoveLocationList[1])
            }else{
                startXList.add(0)
                startYList.add(0)
            }
        }
    }

    /** キャラ設定 */
    @SuppressLint("ResourceType")
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (initialCount == 0) {
            // これ以降は初期化しない
            initialCount = 1
            // 戦闘開始位置設定
            place()
            // キャラ画像アニメーション
            scrollMapFragment!!.charImgAnimation(imgList, numberList)

            for (i in 0 until (2*charNum)){
                if (imgList[i] != null) {
                    // 残キャラ数計算
                    changeImagesFragment!!.updatePlayerCharNum(i, 0)
                    changeImagesFragment!!.drawList[i] = resources.getIdentifier("char" + parser!!.objects[numberList[i]].id + "12", "drawable", packageName)
                } else {
                    charMap[charPlaceList[i].Y][charPlaceList[i].X] = 0
                    charPlaceList[i] = notCharPlace
                }
                Glide.with(applicationContext).load(changeImagesFragment!!.drawList[i]).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(changeImagesFragment!!.allPlayerImageList[i])
            }
            // 最大キャラ数の計算
            changeImagesFragment!!.calMaxCharNum()
            //scrollMapFragment!!.addCharImg(Place(0,0, Player.Player2), 3, imgList, true)
            // スクロールビューの初期位置
            vScroll!!.getLocationOnScreen(coordinateScrollView)
            changeImagesFragment!!.focusCharAnim0((hScroll!!.getChildAt(0).right - hScroll!!.width) / 2, (vScroll!!.getChildAt(0).bottom - vScroll!!.height) / 2)
            // AI
            AIModel = WeakAI(applicationContext, placeList)
            // 各変数・Listを更新
            rightStatusFragment!!.sumHP1 = unitList[0].HP + unitList[1].HP + unitList[2].HP + unitList[3].HP
            rightStatusFragment!!.sumHP2 = unitList[4].HP + unitList[5].HP + unitList[6].HP + unitList[7].HP
            rightStatusFragment!!.init()
            // 最初のキャラステータスを表示
            leftStatusFragment!!.leftWatchStatus(statusList[0], unitList[0], bmpList[0][0], parser!!.objects[numberList[0]].name, charPlaceList[0])
            // 行動順アニメーション
            density = resources.displayMetrics.density
            changeImagesFragment!!.density = density
            changeImagesFragment!!.colorAnimation()
        }
    }

    /** バトル開始のアニメーション */
    @SuppressLint("ResourceType", "ClickableViewAccessibility")
    private fun battleStartEvent() {
        initialCount = 3
        // リスナーを破棄
        otherImagesFragment!!.layoutStartFinish!!.setOnTouchListener(null)
        val animationList = otherImagesFragment!!.battleStartAnimation2()
        otherImagesFragment!!.sword1!!.startAnimation(animationList[0])
        otherImagesFragment!!.sword2!!.startAnimation(animationList[1])
        animationList[1].setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(arg0: Animation) {
                otherImagesFragment!!.textStartFinish!!.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.animator.zoom_out))
            }
            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                scrollWidth = vScroll!!.width
                scrollHeight = vScroll!!.height
                changeImagesFragment!!.focusCharAnim(changeImagesFragment!!.charturn, 900, false)
                if (applyAI[changeImagesFragment!!.charturn]) {
                    otherItemsFragment!!.buttonChange(2)
                    //applyAIMove(this@BattleActivity).execute()
                    applyAIMove()
                } else {
                    route = MoveSearch(applicationContext, canmoveMap, placeList, charMap, charPlaceList[changeImagesFragment!!.charturn]).getMoveRange(parser!!.objects[numberList[0]].move.toInt() + 1, otherItemsFragment!!.moveFlag)
                    CharSearch(applicationContext, charMap, placeList, charPlaceList[changeImagesFragment!!.charturn]).plotPlayer()
                    // 攻撃・終了ボタンを有効化
                    statusChange!!.ButtonEnabled(otherItemsFragment!!.attack!!)
                    statusChange!!.ButtonEnabled(otherItemsFragment!!.end!!)
                }
            }
        })
    }

    /** 斜めスクロール */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (!changeImagesFragment!!.scrolling && !applyAI[changeImagesFragment!!.charturn]) {
            val curX: Float
            val curY: Float

            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    mx = ev.x
                    my = ev.y
                    val coordinateVScroll = calCoordinateScrollView()
                    canScroll = coordinateVScroll[0] <= mx && mx <= coordinateVScroll[0] + scrollWidth && coordinateVScroll[1] <= my && my <= coordinateVScroll[1] + scrollHeight
                }
                MotionEvent.ACTION_MOVE -> {
                    if (canScroll) {
                        curX = ev.x
                        curY = ev.y
                        vScroll!!.scrollBy((mx - curX).toInt(), (my - curY).toInt())
                        hScroll!!.scrollBy((mx - curX).toInt(), (my - curY).toInt())
                        mx = curX
                        my = curY
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (canScroll) {
                        curX = ev.x
                        curY = ev.y
                        vScroll!!.scrollBy((mx - curX).toInt(), (my - curY).toInt())
                        hScroll!!.scrollBy((mx - curX).toInt(), (my - curY).toInt())
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /** スクロールビューの座標を算出 */
    private fun calCoordinateScrollView(): IntArray{
        val location = IntArray(2)
        vScroll!!.getLocationOnScreen(location)
        return location
    }

    /** 全キャラのステータスリスト算出 */
    fun calStatusList(): Array<Array<Int>>{
        val res = Array(imgList.size, { Array(8, { 0 }) })
        for (i in 0 until imgList.size){
            val type = when (parser!!.objects[numberList[i]].type){
                "バランス型" -> 0
                "攻撃型" -> 1
                "防御型" -> 2
                else -> 3
            }
            if (imgList[i] != null){
                val sp = when (unitList[i].SP){
                    0 -> 0
                    else -> 1
                }
                res[i] = arrayOf(
                        unitList[i].HP,
                        unitList[i].MP,
                        unitList[i].Attack.toInt(),
                        unitList[i].Defence.toInt(),
                        parser!!.objects[numberList[i]].move.toInt(),
                        parser!!.objects[numberList[i]].attackRange.toInt(),
                        type,
                        sp
                )
            } else {
                res[i] = arrayOf(
                        0,
                        0,
                        unitList[i].Attack.toInt(),
                        unitList[i].Defence.toInt(),
                        parser!!.objects[numberList[i]].move.toInt(),
                        parser!!.objects[numberList[i]].attackRange.toInt(),
                        type,
                        0
                )
            }
        }
        return res
    }

    /** 現在操作しているキャラを考慮したcharMapの値の変更 */
    fun changeCharMap(charMap: Array<Array<Int>>): Array<Array<Int>>{
        val res = Array(boardSize, { Array(boardSize, { 0 }) })
        for (i in 0 until res.size){
            for (j in 0 until res[0].size) {
                res[i][j] = charMap[i][j]
            }
        }
        return res
    }

    /** マップチップのクリックイベント */
    fun onClickEvent(touchX: Int, touchY: Int){
        // 初期化
        val mapInformation = MapInformation(placeList)
        // タッチイベント変数を定義
        val onClick = MoveLocation(mapInformation.getCenter(mapInformation))
        // クリック時の処理
        flagList = onClick.moveMap(touchX, touchY)
        val res = onClick.onMoveEvent(
                imgList[changeImagesFragment!!.charturn]!!,
                startXList[changeImagesFragment!!.charturn],
                startYList[changeImagesFragment!!.charturn],
                flagList
        )
        if ( changeImagesFragment!!.judgeTurnEnd() && changeImagesFragment!!.mapFlag == 0) {
            /* -------------------------- 移動処理 -------------------------- */
            if (otherItemsFragment!!.moveFlag == 0) moveEvent(res)
            /* -------------------------- 攻撃処理 -------------------------- */
            if (otherItemsFragment!!.attackFlag == 0) attackEvent(res)
        }
    }

    /** 移動イベント */
    private fun moveEvent(resMoveList: List<Int>){
        // move処理
        if (flagList[0] == 0 && resMoveList[0] == 0 && (canmoveMap[flagList[4]][flagList[2]] > 0 || applyAI[changeImagesFragment!!.charturn])) {
            // 全ボタンを無効化
            statusChange!!.ButtonNotEnabled(otherItemsFragment!!.attack!!)
            statusChange!!.ButtonNotEnabled(otherItemsFragment!!.move!!)
            statusChange!!.ButtonNotEnabled(otherItemsFragment!!.end!!)
            // 次の行動までキャラクターの移動不可
            otherItemsFragment!!.moveFlag = 1
            // アニメーション
            val label = flagList[4]*boardSize + flagList[2]
            charMoveAnimation = CharMoveAnimation(
                    imgList[changeImagesFragment!!.charturn]!!,
                    placeList,
                    otherItemsFragment!!,
                    applicationContext,
                    numberList[changeImagesFragment!!.charturn]
            )
            val moveList = charMoveAnimation!!.optimizeCharMove(
                    startXList[changeImagesFragment!!.charturn],
                    startYList[changeImagesFragment!!.charturn],
                    charPlaceList[changeImagesFragment!!.charturn].X,
                    charPlaceList[changeImagesFragment!!.charturn].Y,
                    route[label].getValue(label),
                    1500
            )
            scrollMapFragment!!.charMoveAnimation(moveList.first, flagList, moveList.second)
            // 一個前の座標を保持
            startXList[changeImagesFragment!!.charturn] += resMoveList[3]
            startYList[changeImagesFragment!!.charturn] += resMoveList[4]
            // 移動バグ防止
            flagList[0] = 1
            // 移動可能範囲を消す
            ResetMap(canmoveMap, attackMap, placeList).resetList()
            // charPlaceを更新
            charMap[charPlaceList[changeImagesFragment!!.charturn].Y][charPlaceList[changeImagesFragment!!.charturn].X] = 0
            charPlaceList[changeImagesFragment!!.charturn].X = flagList[2]
            charPlaceList[changeImagesFragment!!.charturn].Y = flagList[4]
            charMap[charPlaceList[changeImagesFragment!!.charturn].Y][charPlaceList[changeImagesFragment!!.charturn].X] =
                    when (charPlaceList[changeImagesFragment!!.charturn].player) {
                        Player.Player1 -> 1
                        Player.Player2 -> 2
                        else -> 0
                    }
            // 状態異常の更新
            leftStatusFragment!!.getUnitStatus(charPlaceList[changeImagesFragment!!.charturn], unitList[changeImagesFragment!!.charturn])
            leftStatusFragment!!.leftWatchStatus(
                    statusList[changeImagesFragment!!.charturn],
                    unitList[changeImagesFragment!!.charturn],
                    bmpList[changeImagesFragment!!.charturn][0],
                    parser!!.objects[numberList[changeImagesFragment!!.charturn]].name,
                    charPlaceList[changeImagesFragment!!.charturn]
            )
        }
    }

    /** 攻撃イベント */
    private fun attackEvent(resAttackList: List<Int>){
        // エフェクトの音量更新
        effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
        // 全体のダメージ前のHPを計算
        val preHP =
                if (charPlaceList[changeImagesFragment!!.charturn].player == Player.Player2) unitList[0].HP + unitList[1].HP + unitList[2].HP + unitList[3].HP
                else unitList[4].HP + unitList[5].HP + unitList[6].HP + unitList[7].HP
        val attackMethod = Attack(imgList[changeImagesFragment!!.charturn]!!, placeList, charMap, attackMap, charPlaceList[changeImagesFragment!!.charturn], unitList, applicationContext, numberList[changeImagesFragment!!.charturn])
        if (flagList[0] == 0 && resAttackList[0] == 0 && (attackMap[flagList[4]][flagList[2]] == 1 || applyAI[changeImagesFragment!!.charturn])) {
            attackList = if (attackType == 0)
                attackMethod.normalAttack(flagList[2], flagList[4], charPlaceList, mapType, unitList[changeImagesFragment!!.charturn])
            else
                attackMethod.specialAttack(flagList[2], flagList[4], charPlaceList, mapType, unitList[changeImagesFragment!!.charturn], unitList[changeImagesFragment!!.charturn].type, changeImagesFragment!!.charturn)
            flagList[0] = 1
            if (attackList.first[0] != 0) {
                // スクロールアニメーション
                changeImagesFragment!!.focusCharAnim(attackList.first[1], 700, false)
                // log
                val colorList = if (changeImagesFragment!!.charturn < 4) listOf("#7000bfff", "#70ff4500") else listOf("#70ff4500", "#7000bfff")
                if (attackType == 0) {
                    dataLog!!.addLog(getString(R.string.battle_character, colorList[0], parser!!.objects[numberList[changeImagesFragment!!.charturn]].name) + getString(R.string.normal_attack_message))
                } else {
                    dataLog!!.addLog(getString(R.string.battle_character, colorList[0], parser!!.objects[numberList[changeImagesFragment!!.charturn]].name) + getString(R.string.special_attack_message))
                }
                // 全ボタンを無効化
                otherItemsFragment!!.allButtonNotEnabled()
                // 攻撃可能範囲を消す
                ResetMap(canmoveMap, attackMap, placeList).resetList()
                // キャラクターの向きを変更
                val dir = attackMethod.charAttackAnim(flagList[2], flagList[4])
                // ダメージ表示のアニメーション
                val saAttack: Array<Float> = arrayOf(
                        startXList[changeImagesFragment!!.charturn].toFloat(),
                        startXList[changeImagesFragment!!.charturn].toFloat()+20f,
                        startYList[changeImagesFragment!!.charturn].toFloat(),
                        startYList[changeImagesFragment!!.charturn].toFloat()+20f
                )
                val saDefense: Array<Float> = arrayOf(
                        startXList[attackList.first[1]].toFloat(),
                        startXList[attackList.first[1]].toFloat()+20f,
                        startYList[attackList.first[1]].toFloat(),
                        startYList[attackList.first[1]].toFloat()+20f
                )
                when (attackType) {
                    0 -> effectBgm!!.play("normal_attack")
                    else -> effectBgm!!.play("sp_attack")
                }
                attackMethod.attackAnim(imgList[changeImagesFragment!!.charturn]!!, imgList[attackList.first[1]]!!, scrollMapFragment!!.damageText!!, saAttack, saDefense, dir)
                scrollMapFragment!!.setDamageText(attackList.first[4].toString(), 750)
                // 0.75秒後に処理を実行する
                mHandler.postDelayed({
                    when (attackList.second){
                        getString(R.string.critical_message) -> effectBgm!!.play("critical")
                        getString(R.string.miss_message) -> effectBgm!!.play("miss")
                        else -> effectBgm!!.play("damage")
                    }
                    leftStatusFragment!!.damageFlag = 1
                    // メッセージ
                    val logMessage = attackList.second + getString(R.string.battle_character, colorList[1], parser!!.objects[numberList[attackList.first[1]]].name) + getString(R.string.log_message, attackList.first[4])
                    damageTextMessage!!.text = Html.fromHtml(logMessage)
                    dataLog!!.addLog(logMessage)
                    // ステータス画面のアニメーション
                    rightStatusFragment!!.sumHP1 = unitList[0].HP + unitList[1].HP + unitList[2].HP + unitList[3].HP
                    rightStatusFragment!!.sumHP2 = unitList[4].HP + unitList[5].HP + unitList[6].HP + unitList[7].HP
                    leftStatusFragment!!.getUnitStatus(charPlaceList[attackList.first[1]], unitList[attackList.first[1]])
                    leftStatusFragment!!.leftStatusAnimation(
                            statusList[attackList.first[1]],
                            unitList[attackList.first[1]],
                            bmpList[attackList.first[1]][0],
                            attackList.first[2],
                            attackList.first[3],
                            parser!!.objects[numberList[attackList.first[1]]].name,
                            charPlaceList[attackList.first[1]]
                    )
                    rightStatusFragment!!.rightStatusAnimation(charPlaceList[changeImagesFragment!!.charturn].player, preHP)
                }, 750)
                damageAnimation(0, attackMethod)
                otherItemsFragment!!.attackFlag = 1
                // 移動・通常攻撃・特殊攻撃のボタンを無効化
                statusChange!!.ButtonNotEnabled(otherItemsFragment!!.move!!)
                statusChange!!.ButtonNotEnabled(otherItemsFragment!!.attack!!)
                statusChange!!.ButtonNotEnabled(otherItemsFragment!!.normalAttack!!)
                statusChange!!.ButtonNotEnabled(otherItemsFragment!!.specialAttack!!)
            }
        }
    }

    /** WeakAI適用時の行動 */
    private fun applyWeakAIMove(){
        // 移動
        val moveNextList = AIModel!!.computeNext(
                charPlaceList[changeImagesFragment!!.charturn],
                parser!!.objects[numberList[changeImagesFragment!!.charturn]].move.toInt() + 1,
                parser!!.objects[numberList[changeImagesFragment!!.charturn]].attackRange.toInt() + 1,
                charMap
        )
        CharSearch(applicationContext, charMap, placeList, charPlaceList[changeImagesFragment!!.charturn]).plotPlayer()
        aiMovePlace = moveNextList.first
        route = moveNextList.second
        mHandler.postDelayed({
            val mapInformation = MapInformation(placeList)
            val onClick = MoveLocation(mapInformation.getCenter(mapInformation))
            flagList = onClick.moveMap(aiMovePlace!!.X, aiMovePlace!!.Y)
            val res = onClick.onMoveEvent(imgList[changeImagesFragment!!.charturn]!!, startXList[changeImagesFragment!!.charturn], startYList[changeImagesFragment!!.charturn], flagList)
            moveEvent(res)
        }, 1000)
    }

    private class applyDeepAIMove internal constructor(context: BattleActivity): AsyncTask<Void, Void, String>() {

        private val activityReference: WeakReference<BattleActivity> = WeakReference(context)

        override fun onPreExecute() {
            super.onPreExecute()
            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) return
            activity.otherItemsFragment!!.load()
        }

        override fun doInBackground(vararg params: Void): String {
            return postHtml1().toString()
        }

        override fun onPostExecute(result: String) {
            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) return
            val data = result.toInt()

            // 移動
            val moveNextList = activity.AIModel!!.computeNext(
                    activity.charPlaceList[activity.changeImagesFragment!!.charturn],
                    activity.parser!!.objects[activity.numberList[activity.changeImagesFragment!!.charturn]].move.toInt() + 1,
                    activity.parser!!.objects[activity.numberList[activity.changeImagesFragment!!.charturn]].attackRange.toInt() + 1,
                    activity.charMap
            )
            CharSearch(activity.applicationContext, activity.charMap, activity.placeList, activity.charPlaceList[activity.changeImagesFragment!!.charturn]).plotPlayer()
            activity.aiMovePlace = moveNextList.first
            activity.route = moveNextList.second
            Handler().postDelayed({
                val mapInformation = MapInformation(activity.placeList)
                val onClick = MoveLocation(mapInformation.getCenter(mapInformation))
                activity.flagList = onClick.moveMap(activity.aiMovePlace!!.X, activity.aiMovePlace!!.Y)
                val res = onClick.onMoveEvent(
                        activity.imgList[activity.changeImagesFragment!!.charturn]!!,
                        activity.startXList[activity.changeImagesFragment!!.charturn],
                        activity.startYList[activity.changeImagesFragment!!.charturn],
                        activity.flagList
                )
                activity.moveEvent(res)
            }, 1000)
        }

        private fun postHtml1(): String? {
            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) {
                return "error"
            } else {
                val client1 = OkHttpClient()
                val json = JSONObject()
                json.put("x1", activity.calStatusList().contentDeepToString())
                json.put("x2", activity.changeCharMap(activity.charMap).contentDeepToString())
                json.put("x3", activity.mapType.contentDeepToString())
                json.put("movemap", activity.canmoveMap.contentDeepToString())
                Log.d("x1", activity.calStatusList().contentDeepToString())
                Log.d("x2", activity.changeCharMap(activity.charMap).contentDeepToString())
                Log.d("x3", activity.mapType.contentDeepToString())
                Log.d("movemap", activity.canmoveMap.contentDeepToString())
                val postBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
                val req = Request.Builder().url("https://c5fh09hi43.execute-api.us-east-2.amazonaws.com/default/chainer1_map").post(postBody).build()
                val resp = client1.newCall(req).execute()
                return if (resp?.body() != null) resp.body().string() else "error"
            }
        }
    }

    private fun applyAIMove() {
        if (enemyAIType == "DeepAI" || changeImagesFragment!!.charturn < 4) {
            MoveSearch(applicationContext, canmoveMap, placeList, charMap, charPlaceList[changeImagesFragment!!.charturn]).getMoveRange(parser!!.objects[numberList[changeImagesFragment!!.charturn]].move.toInt() + 1, 0)
            CharSearch(applicationContext, charMap, placeList, charPlaceList[changeImagesFragment!!.charturn]).plotPlayer()
            applyDeepAIMove(this).execute()
        } else {
            applyWeakAIMove()
        }
    }


    private class applyDeepAIAttackType internal constructor(context: BattleActivity): AsyncTask<Void, Void, String>() {

        private val activityReference: WeakReference<BattleActivity> = WeakReference(context)

        override fun onPreExecute() {
            super.onPreExecute()
            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) return
            activity.otherItemsFragment!!.load()
        }

        override fun doInBackground(vararg params: Void): String {
            return postHtml2().toString()
        }

        override fun onPostExecute(result: String) {
            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) return
            activity.otherItemsFragment!!.finishLoad()
            val data = result.toInt()
        }

        private fun postHtml2(): String? {
            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) {
                return "error"
            } else {
                val client2 = OkHttpClient()
                val json = JSONObject()
                json.put("x1", activity.calStatusList().contentDeepToString())
                json.put("x2", activity.changeCharMap(activity.charMap).contentDeepToString())
                json.put("x3", activity.mapType.contentDeepToString())
                json.put("movemap", activity.canmoveMap.contentDeepToString())
                json.put("characters", activity.charMap.contentDeepToString())
                json.put("range", activity.parser!!.objects[activity.numberList[activity.changeImagesFragment!!.charturn]].attackRange)
                val postBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
                val req = Request.Builder().url("https://c5fh09hi43.execute-api.us-east-2.amazonaws.com/default/chainer1_map").post(postBody).build()
                val resp = client2.newCall(req).execute()
                return if (resp?.body() != null) resp.body().string() else "error"
            }
        }
    }

    private class applyDeepAIAttack internal constructor(context: BattleActivity, movePlace: Place): AsyncTask<Void, Void, String>() {

        private val activityReference: WeakReference<BattleActivity> = WeakReference(context)
        private val mMovePlace = movePlace

        override fun onPreExecute() {
            super.onPreExecute()
            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) return
            activity.otherItemsFragment!!.load()
        }

        override fun doInBackground(vararg params: Void): String {
            return postHtml3().toString()
        }

        override fun onPostExecute(result: String) {
            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) return
            activity.otherItemsFragment!!.finishLoad()
            val data = result.toInt()

            // 攻撃 or 行動終了
            if (activity.AIModel!!.canAttack) {
                Handler().postDelayed({
                    val random = Random().nextInt(2) % 2
                    if (activity.unitList[activity.changeImagesFragment!!.charturn].MP > 0) activity.otherItemsFragment!!.attack(random) else activity.otherItemsFragment!!.attack(0)
                    val attackPlace = activity.AIModel!!.computeAttack(mMovePlace)
                    Handler().postDelayed({
                        val mapInformation = MapInformation(activity.placeList)
                        val onClick = MoveLocation(mapInformation.getCenter(mapInformation))
                        activity.flagList = onClick.moveMap(attackPlace.X, attackPlace.Y)
                        val res = onClick.onMoveEvent(
                                activity.imgList[activity.changeImagesFragment!!.charturn]!!,
                                activity.startXList[activity.changeImagesFragment!!.charturn],
                                activity.startYList[activity.changeImagesFragment!!.charturn],
                                activity.flagList
                        )
                        activity.attackEvent(res)
                    }, 1000)
                }, 1000)
            }else {
                activity.AIModel!!.resetMap()
                activity.end()
            }
        }

        private fun postHtml3(): String? {
            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) {
                return "error"
            } else {
                val client3 = OkHttpClient()
                val json = JSONObject()
                json.put("x1", activity.calStatusList().contentDeepToString())
                json.put("x2", activity.changeCharMap(activity.charMap).contentDeepToString())
                json.put("x3", activity.mapType.contentDeepToString())
                json.put("movemap", activity.canmoveMap.contentDeepToString())
                json.put("characters", activity.charMap.contentDeepToString())
                json.put("range", activity.parser!!.objects[activity.numberList[activity.changeImagesFragment!!.charturn]].attackRange)
                val postBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
                val req = Request.Builder().url("https://c5fh09hi43.execute-api.us-east-2.amazonaws.com/default/chainer1_map").post(postBody).build()
                val resp = client3.newCall(req).execute()
                return if (resp?.body() != null) resp.body().string() else "error"
            }
        }
    }

    fun applyAIAttack(movePlace: Place) {
        // 攻撃 or 行動終了
        if (enemyAIType == "DeepAI" || changeImagesFragment!!.charturn < 4) {
            applyDeepAIAttackType(this).execute()
            applyDeepAIAttack(this, movePlace).execute()
        } else {
            applyWeakAIAttack(movePlace)
        }
    }

    private fun applyWeakAIAttack(movePlace: Place){
        // 攻撃 or 行動終了
        if (AIModel!!.canAttack) {
            Handler().postDelayed({
                val random = Random().nextInt(2) % 2
                if (unitList[changeImagesFragment!!.charturn].MP > 0) otherItemsFragment!!.attack(random) else otherItemsFragment!!.attack(0)
                val attackPlace = AIModel!!.computeAttack(movePlace)
                mHandler.postDelayed({
                    val mapInformation = MapInformation(placeList)
                    val onClick = MoveLocation(mapInformation.getCenter(mapInformation))
                    flagList = onClick.moveMap(attackPlace.X, attackPlace.Y)
                    val res = onClick.onMoveEvent(imgList[changeImagesFragment!!.charturn]!!, startXList[changeImagesFragment!!.charturn], startYList[changeImagesFragment!!.charturn], flagList)
                    attackEvent(res)
                }, 1000)
            }, 1000)
        }else {
            AIModel!!.resetMap()
            end()
        }
    }

    /** ペイン床にいる時の終了処理 */
    fun damageEnd(){
        // エフェクトの音量更新
        effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
        // 全体のダメージ前のHPを計算
        val preHP =
                if (charPlaceList[changeImagesFragment!!.charturn].player.other() == Player.Player2) unitList[0].HP + unitList[1].HP + unitList[2].HP + unitList[3].HP
                else unitList[4].HP + unitList[5].HP + unitList[6].HP + unitList[7].HP
        val attackMethod = Attack(imgList[changeImagesFragment!!.charturn]!!, placeList, charMap, attackMap, charPlaceList[changeImagesFragment!!.charturn], unitList, applicationContext, numberList[changeImagesFragment!!.charturn])
        val painDamage = 30
        val unitHP = unitList[changeImagesFragment!!.charturn].HP - painDamage
        unitList[changeImagesFragment!!.charturn].HP = if (unitHP > 0) unitHP else 0
        scrollMapFragment!!.setDamageText(painDamage.toString(), 100)
        attackMethod.painAnim(scrollMapFragment!!.damageText!!, startXList[changeImagesFragment!!.charturn].toFloat(), startYList[changeImagesFragment!!.charturn].toFloat())
        leftStatusFragment!!.damageFlag = 1
        // メッセージ
        val color = if (changeImagesFragment!!.charturn < 4) "#7000bfff" else "#70ff4500"
        val damageText = getString(R.string.pain_damage) + getString(R.string.battle_character, color, parser!!.objects[numberList[changeImagesFragment!!.charturn]].name) + getString(R.string.log_message, painDamage)
        damageTextMessage!!.text = Html.fromHtml(damageText)
        dataLog!!.addLog(damageText)
        // ステータス画面のアニメーション
        rightStatusFragment!!.sumHP1 = unitList[0].HP + unitList[1].HP + unitList[2].HP + unitList[3].HP
        rightStatusFragment!!.sumHP2 = unitList[4].HP + unitList[5].HP + unitList[6].HP + unitList[7].HP
        leftStatusFragment!!.getUnitStatus(charPlaceList[changeImagesFragment!!.charturn], unitList[changeImagesFragment!!.charturn])
        leftStatusFragment!!.leftStatusAnimation(
                statusList[changeImagesFragment!!.charturn],
                unitList[changeImagesFragment!!.charturn],
                bmpList[changeImagesFragment!!.charturn][0],
                unitList[changeImagesFragment!!.charturn].HP + painDamage,
                unitList[changeImagesFragment!!.charturn].MP,
                parser!!.objects[numberList[changeImagesFragment!!.charturn]].name,
                charPlaceList[changeImagesFragment!!.charturn]
        )
        rightStatusFragment!!.rightStatusAnimation(charPlaceList[changeImagesFragment!!.charturn].player.other(), preHP)
        effectBgm!!.play("damage")
        damageAnimation(1, attackMethod)
    }

    private fun damageAnimation(condition: Int, attackMethod: Attack){
        statusChange!!.ButtonNotEnabled(otherItemsFragment!!.end!!)
        statusChange!!.ButtonNotEnabled(otherItemsFragment!!.attackEnd!!)
        val unitIndex = if (condition == 0) attackList.first[1] else changeImagesFragment!!.charturn
        scrollMapFragment!!.damageText!!.startWaveAnimation(object : WaveAnimationLayout.EndAnimationListener {
            override fun onEnd() {
                // 0.6秒後に処理を実行する
                mHandler.postDelayed({
                    scrollMapFragment!!.resetDamageText()
                    val waveLocation = IntArray(2)
                    scrollMapFragment!!.damageText!!.getLocationOnScreen(waveLocation)
                    attackMethod.init(scrollMapFragment!!.damageText!!, waveLocation[0].toFloat(), 0f, waveLocation[1].toFloat(), 0f,0)
                    if (unitList[unitIndex].HP == 0){
                        // メッセージ
                        val color = if (unitIndex < 4) "#7000bfff" else "#70ff4500"
                        val damageText = getString(R.string.battle_character, color, parser!!.objects[numberList[unitIndex]].name) + getString(R.string.char_down_message)
                        dataLog!!.addLog(damageText)
                        changeImagesFragment!!.updatePlayerCharNum(unitIndex, 1)
                        effectBgm!!.play("down")
                        changeImagesFragment!!.grayScale(unitIndex)
                        leftStatusFragment!!.allStateStop()
                        scrollMapFragment!!.charDisappearEffect(condition, unitIndex)
                    } else {
                        damageAnimationFinish(condition)
                    }
                }, 600)
            }
        })
    }

    fun damageAnimationFinish(condition: Int){
        if (condition == 1 && changeImagesFragment!!.judgeBattleEnd()){
            normalEnd()
        } else if (condition == 0 && changeImagesFragment!!.judgeBattleEnd() && !applyAI[changeImagesFragment!!.charturn]){
            end()
        } else if (condition == 0 && changeImagesFragment!!.judgeBattleEnd() && applyAI[changeImagesFragment!!.charturn]) {
            AIModel!!.resetMap()
            end()
        }
    }

    /** ペイン床にいない時の終了処理 */
    @SuppressLint("ResourceType")
    private fun normalEnd(){
        dataLog!!.addLog(getString(R.string.turn_end))
        // マップ初期化
        ResetMap(canmoveMap, attackMap, placeList).resetList()
        // 行動順の更新
        changeImagesFragment!!.colorAnimationCancel()
        if (imgList[changeImagesFragment!!.charturn] == null) changeImagesFragment!!.grayScale(changeImagesFragment!!.charturn)
        changeImagesFragment!!.updateCharTurn(imgList)
        changeImagesFragment!!.colorAnimation()
        leftStatusFragment!!.getUnitStatus(charPlaceList[changeImagesFragment!!.charturn], unitList[changeImagesFragment!!.charturn])
        changeImagesFragment!!.focusCharAnim(changeImagesFragment!!.charturn, 700, false)
        // ターン数更新
        if (changeImagesFragment!!.charturn == firstTurnCharNum) changeImagesFragment!!.updateTurn()
        // ターン数と各playerの残キャラ数によって処理変更
        if (changeImagesFragment!!.judgeTurnEnd() && changeImagesFragment!!.judgeBattleEnd()) {
            // 移動可能にする
            otherItemsFragment!!.moveFlag = 0
            // 攻撃可能にする
            otherItemsFragment!!.attackFlag = 0
            // 特殊攻撃の効果削除
            unitList[changeImagesFragment!!.charturn].SP = 0
            // メッセージ初期化
            val color = if (changeImagesFragment!!.charturn < 4) "#7000bfff" else "#70ff4500"
            val damageText = getString(R.string.battle_character, color, parser!!.objects[numberList[changeImagesFragment!!.charturn]].name) + getString(R.string.turn_message)
            damageTextMessage!!.text = Html.fromHtml(damageText)
            dataLog!!.addLog(damageText)
            changeImagesFragment!!.getTurn()
            // ステータス画面
            leftStatusFragment!!.leftWatchStatus(
                    statusList[changeImagesFragment!!.charturn],
                    unitList[changeImagesFragment!!.charturn],
                    bmpList[changeImagesFragment!!.charturn][0],
                    parser!!.objects[numberList[changeImagesFragment!!.charturn]].name,
                    charPlaceList[changeImagesFragment!!.charturn]
            )
            when {
                applyAI[changeImagesFragment!!.charturn] -> {
                    otherItemsFragment!!.buttonChange(2)
                    //applyAIMove(this).execute()
                    applyAIMove()
                }
                else -> {
                    // 移動ボタン・特殊攻撃ボタン以外を有効化
                    otherItemsFragment!!.allButtonEnabled()
                    statusChange!!.ButtonNotEnabled(otherItemsFragment!!.move!!)
                    statusChange!!.ButtonNotEnabled(otherItemsFragment!!.specialAttack!!)
                    // 移動可能範囲の算出
                    route = MoveSearch(applicationContext, canmoveMap, placeList, charMap, charPlaceList[changeImagesFragment!!.charturn])
                            .getMoveRange(parser!!.objects[numberList[changeImagesFragment!!.charturn]].move.toInt()+1, otherItemsFragment!!.moveFlag)
                    CharSearch(applicationContext, charMap, placeList, charPlaceList[changeImagesFragment!!.charturn]).plotPlayer()
                    otherItemsFragment!!.buttonChange(1)
                }
            }
        } else {
            battleFinish()
        }
    }

    /** 行動終了 */
    fun end() {
        if (leftStatusFragment!!.damageFlag == 0) {
            // ペイン床にいる時
            if (mapType[charPlaceList[changeImagesFragment!!.charturn].Y][charPlaceList[changeImagesFragment!!.charturn].X] == 2) {
                statusChange!!.ButtonNotEnabled(otherItemsFragment!!.attack!!)
                statusChange!!.ButtonNotEnabled(otherItemsFragment!!.move!!)
                statusChange!!.ButtonNotEnabled(otherItemsFragment!!.end!!)
                // スクロールアニメーション
                changeImagesFragment!!.focusCharAnim(changeImagesFragment!!.charturn, 50, true)
            } else {
                normalEnd()
            }
        }
    }

    /** バトル終了 */
    @SuppressLint("ResourceType", "SetTextI18n")
    fun battleFinish(){
        // エフェクトの音量更新
        effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
        // ボタンを無効化
        otherItemsFragment!!.allButtonNotEnabled()
        // リザルト画面に移行
        mHandler.postDelayed({
            // 戦闘終了アニメーション
            val animationList = otherImagesFragment!!.battleEndAnimation()
            otherImagesFragment!!.sword1!!.startAnimation(animationList[0])
            otherImagesFragment!!.sword2!!.startAnimation(animationList[1])
            effectBgm!!.play("battle_finish")
            animationList[1].setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(arg0: Animation) {
                    val anim = AnimationUtils.loadAnimation(applicationContext, R.animator.zoom_in)
                    otherImagesFragment!!.textStartFinish!!.startAnimation(anim)
                }
                override fun onAnimationRepeat(arg0: Animation) {}
                override fun onAnimationEnd(arg0: Animation) {
                    val dummyAnim = AnimationUtils.loadAnimation(applicationContext, R.animator.sword_left_out)
                    otherImagesFragment!!.dummyImage!!.startAnimation(dummyAnim)
                    dummyAnim.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(arg0: Animation) {}
                        override fun onAnimationRepeat(arg0: Animation) {}
                        override fun onAnimationEnd(arg0: Animation) { goResult() }
                    })
                }
            })
        },500)
    }

    /** リザルト画面に移行 */
    private fun goResult(){
        Thread.sleep(500)
        val intent = Intent(this, BattleFinishActivity::class.java)
        intent.putExtra("musicId", bgmId)
        intent.putExtra("player1HP", rightStatusFragment!!.sumHP1)
        intent.putExtra("player2HP", rightStatusFragment!!.sumHP2)
        intent.putExtra("playerCharNum1", changeImagesFragment!!.playerCharNum1)
        intent.putExtra("playerCharNum2", changeImagesFragment!!.playerCharNum2)
        startActivity(intent)
        overridePendingTransition(R.animator.battle_finish_open_enter_anim, R.animator.battle_finish_open_exit_anim)
        finish()
    }

    /** 音量設定 */
    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {

        if (event.keyCode === KeyEvent.KEYCODE_VOLUME_UP) {
            // 現在の音量を取得する
            ringVolume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
            effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
            val bgmLevel = data!!.readData("bgmLevel", "1")[0].toFloat()
            val bgmVol = bgmLevel * ringVolume
            val intent = Intent(applicationContext, MyService::class.java)
            intent.putExtra("flag", 3)
            intent.putExtra("bgmLevel", bgmVol)
            startService(intent)
        }

        if (event.keyCode === KeyEvent.KEYCODE_VOLUME_DOWN) {
            // 現在の音量を取得する
            ringVolume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
            effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
            val bgmLevel = data!!.readData("bgmLevel", "1")[0].toFloat()
            val bgmVol = bgmLevel * ringVolume
            val intent = Intent(applicationContext, MyService::class.java)
            intent.putExtra("flag", 3)
            intent.putExtra("bgmLevel", bgmVol)
            startService(intent)
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onResume() {
        super.onResume()
        // プレイヤーの処理
        bgmId = R.raw.bgm_battle
        val intent = Intent(applicationContext, MyService::class.java)
        intent.putExtra("id", bgmId)
        if (bgmFlag == 0) {
            //プレイヤーの初期化
            intent.putExtra("flag", 0)
            startService(intent)
            bgmFlag = 1

            // AudioManagerを取得する
            am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            // 最大音量値を取得
            mVol = am!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
            // 現在の音量を取得する
            ringVolume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
            // プリファレンスの呼び出し
            data = DataManagement(this)
        } else {
            intent.putExtra("flag", 2)
            startService(intent)
        }

        // SoundPool の設定
        audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        soundPool = SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(2)
                .build()
        effectBgm = EffectList(applicationContext, soundPool)
        effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
        effectBgm!!.getList("battle_start")
        effectBgm!!.getList("battle_finish")
        effectBgm!!.getList("normal_attack")
        effectBgm!!.getList("damage")
        effectBgm!!.getList("critical")
        effectBgm!!.getList("miss")
        effectBgm!!.getList("down")
        effectBgm!!.getList("sp_attack")
    }

    override fun onPause() {
        super.onPause()
        if (!isFinishing) {
            val intent = Intent(applicationContext, MyService::class.java)
            intent.putExtra("id", bgmId)
            intent.putExtra("flag", 1)
            startService(intent)
        }
        effectBgm!!.release()
    }

    override fun onDestroy() {
        super.onDestroy()

        changeImagesFragment!!.allInit()
        changeImagesFragment = null
        otherImagesFragment!!.allInit()
        otherImagesFragment = null
        leftStatusFragment!!.allInit()
        leftStatusFragment = null
        otherItemsFragment!!.allInit()
        otherItemsFragment = null
        rightStatusFragment!!.allInit()
        rightStatusFragment = null

        for (i in 0 until placeList.size){
            for (j in 0 until placeList[0].size){
                placeList[i][j].setOnClickListener(null)
                placeList[i][j].setImageResource(0)
                placeList[i][j].setImageDrawable(null)
            }
        }
        for (i in 0 until imgList.size){
            if (imgList[i] != null){
                imgList[i]!!.setImageResource(0)
                imgList[i]!!.setImageDrawable(null)
                imgList[i] = null
            }
        }
        scrollMapFragment!!.allInit()
        scrollMapFragment = null

        vScroll!!.setBackgroundResource(0)
        vScroll = null
        hScroll!!.setBackgroundResource(0)
        hScroll = null
        mx = 0f
        my = 0f
        scrollWidth = 0
        scrollHeight = 0
        canScroll = false
        coordinateScrollView = null

        dataLog!!.release()
        dataLog = null

        mapType = arrayOf()
        attackMap = arrayOf()
        charMap = arrayOf()
        canmoveMap = arrayOf()
        route = mutableListOf()
        charId = mutableListOf()
        numberList = arrayListOf()
        imgList = arrayListOf()
        bmpList = arrayListOf()
        statusList = arrayListOf()
        unitList = arrayListOf()
        charPlaceList = mutableListOf()
        applyAI = mutableListOf()
        flagList = mutableListOf()
        attackList = Pair(listOf(), "")

        attackType = 0
        firstTurnCharNum = 0
        partyNum = 0
        boardSize = 0
        charNum = 0
        density = 0f

        aiMovePlace = null
        AIModel!!.init()
        AIModel = null
        damageTextMessage!!.setBackgroundResource(0)
        damageTextMessage = null
        data = null
        am = null
        mVol = 0f
        ringVolume = 0f
        statusChange = null
        parser = null
        Glide.get(this).clearMemory()
    }

    override fun onBackPressed() {}

}