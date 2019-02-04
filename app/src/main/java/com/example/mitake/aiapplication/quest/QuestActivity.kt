package com.example.mitake.aiapplication.quest

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.SoundPool
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.bgm.EffectList
import com.example.mitake.aiapplication.home.IntentActivity
import com.example.mitake.aiapplication.bgm.MyService


class QuestActivity : AppCompatActivity() {
    /** アイコン */
    private var homeIcon: ImageButton? = null
    private var mainQuestIcon: ImageButton? = null
    private var tutorialIcon: ImageButton? = null
    private var dojoIcon: ImageButton? = null
    private var pvpIcon: ImageButton? = null

    /** スクロールアニメーション */
    private var scrollBackground: HorizontalScrollView? = null
    private var mapStart: ImageView? = null
    private var mapEnd: ImageView? = null
    private var set: AnimatorSet? = null
    private var objectAnimator: ObjectAnimator? = null
    private var canTouch: Boolean = true

    /** BGM再生 */
    private var bgmId: Int = 0
    private var bgmFlag = 0
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null

    @SuppressLint("ResourceType", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quest)

        // ホームボタン
        // ホーム画面に遷移するリスナーを設定
        homeIcon = findViewById(R.id.background_quest_home)
        homeIcon!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        homeIcon!!.setOnClickListener {
            effectBgm!!.play("button")
            homeIcon!!.setOnClickListener(null)
            val intent = Intent(this, IntentActivity::class.java)
            intent.putExtra("Name", "Home")
            intent.putExtra("musicId", bgmId)
            startActivity(intent)
            finish()
        }

        // メインクエストボタン
        // メインクエスト画面に遷移するリスナーを設定
        mainQuestIcon = findViewById(R.id.background_quest_battle)
        mainQuestIcon!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        mainQuestIcon!!.setOnClickListener {
            effectBgm!!.play("button")
            mainQuestIcon!!.setOnClickListener(null)
            val intent = Intent(this, IntentActivity::class.java)
            intent.putExtra("Name", "MainQuest")
            intent.putExtra("musicId", bgmId)
            startActivity(intent)
            finish()
        }

        // チュートリアルボタン
        // チュートリアル画面に遷移するリスナーを設定
        tutorialIcon = findViewById(R.id.background_quest_tutorial)
        tutorialIcon!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        tutorialIcon!!.setOnClickListener {
            effectBgm!!.play("button")
            tutorialIcon!!.setOnClickListener(null)
            val intent = Intent(this, IntentActivity::class.java)
            intent.putExtra("Name", "TutorialQuest")
            intent.putExtra("musicId", bgmId)
            startActivity(intent)
            finish()
        }

        // 道場ボタン
        // 道場画面に遷移するリスナーを設定
        dojoIcon = findViewById(R.id.background_quest_dojo)
        dojoIcon!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        dojoIcon!!.setOnClickListener {
            effectBgm!!.play("button")
            dojoIcon!!.setOnClickListener(null)
            val intent = Intent(this, IntentActivity::class.java)
            intent.putExtra("Name", "Practice")
            intent.putExtra("musicId", bgmId)
            startActivity(intent)
            finish()
        }

        // 対人戦は後で実装
        pvpIcon = findViewById(R.id.background_quest_pvp)
        pvpIcon!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        pvpIcon!!.isEnabled = false
        pvpIcon!!.setColorFilter(Color.parseColor("#aa808080"))
        val pvpText: TextView = findViewById(R.id.text_quest_pvp)
        pvpText.setTextColor(Color.parseColor("#808080"))

