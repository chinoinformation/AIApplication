package com.example.mitake.aiapplication.start

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.view.KeyEvent
import android.widget.Button
import android.widget.ImageButton
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.bgm.EffectList
import com.example.mitake.aiapplication.bgm.MyService
import com.example.mitake.aiapplication.data.DataManagement
import com.example.mitake.aiapplication.home.IntentActivity
import com.example.mitake.aiapplication.home.SiteDialogFragment

class MenuActivity : AppCompatActivity() {

    /** ボタン */
    private var resetButton: Button? = null
    private var siteButton: Button? = null
    private var returnButton: ImageButton? = null

    /** BGM再生 */
    private var bgmId: Int = 0
    private var bgmFlag: Int = 0
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null
    private var am: AudioManager? = null
    private var mVol: Float = 0f
    private var ringVolume: Float = 0f

    /** プリファレンス */
    private var data: DataManagement? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        /**
         * ×ボタン
         * メニュー画面を閉じるリスナーを設定
         */
        returnButton = findViewById(R.id.attack_move_button)
        returnButton!!.setOnClickListener {
            returnButton!!.setOnClickListener(null)
            effectBgm!!.play("other_button")
            finish()
            overridePendingTransition(R.animator.act_close_enter_anim, R.animator.act_close_exit_anim)
        }

        /**
         * リセットボタン
         * プリファレンスを初期化
         */
        resetButton = findViewById(R.id.data_reset)
        resetButton!!.setOnClickListener {
            effectBgm!!.play("other_button")
            resetButton!!.isEnabled = false
            val builder = AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
            builder.setTitle("最終確認")
                    .setMessage("アプリ内のデータをリセットします．よろしいですか．")
                    .setPositiveButton("キャンセル"){ _, _ ->
                        effectBgm!!.play("other_button")
                    }
                    .setCancelable(false)
                    .setNegativeButton("OK"){ _, _ ->
                        // OK button pressed
                        effectBgm!!.play("other_button")
                        data!!.clearData()
                    }
            val dialog = builder.create()
            dialog.show()
            dialog.getButton(Dialog.BUTTON_POSITIVE).isSoundEffectsEnabled = false
            dialog.getButton(Dialog.BUTTON_NEGATIVE).isSoundEffectsEnabled = false
            Handler().postDelayed({
                resetButton!!.isEnabled = true
            }, 750)
        }

        // ライセンス画面を表示
        siteButton = findViewById(R.id.site)
        siteButton!!.setOnClickListener {
            effectBgm!!.play("other_button")
            siteButton!!.isEnabled = false
            val newFragment = SiteDialogFragment.newInstance()
            newFragment.isCancelable = false
            newFragment.show(fragmentManager, "dialog")
            Handler().postDelayed({
                siteButton!!.isEnabled = true
            }, 750)
        }

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
        bgmId = R.raw.bgm_start
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

        resetButton!!.setOnClickListener(null)
        resetButton!!.setBackgroundResource(0)
        resetButton = null
        returnButton!!.setOnClickListener(null)
        returnButton!!.setImageResource(0)
        returnButton!!.setImageDrawable(null)
        returnButton = null
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
