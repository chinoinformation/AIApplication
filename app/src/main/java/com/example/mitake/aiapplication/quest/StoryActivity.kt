package com.example.mitake.aiapplication.quest

import android.annotation.SuppressLint
import android.content.Intent
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
import com.example.mitake.aiapplication.bgm.MyService
import com.example.mitake.aiapplication.custom_layout.start.CharByCharTextView
import com.example.mitake.aiapplication.custom_layout.start.TextString
import com.example.mitake.aiapplication.home.IntentActivity
import java.util.*

class StoryActivity : AppCompatActivity() {
    // BGM再生
    private var bgmId: Int = R.raw.bgm_opening_story
    private var bgmFlag = 0

    private var initialNum = 0
    private var finish = false
    private var mCustomTextView: CharByCharTextView? = null
    private var canTouch = false

    private var talkerName: TextView? = null
    private var newStr: String? = null
    private var endImage: ImageView? = null

    private var talker1: ImageView? = null
    private var talker2: ImageView? = null

    private var storyImages: ImageView? = null

    /** スレッドUI操作用ハンドラ  */
    private val mHandler = Handler()
    /** テキストオブジェクト  */
    private var updateText: Runnable? = null

    private var backButton: Button? = null

    private val texts = Arrays.asList(
            TextString("test1test1test1test1test1test1test1","test1"),
            TextString("test2test2test2test2test2test2test2","test2")
    )

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        mCustomTextView = findViewById(R.id.customTextView)
        mCustomTextView!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        talkerName = findViewById(R.id.talker)
        talkerName!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        endImage = findViewById(R.id.end)
        endImage!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        talker1 = findViewById(R.id.talker_image1)
        talker1!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        Glide.with(applicationContext).load(R.drawable.ningyoya).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(talker1!!)
        talker2 = findViewById(R.id.talker_image2)
        talker2!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        Glide.with(applicationContext).load(R.drawable.aren_2).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(talker2!!)
        storyImages = findViewById(R.id.images_story)
        storyImages!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        endImage!!.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.animator.text_updown))

        newStr = texts[0].textwords
        talkerName!!.text = texts[0].charactername
        mCustomTextView!!.setTargetText(texts[0].textwords)
        mCustomTextView!!.startCharByCharAnim()

        updateText = Runnable {
            if (!finish && mCustomTextView!!.text == newStr && mCustomTextView!!.flag == 0){
                canTouch = true
                endImage!!.alpha = 1f
            }
            mHandler.removeCallbacks(updateText)
            mHandler.postDelayed(updateText, 100)
        }
        mHandler.postDelayed(updateText, 100)

        backButton = findViewById(R.id.back)
        backButton!!.setOnClickListener {
            val intent = Intent(this, IntentActivity::class.java)
            intent.putExtra("Name", "Home")
            intent.putExtra("musicId", bgmId)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (canTouch) {
            if (ev!!.action == MotionEvent.ACTION_UP) {
                if (mCustomTextView!!.flag == 0) {
                    if (newStr == "test2test2test2test2test2test2test2") newStr = "test1test1test1test1test1test1test1"
                    var index = 0
                    for (i in 0 until texts.size) {
                        if (texts[i].textwords == newStr) {
                            index = i + 1
                            break
                        }
                    }

                    endImage!!.alpha = 0f
                    talkerName!!.text = texts[index].charactername
                    newStr = texts[index].textwords
                    mCustomTextView!!.setTargetText(newStr!!)
                    mCustomTextView!!.startCharByCharAnim()

                } else if (mCustomTextView!!.flag == 1) {
                    canTouch = false
                    mCustomTextView!!.flag = 2
                }
            }
        }
        return super.dispatchTouchEvent(ev)
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
        } else {
            intent.putExtra("flag", 2)
            startService(intent)
        }
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
    }

    override fun onDestroy() {
        super.onDestroy()

        talkerName!!.setBackgroundResource(0)
        talkerName = null
        endImage!!.setImageResource(0)
        endImage!!.setImageDrawable(null)
        endImage = null
        talker1!!.setImageResource(0)
        talker1!!.setImageDrawable(null)
        talker1 = null
        talker2!!.setImageResource(0)
        talker2!!.setImageDrawable(null)
        talker2 = null

        backButton!!.setBackgroundResource(0)
        backButton = null
        Glide.get(this).clearMemory()
    }
}
