package com.example.mitake.aiapplication.tutorial

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
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.bgm.EffectList
import com.example.mitake.aiapplication.bgm.MyService
import com.example.mitake.aiapplication.custom_layout.start.CharByCharTextView
import com.example.mitake.aiapplication.custom_layout.start.TextString
import com.example.mitake.aiapplication.home.IntentActivity
import java.util.*

class TutorialStoryActivity : AppCompatActivity() {
    /** BGM再生 */
    private var bgmId: Int = R.raw.bgm_opening_story
    private var bgmFlag = 0
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null

    /**
     * initialNum: 初期設定変数
     * finish: ストーリーが終わったかどうかの判定変数
     * mCustomTextView: 一文字ずつ表示するカスタムテキストビュー
     */
    private var initialNum = 0
    private var finish = false
    private var mCustomTextView: CharByCharTextView? = null
    private val texts = Arrays.asList(
            TextString("「坊主、人形たちに搭載されている人工知能について教えてやる」","人形屋"),
            TextString("「特別なものでニューラルネットワークという人間の脳を模したものだ」","人形屋"),
            TextString("「まずニューラルネットワークについて詳しく説明する前にその起源であるパーセプトロンについて教えよう」","人形屋"),
            TextString("「パーセプトロンは神経細胞を模式的に再現したもので、いくつかの入力口から信号を受け取り、信号1つを出力するというシンプルなものだ」","人形屋"),
            TextString("「信号はどの入力口から入ってくるかによって重要度が異なる。それを学習させる訳だ。」","人形屋"),
            TextString("「ちょうどお前の詰まってるかどうかわからない脳みそでやってることと同じだな」","人形屋"),
            TextString("「もちろんパーセプトロン一つでは複雑な問題には対応できない」","人形屋"),
            TextString("「だが、人間の脳は神経細胞が複雑に繋がって構成されている。それと同じようにパーセプトロンを何重にも重ねたらどうなる？」","人形屋"),
            TextString("「パーセプトロンでは解けなかった難しい問題でも学習し、正しく回答できるようになる訳だ」","人形屋"),
            TextString("「これがニューラルネットワークだ。」","人形屋")

            )

    /**
     * talkerName: 現在話をしているキャラクターの名前を表示するテキストビュー
     * newStr: 現在流しているテキスト
     * endImage: テキストをすべて表示したときに表示する画像
     * yesButton: 「はい」ボタン
     * noButton: 「いいえ」ボタン
     */
    private var talkerName: TextView? = null
    private var newStr: String? = null
    private var endImage: ImageView? = null
    private var button: Button? = null
    private var yesButton: Button? = null
    private var noButton: Button? = null

    /**
     * isStoryStart: ストーリーが始まったかどうかの判定変数
     * talker1: 人形屋の画像
     * talker2: 主人公の画像
     * mainChar1: メインキャラクター1の画像
     * mainChar2: メインキャラクター2の画像
     * select: メインキャラクターを選ぶときに使用する変数
     * canTouch: 画面タッチが有効かどうか判定する変数
     */
    private var isStoryStart = false
    private var talker1: ImageView? = null
    private var talker2: ImageView? = null
    private var setumei: ImageView? = null
    private var mainChar1: ImageView? = null
    private var mainChar2: ImageView? = null
    private var select = true
    private var canTouch = false