        /**
         * スクロールアニメーション
         * mapStart: 画面左上に矢印を表示
         * mapEnd: 画面右上に矢印を表示
         * scrollBackground: スクロールビュー
         * (1) 画面左端までスクロール表示された場合 -> 左矢印を非表示
         * (2) 画面右端までスクロール表示された場合 -> 右矢印を非表示
         * (3) それ以外 -> 左矢印・右矢印両方表示
         */
        scrollBackground = findViewById(R.id.background_quest)
        scrollBackground!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        mapStart = findViewById(R.id.map_start)
        mapStart!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        Glide.with(applicationContext).load(R.drawable.map_start).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(mapStart!!)
        mapStart!!.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.animator.map_start_anim))
        mapStart!!.alpha = 0f
        mapStart!!.setOnClickListener { mapScroll(mapStart!!, 0) }
        mapEnd = findViewById(R.id.map_end)
        mapEnd!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        Glide.with(applicationContext).load(R.drawable.map_end).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(mapEnd!!)
        mapEnd!!.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.animator.map_end_anim))
        mapEnd!!.setOnClickListener { mapScroll(mapEnd!!, 1) }
        scrollBackground!!.viewTreeObserver
                .addOnScrollChangedListener({
                    if (scrollBackground!!.getChildAt(0).right == scrollBackground!!.width + scrollBackground!!.scrollX) {
                        mapEnd!!.alpha = 0f
                    } else {
                        if (scrollBackground!!.scrollX == 0) {
                            mapStart!!.alpha = 0f
                        } else {
                            if (mapStart!!.alpha != 1f) mapStart!!.alpha = 1f
                            if (mapEnd!!.alpha != 1f) mapEnd!!.alpha = 1f
                        }
                    }
                })
        scrollBackground!!.setOnTouchListener { _, _ ->
            when (canTouch) {
                true -> return@setOnTouchListener false
                else -> return@setOnTouchListener true
            }
        }

    }

    /**
     * マップスクロールアニメーション関数
     * mode == 0: 画面左端まで強制スクロール
     * mode == 1: 画面右端まで強制スクロール
     */
    private fun mapScroll(img: ImageView, mode: Int){
        if (img.alpha == 1f){
            canTouch = false
            if (mode == 0){
                mapScrollAnimation(scrollBackground!!, scrollBackground!!.scrollX, 0)
            } else {
                mapScrollAnimation(scrollBackground!!, scrollBackground!!.scrollX, scrollBackground!!.getChildAt(0).right - scrollBackground!!.width)
            }
        }
    }

    /**
     * マップスクロールアニメーション関数詳細
     * canTouchの値によって画面タッチの設定を変更させる
     */
    private fun mapScrollAnimation(scrollView: HorizontalScrollView, preX: Int, X: Int){
        set = AnimatorSet()
        set!!.duration = 800
        objectAnimator = ObjectAnimator.ofInt(scrollView, "scrollX", preX, X)
        set!!.play(objectAnimator!!)
        set!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                set = null
                objectAnimator = null
                canTouch = true
            }
        })
        set!!.start()
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
            effectBgm!!.getList("button")
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

        homeIcon!!.setOnClickListener(null)
        homeIcon!!.setImageResource(0)
        homeIcon!!.setImageDrawable(null)
        mainQuestIcon!!.setOnClickListener(null)
        mainQuestIcon!!.setImageResource(0)
        mainQuestIcon!!.setImageDrawable(null)
        tutorialIcon!!.setOnClickListener(null)
        tutorialIcon!!.setImageResource(0)
        tutorialIcon!!.setImageDrawable(null)
        dojoIcon!!.setOnClickListener(null)
        dojoIcon!!.setImageResource(0)
        dojoIcon!!.setImageDrawable(null)
        pvpIcon!!.setOnClickListener(null)
        pvpIcon!!.setImageResource(0)
        pvpIcon!!.setImageDrawable(null)
        homeIcon = null
        mainQuestIcon = null
        tutorialIcon = null
        dojoIcon = null
        pvpIcon = null

        scrollBackground!!.setBackgroundResource(0)
        scrollBackground = null
        mapStart!!.setOnClickListener(null)
        mapStart!!.setImageResource(0)
        mapStart!!.setImageDrawable(null)
        mapStart = null
        mapEnd!!.setOnClickListener(null)
        mapEnd!!.setImageResource(0)
        mapEnd!!.setImageDrawable(null)
        mapEnd = null

        set = null
        objectAnimator = null

        effectBgm!!.release()
        Glide.get(this).clearMemory()
    }

    override fun onBackPressed() {}
}
