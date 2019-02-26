package com.example.mitake.aiapplication.start

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
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
import com.example.mitake.aiapplication.data.DataManagement
import com.example.mitake.aiapplication.home.IntentActivity
import java.util.*

class OpeningStoryActivity : AppCompatActivity() {
    /** BGM再生 */
    private var bgmId: Int = R.raw.bgm_opening_story
    private var bgmFlag = 0
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null
    private var am: AudioManager? = null
    private var mVol: Float = 0f
    private var ringVolume: Float = 0f
    /**
     * initialNum: 初期設定変数
     * finish: ストーリーが終わったかどうかの判定変数
     * mCustomTextView: 一文字ずつ表示するカスタムテキストビュー
     */
    private var initialNum = 0
    private var finish = false
    private var mCustomTextView: CharByCharTextView? = null
    private val mainID = arrayOf(1,2,3,4)
    private val ningyoyaId = arrayOf(1,2,3)
    private val texts = Arrays.asList(
            TextString("「いらっしゃい」","人形屋", 1),
            TextString("「ずいぶん小さなお客さんだな、人形を見に来たのか？」","人形屋", 1),
            TextString("「これで人形1つ買えますか？」","主人公", 1),
            TextString("「なっ、大金じゃないか」","人形屋", 1),
            TextString("「誕生日プレゼントで人形が欲しいって言ったら、これで買ってきなさいって言われたんだ」","主人公", 1),
            TextString("「贅沢な子供だな」","人形屋", 0),
            TextString("人形屋はギロリと僕を睨みつけた。","", 0),
            TextString("「俺が初めて人形を手に入れたのは19のときだったんだぞ」","人形屋", 0),
            TextString("「必死になって働いて金を貯めて、ボロボロの中古の人形を買った」","人形屋", 0),
            TextString("「そうなんだ」","主人公", 1),
            TextString("「俺は人形が大好きなんだ」","人形屋", 0),
            TextString("「だから万が一にも，一時の物欲しさで買って、飽きて、」","人形屋", 0),
            TextString("「放置してダメにするような奴に俺は売りたくねえんだ」","人形屋", 0),
            TextString("「例え先生のご子息であっても、な」","人形屋", 0),
            TextString("「だから確かめさせてもらうぜ」","人形屋", 0),
            TextString("人形屋はスイングドアを足で押してカウンターから出てくると、僕の前で止まった。","", 0),
            TextString("そしてその場にしゃがみ込んで真っすぐに目を合わせて聞いてきた。","", 0),
            TextString("「おい坊主、」","人形屋", 0),
            TextString("「お前はなんで人形が欲しいんだ？」","人形屋", 0),
            TextString("「コロシアムに出たいからだ」","主人公", 1),
            TextString("「コロシアム？」","人形屋", 0),
            TextString("「なんのためにだ？目当てはなんだ？賞金か？」","人形屋", 0),
            TextString("「違う！」","主人公", 1),
            TextString("「僕はコロシアムで優勝して、」","主人公", 1),
            TextString("「……まだ知らない地上の世界を歩いてみたいんだ！」","主人公", 1),
            TextString("「――！！」","人形屋", 0),
            TextString("「……そうか」","人形屋", 2),
            TextString("彼は口の端だけでニヤリと笑った。","", 0),
            TextString("指の長い大きな手で頭をわしわしと撫でられた。","", 0),
            TextString("「親子だな……」","人形屋", 2),//小文字
            TextString("人形屋はカウンターに何かを取って戻ってくると、部屋の奥の古びた扉を指さしながら僕に向かって手招きした。","", 0),
            TextString("「いいだろう、こっちに来いよ。」","人形屋", 0),
            TextString("「初心者でも扱いやすく性能の良いとっておきの人形を選ばせてやるぜ！！」","人形屋", 0),
            TextString("人形屋は手に持った鍵束の中から扉に合うカギをガチャガチャ鳴らしながら探している。","", 0),
            TextString("「ありがとうございます」","主人公", 0),
            TextString("ガチャリと音がして鍵が開く音がした。","", 0),
            TextString("「開いたな、こっちだ」","人形屋", 0),
            TextString("人形屋は扉を足で開けながら僕を招き入れた。","", 0),
            TextString("僕は人形屋に連れられて、古びた扉の先へ足を踏み入れた。","", 0),
            TextString("薄暗い通路の両側には入り口と同じような古びた扉が等間隔で並んでいた。","", 0),
            TextString("通路には物がなく、しっかりと掃除がされている雰囲気だったが、ツンとしたカビの匂いが立ち込めていた。","", 0),
            TextString("袖で鼻と口を抑えながら歩いていると、それに気が付いた人形屋が話しかけてきた。","", 0),
            TextString("「ここはお前が住んでる高級住宅街じゃないから人工光なんて当たらないからな」","人形屋", 0),
            TextString("「一日中薄暗いから油断するとすぐカビが生えてきやがる」","人形屋", 0),
            TextString("「まぁ自動人形に関係がある部屋さえ大丈夫なら俺は気にしないんだけどな」","人形屋", 0),
            TextString("「さて、もうそろそろだぞ」","人形屋", 0),
            TextString("ここだな、そう呟いて彼は突然立ち止まった。","", 0),
            TextString("彼は自分が首から下げていた鍵の形のペンダントを鍵穴に差し込んで扉を開けた。","", 0),
            TextString("「さあ選んでくれ、ここには俺が現役時代に集めていた選りすぐりの自動人形のパーツ置き場なんだ」","人形屋", 0),
            TextString("「俺が早々に引退しちまったから全部新品なんだけどな」","人形屋", 0),
            TextString("「見た目で性能は変わらないから好きなものを選べよ！！」","人形屋", 0),
            TextString("「本当にこいつでいいんだな？」","人形屋", 0),
            TextString("「分かった。じゃあ今日からお前がこいつのマスターだ」","人形屋", 0),
            TextString("「ところで自動人形の売買には書類が必要なんだ」","人形屋", 0),
            TextString("「この自動人形の権利を俺からお前に移動するという書類にお前のサインが必要なんだ」","人形屋", 0),
            TextString("「ここに自分で書けるな？」","人形屋", 0)
    )
    private var textCount = 0
    private var inputUserName = false
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
    private var mainChar1: ImageView? = null
    private var mainChar2: ImageView? = null
    private var select = true
    private var canTouch = false