    /** スレッドUI操作用ハンドラ  */
    private val mHandler = Handler()
    /** テキストオブジェクト  */
    private var updateText: Runnable? = null


    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial_story)

        // 人形屋・主人公の画像を設定
        talker1 = findViewById(R.id.talker_image1)
        talker1!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        talker1!!.alpha = 0f
        Glide.with(applicationContext).load(R.drawable.ningyoya).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(talker1!!)
        talker2 = findViewById(R.id.talker_image2)
        talker2!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        talker2!!.alpha = 0f
        Glide.with(applicationContext).load(R.drawable.aren_2).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(talker2!!)

        //説明画像設定
        setumei = findViewById(R.id.setumei_image)
        setumei!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        setumei!!.alpha = 0f
        Glide.with(applicationContext).load(R.drawable.aren_2).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(setumei!!)



        // テキストをすべて表示したときに表示する画像を設定
        endImage = findViewById(R.id.end)
        endImage!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        endImage!!.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.animator.text_updown))
        endImage!!.alpha = 0f

        // 一文字ずつ表示するカスタムテキストビューを設定
        mCustomTextView = findViewById(R.id.customTextView)
        mCustomTextView!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        // 現在話しているキャラクターの名前を表示するテキストビューを設定
        talkerName = findViewById(R.id.talker)
        talkerName!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        // 最初に流すテキストを設定
        newStr = texts[0].textwords

        // endImageを表示させる判定を行う
        updateText = Runnable {
            if (!finish && mCustomTextView!!.text == newStr && mCustomTextView!!.flag == 0){
                canTouch = true
                endImage!!.alpha = 1f
            }
            mHandler.removeCallbacks(updateText)
            mHandler.postDelayed(updateText, 100)
        }
        mHandler.postDelayed(updateText, 100)

        button = findViewById(R.id.backlogB)
        button!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        button!!.alpha = 0f
        button!!.setOnClickListener{
            if (finish){
                val intent = Intent(this, IntentActivity::class.java)
                intent.putExtra("Name", "Main")
                intent.putExtra("musicId", bgmId)
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()
            }
        }

        // 「はい」ボタンを設定
        yesButton = findViewById(R.id.yes)
        yesButton!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        // 「いいえ」ボタンを設定
        noButton = findViewById(R.id.no)
        noButton!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        // メインキャラクターの画像を設定
        mainChar1 = findViewById(R.id.main_char1)
        mainChar1!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        mainChar2 = findViewById(R.id.main_char2)
        mainChar2!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        Glide.with(applicationContext).load(R.drawable.app_ririi2).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(mainChar1!!)
        Glide.with(applicationContext).load(R.drawable.app_souta_touka).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(mainChar2!!)

        // 各種Viewの初期設定
        yesButton!!.alpha = 0f
        noButton!!.alpha = 0f
        yesButton!!.setOnClickListener(null)
        noButton!!.setOnClickListener(null)
        mainChar1!!.alpha = 0f
        mainChar2!!.alpha = 0f
    }

    @SuppressLint("ResourceType")
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!isStoryStart){
            // 初期設定
            isStoryStart = true
            charAnim(talker1!!, 0f, -2000f, 0, 0)
            charAnim(talker2!!, 0f, 2000f, 0, 0)

            //会話イベント開始
            Handler().postDelayed({
                val location = IntArray(2)
                talker1!!.getLocationOnScreen(location)
                talker1!!.alpha = 1f
                charAnim(talker1!!, location[0].toFloat(), 0f, 1000, 1)
            },1000)
        }
    }

    /**
     * タッチイベント
     * 基本，テキストを全表示させるアニメーションを行う.
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (select && canTouch) {
            if (ev!!.action == MotionEvent.ACTION_UP) {
                if (mCustomTextView!!.flag == 0) {
                    var index = 0
                    for (i in 0 until texts.size) {
                        if (texts[i].textwords == newStr) {
                            index = i + 1
                            break
                        }
                    }
                    if (index == 0 || index == texts.size-1 || index >= texts.size) {
                        button!!.alpha = 1f
                        index = texts.size - 1
                        button!!.text = "ホーム画面へ"
                        finish = true
                        canTouch = false
                    }

                    sayChar(talker1!!, talker2!!, texts[index].charactername)
                    if (index == 2) {
                        val location = IntArray(2)
                        talker2!!.getLocationOnScreen(location)
                        talker2!!.alpha = 1f
                        charAnim(talker2!!, location[0].toFloat(), 0f, 400, 0)
                        talker2!!.setColorFilter(Color.parseColor("#00ffffff"))
                    }
                    endImage!!.alpha = 0f
                    talkerName!!.text = texts[index].charactername
                    newStr = texts[index].textwords
                    mCustomTextView!!.setTargetText(newStr!!)
                    mCustomTextView!!.startCharByCharAnim()

                    when(index) {//
                        3 -> imageswitcher(1)
                        4 -> imageswitcher(2)
                        5 -> imageswitcher(3)
                        8 -> imageswitcher(5)
                        9 -> imageswitcher(6)
                        10 -> imageswitcher(7)
                        else -> setumei!!.alpha = 1f
                    }
/*
                    // メインキャラクターを選ぶイベント
                    if (newStr == "「見た目で性能は変わらないから好きなものを選べよ！！」") {
                        talker1!!.alpha = 0f
                        talker2!!.alpha = 0f
                        mainChar1!!.alpha = 1f
                        mainChar2!!.alpha = 1f
                        mainChar1!!.setOnClickListener {
                            newStr = "「本当にこいつでいいんだな？」"
                            mCustomTextView!!.setTargetText(newStr!!)
                            mCustomTextView!!.startCharByCharAnim()
                            selectButton(mainChar1!!, mainChar2!!)
                            selectMainChar(mainChar1!!)
                        }
                        mainChar2!!.setOnClickListener {
                            newStr = "「本当にこいつでいいんだな？」"
                            mCustomTextView!!.setTargetText(newStr!!)
                            mCustomTextView!!.startCharByCharAnim()
                            selectButton(mainChar1!!, mainChar2!!)
                            selectMainChar(mainChar2!!)
                        }
                        select = false
                    }
*/
                } else if (mCustomTextView!!.flag == 1) {
                    canTouch = false
                    mCustomTextView!!.flag = 2
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * 登場人物のアニメーション
     * 位置を確定
    */
    fun charAnim(img: ImageView, saX: Float, moveX: Float, duration: Long, mode: Int){
        val set = AnimatorSet()
        set.duration = duration
        val objectAnimatorX = ObjectAnimator.ofFloat(img, "TranslationX", saX, moveX)
        val objectAnimatorY = ObjectAnimator.ofFloat(img, "TranslationY", 0f,0f)

        if (Math.abs(saX - moveX) == 0f) {
            set.play(objectAnimatorX).after(objectAnimatorY)
        }else{
            set.play(objectAnimatorY).after(objectAnimatorX)
        }

        // アニメーション終了した時の処理
        set.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                // 会話イベント開始
                if (mode == 1){
                    talkerName!!.text = texts[0].charactername
                    mCustomTextView!!.setTargetText(texts[0].textwords)
                    sayChar(talker1!!, talker2!!, texts[0].charactername)
                    mCustomTextView!!.startCharByCharAnim()
                }
            }
        })
        set.start()
    }

    /** 現在話しているキャラクター以外を暗く表示させる関数 */
    private fun sayChar(img1: ImageView, img2: ImageView, char: String){
        when (char) {
            "人形屋" -> {
                img1.setColorFilter(Color.parseColor("#00ffffff"))
                img2.setColorFilter(Color.parseColor("#aa000000"))
            }
            "主人公" -> {
                img1.setColorFilter(Color.parseColor("#aa000000"))
                img2.setColorFilter(Color.parseColor("#00ffffff"))
            }
            "父"-> {
                img1.setColorFilter(Color.parseColor("#aa000000"))
                img2.setColorFilter(Color.parseColor("#aa000000"))
            }
            else -> {
                img1.setColorFilter(Color.parseColor("#00ffffff"))
                img2.setColorFilter(Color.parseColor("#00ffffff"))
            }
        }
    }

    /** 「はい」「いいえ」ボタンのリスナーを設定 */
