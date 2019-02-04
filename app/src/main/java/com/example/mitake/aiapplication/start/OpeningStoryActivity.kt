package com.example.mitake.aiapplication.start

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

class OpeningStoryActivity : AppCompatActivity() {
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
            TextString("「いらっしゃい」","人形屋"),
            TextString("「ひさしぶりだな。今日はどうしたんだ、先生」","人形屋"),
            TextString("「人形を一体買いに来た。この子の誕生日プレゼントだ」","父"),
            TextString("「なんだって？贅沢な子供だな」","人形屋"),
            TextString("人形屋はギロリと僕を睨みつけた。",""),
            TextString("「俺が初めて人形を手に入れたのは19のときだったんだぞ」","人形屋"),
            TextString("「必死になって金を貯めて、ボロボロの中古の人形を買った」","人形屋"),
            TextString("「昔から君は人形が大好きだったね」","父"),
            TextString("「ああそうだ、俺は人形が大好きなんだ」","人形屋"),
            TextString("「だから万が一にも一時の物欲しさで買って、」","人形屋"),
            TextString("「飽きて放置してダメにするような奴に俺は売りたくねえんだ」","人形屋"),
            TextString("「例え先生のご子息であっても、な」","人形屋"),
            TextString("「だから確かめさせてもらうぜ」","人形屋"),
            TextString("人形屋はスイングドアを足で押してカウンターから出てくると、僕の前で止まった。",""),
            TextString("そしてその場にしゃがみ込んで真っすぐに目を合わせて聞いてきた。",""),
            TextString("「おい坊主、お前はなんで人形が欲しいんだ？」","人形屋"),
            TextString("「コロシアムに出たいからだ」","主人公"),
            TextString("「コロシアム？」","人形屋"),
            TextString("「なんのためにだ？目当てはなんだ？賞金か？」","人形屋"),
            TextString("「違う！」","主人公"),
            TextString("「僕はコロシアムで優勝して、」","主人公"),
            TextString("「……まだ知らない地上の世界を歩いてみたいんだ！」","主人公"),
            TextString("「――！！」","人形屋"),
            TextString("「……そうか」","人形屋"),
            TextString("彼は口の端だけでニヤリと笑った。",""),
            TextString("指の長い大きな手で頭をわしわしと撫でられた。",""),
            TextString("「親子だな……」","人形屋"),
            TextString("ボソリと呟かれたその言葉の真意を逡巡する間はなかった。",""),
            TextString("人形屋はすくっと立ち上がるや否や、スイングドアにぶつかりながらカウンターの内側に戻っていった。",""),
            TextString("何かを掴んで再びカウンターから出てくると、部屋の奥の古びた扉を指さしながら僕に向かって手招きした。",""),
            TextString("「いいだろう、こっち来いよ。」","人形屋"),
            TextString("「初心者でも扱いやすく性能の良いとっておきの人形を選ばせてやるぜ！！」","人形屋"),
            TextString("人形屋は手に持った鍵束の中から扉に合うカギをガチャガチャ鳴らしながら探している。",""),
            TextString("「よかったね、行ってきなさい」","父"),
            TextString("「父さん……」","主人公"),
            TextString("「ありがとう」","主人公"),
            TextString("「開いたぜ、はやく来いよ！！」","人形屋"),
            TextString("人形屋は扉を足で開けながら僕を待っていた。",""),
            TextString("僕は人形屋に連れられて、古びた扉の先へ足を踏み入れた。",""),
            TextString("後ろを振り向くと、僕に気が付いた父さんが店内の椅子に腰かけながら手でいってらっしゃいの合図をしてくれた。",""),
            TextString("先の見えない薄暗い通路を歩いていた。通路の両側には入り口と同じような古びた扉が一定間隔で並んでいた。",""),
            TextString("通路には物がなく、しっかりと掃除がされている雰囲気だったが、ツンとしたカビの匂いが立ち込めていた。",""),
            TextString("袖で鼻と口を抑えながら歩いていると、それに気が付いた人形屋が話しかけてきた。",""),
            TextString("「ここはお前が住んでる高級住宅街じゃないから人工光なんて当たらないからな」","人形屋"),
            TextString("「一日中薄暗いから油断するとすぐカビが生えてきやがる」","人形屋"),
            TextString("「まぁ自動人形に関係がある部屋さえ大丈夫なら俺は気にしないんだけどな」","人形屋"),
            TextString("「さて、もう行くぞ」","人形屋"),
            TextString("ここだな、そう呟いて彼は突然立ち止まった。",""),
            TextString("彼は自分が首から下げていた鍵の形のペンダントを鍵穴に差し込んで扉を開けた。",""),
            TextString("「さあ選んでくれ、ここには俺が現役時代に集めていた選りすぐりの自動人形のパーツ置き場なんだ」","人形屋"),
            TextString("「俺が早々に引退しちまったから全部新品なんだけどな」","人形屋"),
            TextString("「見た目で性能は変わらないから好きなものを選べよ！！」","人形屋"),
            TextString("「本当にこいつでいいんだな？」","人形屋"),
            TextString("「分かった。じゃあ今日からお前がこいつのマスターだ」","人形屋"),
            TextString("「ところで自動人形の売買には書類が必要なんだ」","人形屋"),
            TextString("「代金は既に先生……お前の父さんが払ってくれたからいいんだが」","人形屋"),
            TextString("「この自動人形の権利を俺からお前に移動するという書類にお前のサインが必要なんだ」","人形屋"),
            TextString("「ここに自分で書けるよな？」","人形屋")
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
        setContentView(R.layout.activity_opening_story)

        // 人形屋・主人公の画像を設定
        talker1 = findViewById(R.id.talker_image1)
        talker1!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        talker1!!.alpha = 0f
        Glide.with(applicationContext).load(R.drawable.ningyoya).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(talker1!!)
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
        talker1 = null
        talker2 = null
        mainChar1!!.setImageResource(0)
        mainChar1!!.setImageDrawable(null)
        mainChar2!!.setImageResource(0)
        mainChar2!!.setImageDrawable(null)
        mainChar1 = null
        mainChar2 = null

        Glide.get(this).clearMemory()
    }
}