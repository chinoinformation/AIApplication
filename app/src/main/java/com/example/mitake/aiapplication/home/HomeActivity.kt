package com.example.mitake.aiapplication.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import com.example.mitake.aiapplication.R
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.example.mitake.aiapplication.bgm.EffectList
import com.example.mitake.aiapplication.bgm.MyService
import com.example.mitake.aiapplication.data.DataManagement

class HomeActivity : AppCompatActivity(){

    /**
     * mainChar: メインキャラクターの画像
     * quest: クエスト画像
     * questText: クエスト画像に重ねるテキスト
     */
    private var mainChar: ImageView? = null
    private var mainCharIndex: Int = 1
    private var quest: ImageView? = null
    private var questText: TextView? = null

    /** BGM再生 */
    private var bgmId = R.raw.bgm_home
    private var bgmFlag = 0
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null
    private var am: AudioManager? = null
    private var mVol: Float = 0f
    private var ringVolume: Float = 0f

    /** プリファレンス */
    private var data: DataManagement? = null

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // ボタンのイラスト変更
        val mainButton = MainButtonFragment()
        val t = supportFragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putInt("musicId", bgmId)
        bundle.putString("activity", "Home")
        mainButton.arguments = bundle
        t.add(R.id.home_main_button, mainButton, "main_button")
        t.commit()

        // ワールドマップに画面遷移するリスナーを設定
        questText = findViewById(R.id.quest_text)
        questText!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        questText!!.setOnClickListener {
            effectBgm!!.play("button")
            questText!!.setOnClickListener(null)
            val intent = Intent(this, IntentActivity::class.java)
            intent.putExtra("Name", "Quest")
            intent.putExtra("musicId", bgmId)
            startActivity(intent)
            finish()
        }

        // メインキャラクター
        mainChar = findViewById(R.id.main_char)
        mainChar!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        Glide.with(applicationContext).load(R.drawable.ririi_1).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(mainChar!!)
        mainChar!!.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.animator.updown))
        mainChar!!.setOnClickListener { changeMainChar() }

        // クエスト画像のアニメーション
        quest = findViewById(R.id.quest_image)
        quest!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        Glide.with(applicationContext).load(R.drawable.quest_anim).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(quest!!)
    }

    /** メインキャラクターの画像変更 */
    private fun changeMainChar(){
        mainCharIndex = if (mainCharIndex == 1) 2 else 1
        Glide.with(applicationContext).load(resources.getIdentifier("ririi_$mainCharIndex", "drawable", packageName)).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(mainChar!!)
    }

    /** 音量設定 */
    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {

        if (event.keyCode === KeyEvent.KEYCODE_VOLUME_UP) {
            // 現在の音量を取得する
            ringVolume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
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
        bgmId = R.raw.bgm_home
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
        effectBgm!!.getList("button")
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
        mainChar!!.setOnClickListener(null)
        mainChar!!.setImageResource(0)
        mainChar!!.setImageDrawable(null)
        mainChar = null
        mainCharIndex = 0
        quest!!.setImageResource(0)
        quest!!.setImageDrawable(null)
        quest = null
        questText!!.setOnClickListener(null)
        questText!!.setBackgroundResource(0)
        questText = null

        effectBgm!!.release()
        data = null
        am = null
        mVol = 0f
        ringVolume = 0f
        Glide.get(this).clearMemory()
    }

    override fun onBackPressed() {}
}
