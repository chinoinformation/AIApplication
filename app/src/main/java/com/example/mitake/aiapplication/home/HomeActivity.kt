package com.example.mitake.aiapplication.home

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
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

class HomeActivity : AppCompatActivity(){

    /**
     * mainChar: メインキャラクターの画像
     * quest: クエスト画像
     * questText: クエスト画像に重ねるテキスト
     */
    private var mainChar: ImageView? = null
    private var quest: ImageView? = null
    private var questText: TextView? = null

    /** BGM再生 */
    private var bgmId = R.raw.bgm_home
    private var bgmFlag = 0
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null

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
        Glide.with(applicationContext).load(R.drawable.app_ririi2).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(mainChar!!)
        mainChar!!.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.animator.updown))

        // クエスト画像のアニメーション
        quest = findViewById(R.id.quest_image)
        quest!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        Glide.with(applicationContext).load(R.drawable.quest_anim).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(quest!!)
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
        } else {
            intent.putExtra("flag", 2)
            startService(intent)
        }

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
        mainChar!!.setImageResource(0)
        mainChar!!.setImageDrawable(null)
        mainChar = null
        quest!!.setImageResource(0)
        quest!!.setImageDrawable(null)
        quest = null
        questText!!.setOnClickListener(null)
        questText!!.setBackgroundResource(0)
        questText = null

        effectBgm!!.release()
        Glide.get(this).clearMemory()
    }

    override fun onBackPressed() {}
}