/*
    private fun selectButton(img1: ImageView, img2: ImageView){
        yesButton!!.alpha = 1f
        noButton!!.alpha = 1f
        yesButton!!.setOnClickListener {
            yesButton!!.alpha = 0f
            noButton!!.alpha = 0f
            talker1!!.alpha = 1f
            talker2!!.alpha = 1f
            mainChar1!!.alpha = 0f
            mainChar2!!.alpha = 0f
            newStr = "「分かった。じゃあ今日からお前がこいつのマスターだ」"
            yesButton!!.setOnClickListener(null)
            noButton!!.setOnClickListener(null)
            mCustomTextView!!.setTargetText(newStr!!)
            mCustomTextView!!.startCharByCharAnim()
            select = true
            canTouch = true
        }
        noButton!!.setOnClickListener {
            yesButton!!.alpha = 0f
            noButton!!.alpha = 0f
            newStr = "「見た目で性能は変わらないから好きなものを選べよ！！」"
            mCustomTextView!!.setTargetText(newStr!!)
            mCustomTextView!!.startCharByCharAnim()
            img1.setColorFilter(Color.parseColor("#00ffffff"))
            img2.setColorFilter(Color.parseColor("#00ffffff"))
            newStr = "「本当にこいつでいいんだな？」"
        }
    }
 */
    /** タッチしたメインキャラクター以外のキャラクターを暗く表示させる関数 */
    private fun selectMainChar(img: ImageView){
        if (img == mainChar1!!){
            mainChar1!!.setColorFilter(Color.parseColor("#00ffffff"))
            mainChar2!!.setColorFilter(Color.parseColor("#aa000000"))
        } else {
            mainChar1!!.setColorFilter(Color.parseColor("#aa000000"))
            mainChar2!!.setColorFilter(Color.parseColor("#00ffffff"))
        }
    }

    private fun imageswitcher(imogenum: Int){//説明画像切替
        when(imogenum){
            1 -> Glide.with(applicationContext).load(R.drawable.perceptron1).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(setumei!!)
            2 -> Glide.with(applicationContext).load(R.drawable.perceptron2).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(setumei!!)
            3 -> Glide.with(applicationContext).load(R.drawable.perceptron3).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(setumei!!)
            4 -> Glide.with(applicationContext).load(R.drawable.neural1).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(setumei!!)
            5 -> Glide.with(applicationContext).load(R.drawable.neural2).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(setumei!!)
            6 -> Glide.with(applicationContext).load(R.drawable.neural3).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(setumei!!)
            7 -> Glide.with(applicationContext).load(R.drawable.neural4).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(setumei!!)
            else -> Glide.with(applicationContext).load(R.drawable.perceptron1).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(setumei!!)
        }
        setumei!!.alpha = 1f
    }

    override fun onResume(){
        super.onResume()
        if (initialNum != 0) {
            mHandler.postDelayed(updateText, 100)
            mCustomTextView!!.restartAnim()
        }
        initialNum = 1
        // プレイヤーの処理
        bgmId = R.raw.bgm_opening_story
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
        effectBgm!!.getList("start")
        effectBgm!!.getList("other_button")
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacks(updateText)
        mCustomTextView!!.stopAnim()
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

        mCustomTextView!!.setBackgroundResource(0)
        mCustomTextView = null
        endImage!!.setImageResource(0)
        endImage!!.setImageDrawable(null)
        endImage = null

        talker1!!.setImageResource(0)
        talker1!!.setImageDrawable(null)
        talker2!!.setImageResource(0)
        talker2!!.setImageDrawable(null)
        setumei!!.setImageResource(0)
        setumei!!.setImageDrawable(null)
        talker1 = null
        talker2 = null
        setumei = null
        mainChar1!!.setImageResource(0)
        mainChar1!!.setImageDrawable(null)
        mainChar2!!.setImageResource(0)
        mainChar2!!.setImageDrawable(null)
        mainChar1 = null
        mainChar2 = null

        Glide.get(this).clearMemory()
    }
}