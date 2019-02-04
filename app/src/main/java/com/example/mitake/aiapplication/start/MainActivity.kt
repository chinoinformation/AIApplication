package com.example.mitake.aiapplication.start

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.media.AudioAttributes
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.mitake.aiapplication.home.IntentActivity
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.bgm.MyService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import android.media.SoundPool
import com.example.mitake.aiapplication.bgm.EffectList
import java.util.concurrent.ScheduledFuture


class MainActivity : AppCompatActivity() {

    /** 点滅アニメーション */
    private val mHandler = Handler()
    private var mScheduledExecutor: ScheduledExecutorService? = null
    private var mLblMeasuring: TextView? = null
    private var task: ScheduledFuture<*>? = null
    private var animeFadeIn: ObjectAnimator? = null
    private var animeFadeOut: ObjectAnimator? = null
    private var animatorList: ArrayList<Animator>? = null
    private var animatorSet: AnimatorSet? = null

    /** メニューボタン */
    private var menuButton: Button? = null

    /** BGM再生 */
    private var bgmId: Int = 0
    private var bgmFlag: Int = 0
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null

    /** スクリーンタッチ可能かどうかの判定 */
    private var canTouchScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         * メニューボタン
         * メニュー画面に遷移するリスナーを設定
         */
        menuButton = findViewById(R.id.menu)
        menuButton!!.setOnClickListener {
            effectBgm!!.play("other_button")
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }

        // 文字の点滅
        startMeasure()

    }

    /** 点滅アニメーション */
    private fun startMeasure() {
        /**
         * 点滅させたいView
         * TextViewじゃなくてもよい.
         */
        mLblMeasuring = findViewById(R.id.load_text)

        /**
         * 第一引数: 繰り返し実行したい処理
         * 第二引数: 指定時間後に第一引数の処理を開始
         * 第三引数: 第一引数の処理完了後、指定時間後に再実行
         * 第四引数: 第二、第三引数の単位
         *
         * new Runnable（無名オブジェクト）をすぐに（0秒後に）実行し、完了後1700ミリ秒ごとに繰り返す.
         * （ただしアニメーションの完了からではない。Handler#postが即時実行だから？？）
         */
        mScheduledExecutor = Executors.newScheduledThreadPool(2)
        task = mScheduledExecutor!!.scheduleWithFixedDelay(object : Runnable {
            @SuppressLint("ObsoleteSdkInt")
            override fun run() {
                mHandler.post {
                    mLblMeasuring!!.visibility = View.VISIBLE
                    // HONEYCOMBより前のAndroid SDKがProperty Animation非対応のため
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        animateAlpha()
                    }
                }
            }


            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            private fun animateAlpha() {
                // 実行するAnimatorのリスト
                animatorList = ArrayList()
                // alpha値を0から1へ1000ミリ秒かけて変化させる.
                animeFadeIn = ObjectAnimator.ofFloat(mLblMeasuring!!, "alpha", 0f, 1f)
                animeFadeIn!!.duration = 1000
                // alpha値を1から0へ600ミリ秒かけて変化させる.
                animeFadeOut = ObjectAnimator.ofFloat(mLblMeasuring!!, "alpha", 1f, 0f)
                animeFadeOut!!.duration = 600
                // 実行対象Animatorリストに追加
                animatorList!!.add(animeFadeIn!!)
                animatorList!!.add(animeFadeOut!!)
                animatorSet = AnimatorSet()
                // リストの順番に実行
                animatorSet!!.playSequentially(animatorList)
                // アニメーション終了した時の処理
                var canceled = false
                animatorSet!!.addListener(object : Animator.AnimatorListener {
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
                            animatorSet = null
                            animatorList = null
                            animeFadeIn = null
                            animeFadeOut = null
                        }
                    }
                })

                animatorSet!!.start()
            }
        }, 0, 1700, TimeUnit.MILLISECONDS)

    }

    /**
     * 画面タッチイベント
     * 初期設定: オープニングイベント画面に遷移
     * それ以外: ホーム画面に遷移
    */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP && canTouchScreen){
            canTouchScreen = false
            effectBgm!!.play("start")
            val intent = Intent(this, IntentActivity::class.java)
            intent.putExtra("Name", "Main")
            intent.putExtra("musicId", bgmId)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
        return super.onTouchEvent(event)
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
            effectBgm!!.getList("start")
            effectBgm!!.getList("other_button")
        } else {
            intent.putExtra("flag", 2)
            startService(intent)
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isFinishing) {
            val intent = Intent(applicationContext, MyService::class.java)
            intent.putExtra("id", bgmId)
            intent.putExtra("flag", 1)
            startService(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(applicationContext, MyService::class.java))
        menuButton!!.setBackgroundResource(0)
        menuButton!!.setOnClickListener(null)
        menuButton = null
        task!!.cancel(true)
        mLblMeasuring!!.setBackgroundResource(0)
        task = null
        mScheduledExecutor = null
        Handler().postDelayed({
            effectBgm!!.release()
        }, 2000)
    }

    override fun onBackPressed() {}
}
