package com.example.mitake.aiapplication.battle.view

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ViewFlipper
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.battle.*
import com.example.mitake.aiapplication.battle.menu.MenuDialogFragment
import com.example.mitake.aiapplication.bgm.EffectList
import kotlinx.android.synthetic.main.attack_buttons.view.*
import kotlinx.android.synthetic.main.buttons.view.*
import kotlinx.android.synthetic.main.fragment_other_items.view.*
import com.example.mitake.aiapplication.battle.menu.AttackAbilityDialogFragment
import com.example.mitake.aiapplication.data.DataManagement
import kotlinx.android.synthetic.main.use_ai.view.*


class OtherItemsFragment : Fragment() {
    /** View */
    private var flipper: ViewFlipper? = null
    var attack: Button? = null
    var move: Button? = null
    var end: Button? = null
    var normalMenu: Button? = null
    var normalAttack: Button? = null
    var specialAttack: Button? = null
    var attackMove: Button? = null
    var attackEnd: Button? = null
    private var loadText: TextView? = null
    private var progress: ProgressBar? = null

    /** 移動と攻撃のフラグ */
    var moveFlag: Int = 0
    var attackFlag: Int = 0

    /** クラス */
    private var statusChange: StatusChange? = null
    private var mainActivity: BattleActivity? = null
    private var data: DataManagement? = null

