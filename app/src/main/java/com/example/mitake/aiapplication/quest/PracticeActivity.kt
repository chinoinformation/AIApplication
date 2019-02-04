package com.example.mitake.aiapplication.quest


import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.bgm.EffectList
import com.example.mitake.aiapplication.bgm.MyService
import com.example.mitake.aiapplication.custom_layout.quest.DetailAIDialogFragment
import com.example.mitake.aiapplication.home.IntentActivity

import java.util.*

class PracticeActivity : AppCompatActivity() {

    /**
     * mainChar: メインキャラクター画像
     * numberPickerChar: キャラ数を選ぶビュー
     * numberPickerTurn: ターン数を選ぶビュー
     * radioGroupType: AIの種類を選ぶビュー
     * mainCharSpeech: メインキャラクターのセリフを制御する変数
     * speechList: メインキャラクターのセリフを格納するリスト
     * data: BattleActivityに送るデータ
    */
    private var mainChar: ImageView? = null
    private var numberPickerChar: NumberPicker? = null
    private var numberPickerTurn: NumberPicker? = null
    private var radioGroupType: RadioGroup? = null
    private var mainCharSpeech: TextView? = null
    private var speechList: MutableList<String> = mutableListOf()
    private var data: Array<Any> = arrayOf(4, 10, "A I")

    /** ボタン */
    private var backHome: Button? = null
    private var backQuest: Button? = null
    private var startQuest: Button? = null
    private var detailAI: Button? = null

    /** 画面初期化変数 */
    private var isStart = false

