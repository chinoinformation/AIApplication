package com.example.mitake.aiapplication.battle

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.bgm.EffectList
import com.example.mitake.aiapplication.bgm.MyService
import com.example.mitake.aiapplication.data.DataManagement
import com.example.mitake.aiapplication.home.IntentActivity
import kotlinx.android.synthetic.main.activity_battle_finish.view.*

class BattleFinishActivity : AppCompatActivity() {

    /** BGM再生 */
    private var bgmId = R.raw.bgm_battle
    private var bgmFlag = 0
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null
    private var am: AudioManager? = null
    private var mVol: Float = 0f
    private var ringVolume: Float = 0f

    private val mHandler = Handler()
    private var hpLayout: LinearLayout? = null
    private var charLayout: LinearLayout? = null
    private var winnerText: TextView? = null
    private var nextButton: Button? = null

    /** プリファレンス */
    private var data: DataManagement? = null

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battle_finish)

        winnerText= findViewById(R.id.winner)
        nextButton = findViewById(R.id.next)
        winnerText!!.alpha = 0f
        nextButton!!.alpha = 0f
        nextButton!!.isEnabled = false

        var playerHP1 = intent.getIntExtra("player1HP", 0).toString()
        when {
            playerHP1.length == 1 -> playerHP1 = "    $playerHP1"
            playerHP1.length == 2 -> playerHP1 = "  $playerHP1"
        }
        var playerHP2 = intent.getIntExtra("player2HP", 0).toString()
        when {
            playerHP2.length == 1 -> playerHP2 = "    $playerHP2"
            playerHP2.length == 2 -> playerHP2 = "  $playerHP2"
        }
        hpLayout = findViewById(R.id.HP_layout)
        hpLayout!!.player1_HP.text = playerHP1
        hpLayout!!.player2_HP.text = playerHP2

        val playerCharNum1 = intent.getIntExtra("playerCharNum1", 0)
        val playerCharNum2 = intent.getIntExtra("playerCharNum2", 0)
        charLayout = findViewById(R.id.char_layout)
        charLayout!!.player1_char.text = playerCharNum1.toString()
        charLayout!!.player2_char.text = playerCharNum2.toString()

        winnerText!!.text = when {
            playerCharNum1 > playerCharNum2 -> getString(R.string.winner)
            playerCharNum1 < playerCharNum2 -> getString(R.string.loser)
            else -> when {
                playerHP1 > playerHP2 -> getString(R.string.winner)
                playerHP1 < playerHP2 -> getString(R.string.loser)
                else -> "ドロー"
            }
        }

        nextButton!!.setOnClickListener {
            effectBgm!!.play("other_button")
            nextButton!!.setOnClickListener(null)
            val intent = Intent(this, IntentActivity::class.java)
            intent.putExtra("Name", "Home")
            intent.putExtra("musicId", bgmId)
            startActivity(intent)
            finish()
        }

        slide(hpLayout!!, R.animator.battle_finish_init)
        slide(charLayout!!, R.animator.battle_finish_init)

        resultAnimation()
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

    @SuppressLint("ResourceType")
    private fun slide(layout: LinearLayout, slideId: Int){
        val slide = AnimationUtils.loadAnimation(applicationContext, slideId)
        layout.startAnimation(slide)
    }

    private fun resultAnimation(){
        mHandler.postDelayed({
            slide(charLayout!!, R.animator.slide_in)
            mHandler.postDelayed({
                slide(hpLayout!!, R.animator.slide_in)
                mHandler.postDelayed({
                    winnerPlayerAnimation(winnerText!!)
                }, 1500)
            }, 1000)
        }, 3000)
    }

    @SuppressLint("ResourceType")
    private fun winnerPlayerAnimation(winnerText: TextView){
        val objectAnimator = ObjectAnimator.ofFloat(winnerText, "alpha", 0f, 1f)
        objectAnimator.duration = 1500

        // アニメーション終了した時の処理
        var canceled = false
        objectAnimator.addListener(object : Animator.AnimatorListener {
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
                    // NEXTボタンを有効化
                    nextButton!!.alpha = 1f
                    nextButton!!.isEnabled = true
                }
            }
        })

        objectAnimator.start()
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

            // AudioManagerを取得する
            am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            // 最大音量値を取得
            mVol = am!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
            // 現在の音量を取得する
            ringVolume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
            // プリファレンスの呼び出し
            data = DataManagement(this)
            effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
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

        nextButton!!.setOnClickListener(null)
        nextButton!!.setBackgroundResource(0)
        nextButton = null
        hpLayout!!.setBackgroundResource(0)
        hpLayout = null
        charLayout!!.setBackgroundResource(0)
        charLayout = null
        winnerText!!.setBackgroundResource(0)
        winnerText = null

        data = null
        am = null
        mVol = 0f
        ringVolume = 0f
        Handler().postDelayed({
            effectBgm!!.release()
        }, 2000)
    }

    override fun onBackPressed() {}

}