    /** スレッドUI操作用ハンドラ  */
    private val mHandler = Handler()
    /** テキストオブジェクト  */
    private var updateText: Runnable? = null

    /** プリファレンス */
    private var data: DataManagement? = null

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opening_story)

        // 人形屋・主人公の画像を設定
        talker1 = findViewById(R.id.talker_image1)
        talker1!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        talker1!!.alpha = 0f
        Glide.with(applicationContext).load(R.drawable.ningyoya_2).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(talker1!!)
        talker2 = findViewById(R.id.talker_image2)
        talker2!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        talker2!!.alpha = 0f
        Glide.with(applicationContext).load(R.drawable.aren_2).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(talker2!!)

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
                button!!.setOnClickListener(null)
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
        Glide.with(applicationContext).load(R.drawable.ririi_1).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(mainChar1!!)
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
            effectBgm!!.play("bell")
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
        if (select && canTouch && textCount < texts.size) {
            if (ev!!.action == MotionEvent.ACTION_UP) {
                if (mCustomTextView!!.flag == 0) {
                    textCount += 1
                    if (textCount >= texts.size-1) {
                        if (!inputUserName){
                            inputUserName = true
                            textCount = texts.size - 1
                            finish = true
                            canTouch = false
                            Handler().postDelayed({
                                val newFragment = InputUserNameFragment()
                                newFragment.isCancelable = false
                                newFragment.show(fragmentManager, "dialog")
                                button!!.alpha = 1f
                                button!!.text = "ホーム画面へ"
                            }, 500)
                        } else {
                            button!!.alpha = 1f
                            textCount = texts.size - 1
                            button!!.text = "ホーム画面へ"
                            finish = true
                            canTouch = false
                        }
                    }
                    findCharResource(texts[textCount].charactername, texts[textCount].charId)
                    sayChar(talker1!!, talker2!!, texts[textCount].charactername)
                    if (textCount == 2) {
                        val location = IntArray(2)
                        talker2!!.getLocationOnScreen(location)
                        talker2!!.alpha = 1f
                        charAnim(talker2!!, location[0].toFloat(), 0f, 400, 0)
                        talker2!!.setColorFilter(Color.parseColor("#00ffffff"))
                        effectBgm!!.play("money")
                    }
                    endImage!!.alpha = 0f
                    talkerName!!.text = texts[textCount].charactername
                    newStr = texts[textCount].textwords
                    mCustomTextView!!.setTargetText(newStr!!)
                    mCustomTextView!!.startCharByCharAnim()

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
                } else if (mCustomTextView!!.flag == 1) {
                    canTouch = false
                    mCustomTextView!!.flag = 2
                }
            }
        }
        return super.dispatchTouchEvent(ev)
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

    /**
     * 登場人物のリソース検索
     */
    private fun findCharResource(charName: String, charId: Int){
        when (charName) {
            "人形屋" -> Glide.with(applicationContext).load(resources.getIdentifier("ningyoya_" + ningyoyaId[charId].toString(), "drawable", packageName)).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(talker1!!)
            "主人公" -> Glide.with(applicationContext).load(resources.getIdentifier("aren_" + mainID[charId].toString(), "drawable", packageName)).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(talker2!!)
        }
    }

    /**
     * 登場人物のアニメーション
     * 位置を確定
     */
    private fun charAnim(img: ImageView, saX: Float, moveX: Float, duration: Long, mode: Int){
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
    private fun selectButton(img1: ImageView, img2: ImageView){
        yesButton!!.alpha = 1f
        noButton!!.alpha = 1f
        yesButton!!.setOnClickListener {
            effectBgm!!.play("other_button")
            yesButton!!.alpha = 0f
            noButton!!.alpha = 0f
            talker1!!.alpha = 1f
            talker2!!.alpha = 1f
            mainChar1!!.setOnClickListener(null)
            mainChar1!!.alpha = 0f
            mainChar2!!.setOnClickListener(null)
            mainChar2!!.alpha = 0f
            textCount += 2
            newStr = "「分かった。じゃあ今日からお前がこいつのマスターだ」"
            yesButton!!.setOnClickListener(null)
            noButton!!.setOnClickListener(null)
            mCustomTextView!!.setTargetText(newStr!!)
            mCustomTextView!!.startCharByCharAnim()
            select = true
            canTouch = true
        }
        noButton!!.setOnClickListener {
            effectBgm!!.play("other_button")
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
        effectBgm!!.getList("other_button")
        effectBgm!!.getList("bell")
        effectBgm!!.getList("money")
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
        talker1 = null
        talker2 = null
        mainChar1!!.setImageResource(0)
        mainChar1!!.setImageDrawable(null)
        mainChar2!!.setImageResource(0)
        mainChar2!!.setImageDrawable(null)
        mainChar1 = null
        mainChar2 = null
        data = null
        am = null
        mVol = 0f
        ringVolume = 0f

        Glide.get(this).clearMemory()
    }

    override fun onBackPressed() {}
}