    /** BGM再生 */
    private var bgmId: Int = 0
    private var bgmFlag = 0
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practice)

        // ホームボタン
        // ホーム画面に遷移するリスナーを設定
        backHome = findViewById(R.id.back_home)
        backHome!!.setOnClickListener {
            effectBgm!!.play("button")
            backHome!!.setOnClickListener(null)
            val intent = Intent(this, IntentActivity::class.java)
            intent.putExtra("Name", "Home")
            intent.putExtra("musicId", bgmId)
            startActivity(intent)
            finish()
        }

        // ワールドマップボタン
        // ワールドマップ画面に遷移するリスナーを設定
        backQuest = findViewById(R.id.back_world)
        backQuest!!.setOnClickListener {
            effectBgm!!.play("button")
            backQuest!!.setOnClickListener(null)
            val intent = Intent(this, IntentActivity::class.java)
            intent.putExtra("Name", "Quest")
            intent.putExtra("musicId", bgmId)
            startActivity(intent)
            finish()
        }

        // 戦闘開始ボタン
        // 戦闘画面に遷移するリスナーを設定
        startQuest = findViewById(R.id.start_quest)
        startQuest!!.setOnClickListener {
            effectBgm!!.play("go_battle")
            startQuest!!.setOnClickListener(null)
            getData()
            val intent = Intent(this, IntentActivity::class.java)
            intent.putExtra("Name", "Battle")
            intent.putExtra("musicId", bgmId)
            startActivity(intent)
            finish()
            overridePendingTransition(0, 0)
        }

        // 戦闘AI詳細ボタン
        // 戦闘AIの詳細を表示する
        detailAI = findViewById(R.id.detail_AI)
        detailAI!!.setOnClickListener {
            effectBgm!!.play("other_button")
            detailAI!!.isEnabled = false
            val newFragment = DetailAIDialogFragment.newInstance()
            newFragment.isCancelable = false
            newFragment.show(supportFragmentManager, "dialog")
            Handler().postDelayed({
                detailAI!!.isEnabled = true
            }, 750)
        }

        // メインキャラ
        mainChar = findViewById(R.id.main_char)
        // メインキャラのセリフ変化
        mainCharSpeech = findViewById(R.id.main_char_speech)
        mainCharSpeech!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        mainCharSpeech!!.alpha = 0f
        speechList = mutableListOf(getString(R.string.char_speech1), getString(R.string.char_speech2), getString(R.string.char_speech3), getString(R.string.char_speech4))
        mainChar!!.setOnClickListener { mainCharSpeech!!.text = speechList[Math.abs(Random().nextInt())%4] }

        // 初期化
        init()
    }

    @SuppressLint("ResourceType")
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!isStart){
            // 初期設定
            isStart = true
            mainCharSpeech!!.alpha = 1f

            val mainCharPosition: ImageView = findViewById(R.id.char_position)
            val location = IntArray(2)
            mainCharPosition.getLocationOnScreen(location)
            charAnim(mainChar!!, 0f, location[0].toFloat()-80f, 0f, location[1].toFloat(),0)
            // メインキャラのアニメーション
            Glide.with(applicationContext).load(R.drawable.app_souta_touka).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(mainChar!!)
            mainChar!!.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.animator.updown))
        }
    }

    /**
     * キャラのアニメーション
     * 位置を確定
     */
    fun charAnim(img: ImageView, saX: Float, moveX: Float, saY: Float, moveY: Float, duration: Long){
        val set = AnimatorSet()
        val setDuration = duration
        set.duration = setDuration
        val objectAnimatorX = ObjectAnimator.ofFloat(img, "TranslationX", saX, moveX)
        val objectAnimatorY = ObjectAnimator.ofFloat(img, "TranslationY", saY, moveY)

        if (Math.abs(saX - moveX) == 0f) {
            set.play(objectAnimatorX).after(objectAnimatorY)
        }else{
            set.play(objectAnimatorY).after(objectAnimatorX)
        }

        set.start()
    }

    /** 初期化 */
    private fun init(){
        numberPickerChar = findViewById(R.id.numberPicker_enemy)
        numberPickerTurn = findViewById(R.id.numberPicker_turn)
        radioGroupType = findViewById(R.id.enemy_type)

        numberPickerChar!!.minValue = 1
        numberPickerChar!!.maxValue = 4
        numberPickerChar!!.wrapSelectorWheel = true
        numberPickerChar!!.value = 4

        numberPickerTurn!!.minValue = 1
        numberPickerTurn!!.maxValue = 10
        numberPickerTurn!!.wrapSelectorWheel = true
        numberPickerTurn!!.value = 10
    }

    /** データ取得 */
    private fun getData(){
        data[0] = numberPickerChar!!.value
        data[1] = numberPickerTurn!!.value
        val checkedRadioButton = findViewById<RadioButton>(radioGroupType!!.checkedRadioButtonId)
        data[2] = checkedRadioButton.text
    }

    override fun onResume() {
        super.onResume()
        // プレイヤーの処理
        bgmId = R.raw.bgm_world
        val intent = Intent(applicationContext, MyService::class.java)
        intent.putExtra("id", bgmId)
        if (bgmFlag == 0) {
            //プレイヤーの初期化
            intent.putExtra("flag", 0)
            startService(intent)
            bgmFlag = 1

            // SoundPool の設定
            Thread.sleep(1000)
            audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            soundPool = SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setMaxStreams(2)
                    .build()
            effectBgm = EffectList(applicationContext, soundPool)
            effectBgm!!.getList("go_battle")
            effectBgm!!.getList("button")
            effectBgm!!.getList("other_button")
        } else {
            intent.putExtra("flag", 2)
            startService(intent)
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isFinishing) {
            val intent = Intent(applicationContext, MyService::class.java)
            intent.putExtra("id", bgmId)
            intent.putExtra("flag", 1)
            startService(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainChar!!.setImageResource(0)
        mainChar!!.setImageDrawable(null)
        mainChar!!.setOnClickListener(null)
        mainChar = null

        numberPickerChar!!.setBackgroundResource(0)
        numberPickerChar = null
        numberPickerTurn!!.setBackgroundResource(0)
        numberPickerTurn = null
        radioGroupType!!.setBackgroundResource(0)
        radioGroupType = null

        backHome!!.setOnClickListener(null)
        backHome!!.setBackgroundResource(0)
        backQuest!!.setOnClickListener(null)
        backQuest!!.setBackgroundResource(0)
        startQuest!!.setOnClickListener(null)
        startQuest!!.setBackgroundResource(0)
        detailAI!!.setOnClickListener(null)
        detailAI!!.setBackgroundResource(0)
        backHome = null
        backQuest = null
        startQuest = null
        detailAI = null

        Glide.get(this).clearMemory()
        Handler().postDelayed({
            effectBgm!!.release()
        }, 2000)
    }

    override fun onBackPressed() {}
}