    /** BGM再生 */
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null
    private var am: AudioManager? = null
    private var mVol: Float = 0f
    private var ringVolume: Float = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_other_items, container, false)

        // プリファレンスの呼び出し
        data = DataManagement(context!!)

        // AudioManagerを取得する
        am = activity!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // 最大音量値を取得
        mVol = am!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
        // 現在の音量を取得する
        ringVolume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
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
        effectBgm!!.getList("other_button")

        flipper = root.findViewById(R.id.button_flipper)
        attack = flipper!!.normal.attack_button
        move = flipper!!.normal.move_button
        end = flipper!!.normal.end_button
        normalMenu = flipper!!.normal.menu_button
        normalAttack = flipper!!.attack.normal_attack_button
        specialAttack = flipper!!.attack.special_attack_button
        attackMove = flipper!!.attack.attack_move_button
        attackEnd = flipper!!.attack.attack_end_button

        loadText = flipper!!.using_ai.text_communication_load
        progress = flipper!!.using_ai.progress_communication_load

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        statusChange = StatusChange()
        mainActivity = (activity as BattleActivity)

        attack!!.setOnClickListener {
            effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
            effectBgm!!.play("other_button")
            attack(0)
            statusChange!!.ButtonNotEnabled(normalAttack!!)
            buttonChange(0)
        }

        move!!.setOnClickListener {
            effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
            effectBgm!!.play("other_button")
            move()
        }

        end!!.setOnClickListener {
            effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
            effectBgm!!.play("other_button")
            mainActivity!!.end()
        }

        normalMenu!!.setOnClickListener {
            effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
            effectBgm!!.play("other_button")
            normalMenu!!.isEnabled = false
            val newFragment = MenuDialogFragment.newInstance("クエスト名", mainActivity!!.changeImagesFragment!!.maxTurn, mainActivity!!.changeImagesFragment!!.turn, mainActivity!!.dataLog!!.log!!)
            newFragment.isCancelable = false
            newFragment.show(activity!!.fragmentManager, "dialog")
            Handler().postDelayed({
                normalMenu!!.isEnabled = true
            }, 750)
        }

        normalAttack!!.setOnLongClickListener{
            normalAttack!!.isEnabled = false
            val attackName = "通常攻撃"
            val attackType = "通常"
            val charType = mainActivity!!.parser!!.objects[mainActivity!!.numberList[mainActivity!!.changeImagesFragment!!.charturn]].type
            val effectName = "敵1体に倍率100%のダメージを与える"
            val newFragment = AttackAbilityDialogFragment.newInstance(attackName, attackType, charType, effectName)
            newFragment.isCancelable = false
            newFragment.show(activity!!.fragmentManager, "dialog")
            Handler().postDelayed({
                normalAttack!!.isEnabled = true
            }, 750)
            true    // 戻り値をtrueにするとOnClickイベントは発生しない
        }

        specialAttack!!.setOnLongClickListener {
            normalAttack!!.isEnabled = false
            val attackType = "特殊"
            val charType = mainActivity!!.parser!!.objects[mainActivity!!.numberList[mainActivity!!.changeImagesFragment!!.charturn]].type
            val specialAtackList = when (charType){
                getString(R.string.charType1) -> {
                    listOf("虚無","敵1体に必中で90の固定ダメージを与える")
                }
                getString(R.string.charType2) -> {
                    listOf("メテオストリーム","敵1体に倍率170%のダメージを与える")
                }
                getString(R.string.charType3) -> {
                    listOf("シールドアタック","敵1体に倍率100%のダメージを与え，次の自身のターンまで固定ダメージ軽減の効果を自身に付与する")
                }
                else -> {
                    listOf("しんそく","敵1体に倍率120%のダメージを与え，次の自身のターンまで回避率アップの効果を自身に付与する")
                }
            }
            val newFragment = AttackAbilityDialogFragment.newInstance(specialAtackList[0], attackType, charType, specialAtackList[1])
            newFragment.isCancelable = false
            newFragment.show(activity!!.fragmentManager, "dialog")
            Handler().postDelayed({
                normalAttack!!.isEnabled = true
            }, 750)
            true    // 戻り値をtrueにするとOnClickイベントは発生しない
        }

        normalAttack!!.setOnClickListener {
            effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
            effectBgm!!.play("other_button")
            attack(0)
        }

        specialAttack!!.setOnClickListener {
            effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
            effectBgm!!.play("other_button")
            attack(1)
        }

        attackMove!!.setOnClickListener {
            effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
            effectBgm!!.play("other_button")
            ResetMap(mainActivity!!.canmoveMap, mainActivity!!.attackMap, mainActivity!!.placeList).resetList()
            move()
            statusChange!!.ButtonNotEnabled(move!!)
            buttonChange(1)
        }

        attackEnd!!.setOnClickListener {
            effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
            effectBgm!!.play("other_button")
            buttonChange(0)
            mainActivity!!.end()
        }
    }

    /** 移動範囲を表示 */
    fun move(){
        if (attackFlag == 0 && moveFlag == 0) {
            statusChange!!.ButtonNotEnabled(move!!)
            if (attackFlag == 0) {
                statusChange!!.ButtonEnabled(normalAttack!!)
                if (mainActivity!!.unitList[mainActivity!!.changeImagesFragment!!.charturn].MP > 0) {
                    statusChange!!.ButtonEnabled(specialAttack!!)
                }
            }
            ResetMap(mainActivity!!.canmoveMap, mainActivity!!.attackMap, mainActivity!!.placeList).resetList()
            mainActivity!!.route = MoveSearch(
                    activity!!.applicationContext,
                    mainActivity!!.canmoveMap,
                    mainActivity!!.placeList,
                    mainActivity!!.charMap,
                    mainActivity!!.charPlaceList[mainActivity!!.changeImagesFragment!!.charturn]
            ).getMoveRange(
                    mainActivity!!.parser!!.objects[mainActivity!!.numberList[mainActivity!!.changeImagesFragment!!.charturn]].move.toInt()+1,
                    moveFlag
            )
            CharSearch(
                    activity!!.applicationContext,
                    mainActivity!!.charMap,
                    mainActivity!!.placeList,
                    mainActivity!!.charPlaceList[mainActivity!!.changeImagesFragment!!.charturn]
            ).plotPlayer()
        }
    }

    /** 攻撃範囲を表示 */
    fun attack(attackType: Int){
        if (attackFlag == 0) {
            mainActivity!!.attackType = attackType
            when (attackType) {
                0 -> {
                    statusChange!!.ButtonNotEnabled(normalAttack!!)
                    if (mainActivity!!.unitList[mainActivity!!.changeImagesFragment!!.charturn].MP > 0) {
                        statusChange!!.ButtonEnabled(specialAttack!!)
                    }
                }
                else -> {
                    statusChange!!.ButtonNotEnabled(specialAttack!!)
                    statusChange!!.ButtonEnabled(normalAttack!!)
                }
            }
            ResetMap(mainActivity!!.canmoveMap, mainActivity!!.attackMap, mainActivity!!.placeList).resetList()
            Attack(
                    mainActivity!!.imgList[mainActivity!!.changeImagesFragment!!.charturn]!!,
                    mainActivity!!.placeList,
                    mainActivity!!.charMap,
                    mainActivity!!.attackMap,
                    mainActivity!!.charPlaceList[mainActivity!!.changeImagesFragment!!.charturn],
                    mainActivity!!.unitList,
                    activity!!.applicationContext,
                    mainActivity!!.numberList[mainActivity!!.changeImagesFragment!!.charturn]
            ).getAttackRange(
                    mainActivity!!.parser!!.objects[mainActivity!!.numberList[mainActivity!!.changeImagesFragment!!.charturn]].attackRange.toInt() + 1,
                    mainActivity!!.attackType
            )
            CharSearch(
                    activity!!.applicationContext,
                    mainActivity!!.charMap,
                    mainActivity!!.placeList,
                    mainActivity!!.charPlaceList[mainActivity!!.changeImagesFragment!!.charturn]
            ).plotPlayer()
        }
    }

    fun load(){
        loadText!!.visibility = View.VISIBLE
        progress!!.visibility = View.VISIBLE
    }

    fun finishLoad(){
        loadText!!.visibility = View.INVISIBLE
        progress!!.visibility = View.INVISIBLE
    }

    /** ボタン切り替え */
    fun buttonChange(mode: Int){
        flipper!!.inAnimation = AnimationUtils.loadAnimation(activity!!.applicationContext, android.R.anim.slide_in_left)
        if (mode == 0){
            statusChange!!.ButtonNotEnabled(move!!)
            if (flipper!!.displayedChild != 1) flipper!!.displayedChild = 1
        } else if (mode == 1) {
             if (flipper!!.displayedChild != 0) flipper!!.displayedChild = 0
        } else {
            if (flipper!!.displayedChild != 2) flipper!!.displayedChild = 2
        }
    }

    /** 全ボタン有効化 */
    fun allButtonEnabled(){
        statusChange!!.ButtonEnabled(attack!!)
        statusChange!!.ButtonEnabled(move!!)
        statusChange!!.ButtonEnabled(end!!)
        statusChange!!.ButtonEnabled(normalMenu!!)
        statusChange!!.ButtonEnabled(normalAttack!!)
        statusChange!!.ButtonEnabled(specialAttack!!)
        statusChange!!.ButtonEnabled(attackMove!!)
        statusChange!!.ButtonEnabled(attackEnd!!)
    }

    /** 全ボタン無効化 */
    fun allButtonNotEnabled(){
        statusChange!!.ButtonNotEnabled(attack!!)
        statusChange!!.ButtonNotEnabled(move!!)
        statusChange!!.ButtonNotEnabled(end!!)
        statusChange!!.ButtonNotEnabled(normalMenu!!)
        statusChange!!.ButtonNotEnabled(normalAttack!!)
        statusChange!!.ButtonNotEnabled(specialAttack!!)
        statusChange!!.ButtonNotEnabled(attackMove!!)
        statusChange!!.ButtonNotEnabled(attackEnd!!)
    }

    /** 全ての変数を初期化 */
    fun allInit(){
        attack!!.setOnClickListener(null)
        attack!!.setBackgroundResource(0)
        attack = null
        move!!.setOnClickListener(null)
        move!!.setBackgroundResource(0)
        move = null
        end!!.setOnClickListener(null)
        end!!.setBackgroundResource(0)
        end = null
        normalMenu!!.setOnClickListener(null)
        normalMenu!!.setBackgroundResource(0)
        normalMenu = null

        normalAttack!!.setOnClickListener(null)
        normalAttack!!.setBackgroundResource(0)
        normalAttack = null
        specialAttack!!.setOnClickListener(null)
        specialAttack!!.setBackgroundResource(0)
        specialAttack = null
        attackMove!!.setOnClickListener(null)
        attackMove!!.setBackgroundResource(0)
        attackMove = null
        attackEnd!!.setOnClickListener(null)
        attackEnd!!.setBackgroundResource(0)
        attackEnd = null
        flipper = null

        moveFlag = 0
        attackFlag = 0

        statusChange = null
        data = null
        am = null
        mVol = 0f
        ringVolume = 0f

        effectBgm!!.release()
    }

}
