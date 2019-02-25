package com.example.mitake.aiapplication.quest

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
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.example.mitake.aiapplication.GlideAnim
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.bgm.EffectList
import com.example.mitake.aiapplication.bgm.MyService
import com.example.mitake.aiapplication.custom_layout.quest.StoryReader
import com.example.mitake.aiapplication.custom_layout.start.CharByCharTextView
import com.example.mitake.aiapplication.custom_layout.start.TextString
import com.example.mitake.aiapplication.data.DataManagement
import com.example.mitake.aiapplication.home.IntentActivity
import java.util.*

class TutorialStoryActivity : AppCompatActivity() {
    // BGM再生
    private var bgmId: Int = 0
    private var bgmFlag = 0
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null
    private var am: AudioManager? = null
    private var mVol: Float = 0f
    private var ringVolume: Float = 0f

    private var isStoryStart = false
    private var initialNum = 0
    private var finish = false
    private var mCustomTextView: CharByCharTextView? = null
    private var startCount = 0
    private var canTouch = false
    private var talkerName: TextView? = null
    private var newStr: String? = null
    private var endImage: ImageView? = null
    private var talker1: ImageView? = null
    private var talker2: ImageView? = null
    private var title: TextView? = null
    private var storyImages: ImageView? = null

    private var isCharInit = true
    private val mainID = arrayOf(1,2,3,4)
    private val ningyoyaId = arrayOf(1,2,3)

    /** スレッドUI操作用ハンドラ  */
    private val mHandler = Handler()
    /** テキストオブジェクト  */
    private var updateText: Runnable? = null

    private var parser: StoryReader? = null
    private var textCount = 1

    /** プリファレンス */
    private var data: DataManagement? = null

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial_story)

        // ストーリー読み込み
        val storyNum = intent.getIntExtra("story_number", 0)
        parser = StoryReader()
        parser!!.reader(this, storyNum)

        mCustomTextView = findViewById(R.id.customTextView)
        mCustomTextView!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        talkerName = findViewById(R.id.talker)
        talkerName!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        endImage = findViewById(R.id.end)
        endImage!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        talker1 = findViewById(R.id.talker_image1)
        talker1!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        talker2 = findViewById(R.id.talker_image2)
        talker2!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        storyImages = findViewById(R.id.images_story)
        storyImages!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        endImage!!.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.animator.text_updown))
        endImage!!.alpha = 0f

        newStr = parser!!.objects[1].textWords
        talkerName!!.text = parser!!.objects[1].characterName

        title = findViewById(R.id.tutorial_story_title)
        title!!.text = intent.getStringExtra("tutorial_title")
        title!!.visibility = View.INVISIBLE

        updateText = Runnable {
            if (!finish && mCustomTextView!!.text == newStr && mCustomTextView!!.flag == 0){
                canTouch = true
                endImage!!.alpha = 1f
                if (textCount >= parser!!.objects.size-1){
                    finish = true
                }
            }
            mHandler.removeCallbacks(updateText)
            mHandler.postDelayed(updateText, 100)
        }
        mHandler.postDelayed(updateText, 100)
    }

    @SuppressLint("ResourceType")
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!isStoryStart) {
            isStoryStart = true
            val anim = AnimationUtils.loadAnimation(applicationContext, R.animator.zoom_init)
            title!!.startAnimation(anim)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(arg0: Animation) {}
                override fun onAnimationRepeat(arg0: Animation) {}
                override fun onAnimationEnd(arg0: Animation) {
                    startStory()
                }
            })
        }
    }

    @SuppressLint("ResourceType")
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (startCount == 1){
            startCount = 2
            val anim = AnimationUtils.loadAnimation(applicationContext, R.animator.zoom_out)
            title!!.startAnimation(anim)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(arg0: Animation) {}
                override fun onAnimationRepeat(arg0: Animation) {}
                override fun onAnimationEnd(arg0: Animation) {
                    charInit()
                    sayChar(talker1!!, talker2!!, parser!!.objects[1].characterName)
                    talkerName!!.visibility = View.VISIBLE
                    mCustomTextView!!.visibility = View.VISIBLE
                    mCustomTextView!!.setTargetText(parser!!.objects[1].textWords)
                    mCustomTextView!!.startCharByCharAnim()
                }
            })
        } else {
            if (canTouch && textCount < parser!!.objects.size + 1) {
                if (ev!!.action == MotionEvent.ACTION_UP) {
                        if (mCustomTextView!!.flag == 0) {
                            textCount += 1
                            if (textCount > parser!!.objects.size - 1) {
                                if (finish) {
                                    finishStory()
                                }
                            } else {
                                if (textCount > parser!!.objects.size) textCount = parser!!.objects.size - 1
                                imageAnimation()
                                endImage!!.alpha = 0f
                                talkerName!!.text = parser!!.objects[textCount].characterName
                                newStr = parser!!.objects[textCount].textWords
                                mCustomTextView!!.setTargetText(newStr!!)
                                mCustomTextView!!.startCharByCharAnim()
                            }
                        } else if (mCustomTextView!!.flag == 1) {
                            canTouch = false
                            mCustomTextView!!.flag = 2
                        }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /** 登場人物の初期化 */
    private fun charInit(){
        Glide.with(applicationContext).load(R.drawable.ningyoya_2).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(talker1!!)
        Glide.with(applicationContext).load(R.drawable.aren_4).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(talker2!!)
        isCharInit = true
    }

    /**
     * 登場人物のアニメーション
     * 位置を確定
     */
    @SuppressLint("ResourceType")
    private fun startStory(){
        val anim = AnimationUtils.loadAnimation(applicationContext, R.animator.zoom_in)
        title!!.startAnimation(anim)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(arg0: Animation) {
                title!!.visibility = View.VISIBLE
            }
            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                startCount = 1
            }
        })
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
            else -> {
                img1.setColorFilter(Color.parseColor("#00ffffff"))
                img2.setColorFilter(Color.parseColor("#00ffffff"))
            }
        }
    }

    /** アニメーションを付ける場合の処理 */
    private fun imageAnimation(){
        if (parser!!.objects[textCount].animation.toInt() != 0){
            isCharInit = false
            talker1!!.setImageDrawable(null)
            talker1!!.setImageResource(0)
            talker2!!.setImageDrawable(null)
            talker2!!.setImageResource(0)
            when {
                parser!!.objects[textCount].animation.toInt() == 2 -> GlideAnim().animation(applicationContext, storyImages!!, parser!!.objects[textCount].resource + "_1", parser!!.objects[textCount].resource)
                else -> Glide.with(applicationContext).load(resources.getIdentifier(parser!!.objects[textCount].resource, "drawable", packageName)).into(storyImages!!)
            }
            Glide.get(this).clearMemory()
        } else {
            if (!isCharInit){
                charInit()
            }
            storyImages!!.setImageDrawable(null)
            storyImages!!.setImageResource(0)
            findCharResource(parser!!.objects[textCount].characterName, parser!!.objects[textCount].ID.toInt())
            sayChar(talker1!!, talker2!!, parser!!.objects[textCount].characterName)
        }
    }

    /** ストーリーを終了する */
    private fun finishStory(){
        canTouch = false
        val intent = Intent(this, IntentActivity::class.java)
        intent.putExtra("Name", "TutorialQuest")
        intent.putExtra("musicId", bgmId)
        startActivity(intent)
        finish()
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

    // 再開時の処理
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
        effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
        effectBgm!!.getList("start")
        effectBgm!!.getList("other_button")
    }

    // 一時停止状態の処理
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
        talkerName!!.setBackgroundResource(0)
        talkerName = null

        talker1!!.setImageResource(0)
        talker1!!.setImageDrawable(null)
        talker1 = null
        talker2!!.setImageResource(0)
        talker2!!.setImageDrawable(null)
        talker2 = null

        title!!.setBackgroundResource(0)
        title = null
        storyImages!!.setImageResource(0)
        storyImages!!.setImageDrawable(null)
        storyImages = null

        data = null
        parser = null
        am = null
        mVol = 0f
        ringVolume = 0f

        Glide.get(this).clearMemory()
    }

    override fun onBackPressed() {}
}
