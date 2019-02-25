package com.example.mitake.aiapplication.start

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.text.Spanned
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.bgm.MyService
import com.example.mitake.aiapplication.custom_layout.start.UpTextView
import com.example.mitake.aiapplication.data.DataManagement
import com.example.mitake.aiapplication.home.IntentActivity
import kotlinx.android.synthetic.main.activity_opening.*

@Suppress("DEPRECATION")
class OpeningActivity : AppCompatActivity() {

    /**
     * mCustomTextView: 文字が流れるテキストビュー
     * initialNum: 初期設定変数
     * str: 初めに流れるテキスト
     * str2: 最後に流れるテキスト
     * newStr: 現在流れるテキスト
     */
    private var mCustomTextView: UpTextView? = null
    private var firstPosition: Float = 0f
    private var start = false
    private var textHeight: Int = -2000
    private var initialNum = 0
    private var str: Spanned? = null
    private var str2: Spanned? = null
    private var newStr: Spanned? = null

    /** スレッドUI操作用ハンドラ  */
    private val mHandler = Handler()
    /** テキストオブジェクト  */
    private var updateText: Runnable? = null

    /** BGM再生 */
    private var bgmId: Int = R.raw.bgm_opening
    private var bgmFlag = 0
    private var am: AudioManager? = null
    private var mVol: Float = 0f
    private var ringVolume: Float = 0f

    /** 背景画像アニメション変数 */
    private var background: ImageView? = null
    private var set: AnimatorSet? = null

    /** プリファレンス */
    private var data: DataManagement? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opening)

        background = findViewById(R.id.opening_background)

        str = Html.fromHtml(getString(R.string.story1))
        str2 = Html.fromHtml(getString(R.string.story2))

        // 文字が流れるカスタムテキストビューを設定
        mCustomTextView = findViewById(R.id.customTextView)
        mCustomTextView!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        mCustomTextView!!.setTargetText(str!!)

        // 初めにstrを流す
        newStr = str

        /**
         * (1) mCustomTextViewの位置が-2000よりも低い場所にあるとき -> 流すテキストを変更する
         * (2) 流すテキストが無い場合は画面遷移する
         */
        updateText = Runnable {
            if (start) {
                if (mCustomTextView!!.tranY < textHeight) {
                    flowText()
                }
                if (mCustomTextView!!.count > 2) {
                    start = false
                    mHandler.removeCallbacks(updateText)
                    val intent = Intent(this, IntentActivity::class.java)
                    intent.putExtra("Name", "OpeningStory")
                    intent.putExtra("musicId", bgmId)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                }
            }
            mHandler.removeCallbacks(updateText)
            mHandler.postDelayed(updateText, 100)
        }
        mHandler.postDelayed(updateText, 100)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!start){
            start = true
            initPosition()
            mCustomTextView!!.tranY = firstPosition
            mCustomTextView!!.startCharByCharAnim()
        }
    }

    /**
     * 画面タッチイベント
     * 画面をタッチしている間はテキストを流す速さを変える
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (mCustomTextView!!.count <= 2) {
            if (ev!!.action == MotionEvent.ACTION_DOWN) {
                mCustomTextView!!.trans = 25f
            }
            if (ev.action == MotionEvent.ACTION_UP){
                mCustomTextView!!.trans = 5f
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

    /** テキスト位置を初期化 */
    private fun initPosition(){
        firstPosition = (background!!.height + 200).toFloat()
        textHeight = -1 * (mCustomTextView!!.height + 200)
        init(0f, 0f, 0f, firstPosition, 0)
    }

    private fun init(saX: Float, moveX: Float, saY: Float, moveY: Float, duration: Long){
        set = AnimatorSet()
        var setDuration = duration
        if (Math.abs(saX - moveX) == 0f && Math.abs(saY - moveY) == 0f){
            setDuration = 0
        }
        set!!.duration = setDuration
        val objectAnimatorX = ObjectAnimator.ofFloat(mCustomTextView!!, "TranslationX", saX, moveX)
        val objectAnimatorY = ObjectAnimator.ofFloat(mCustomTextView!!, "TranslationY", saY, moveY)
        if (Math.abs(saX - moveX) == 0f) {
            set!!.play(objectAnimatorX).after(objectAnimatorY)
        }else{
            set!!.play(objectAnimatorY).after(objectAnimatorX)
        }

        // アニメーション終了した時の処理
        set!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) { set = null }
        })

        set!!.start()
    }

    /*
    流れるテキストを変更する関数
    (1) count == 0: strを流す
    (2) count == 1: str2を流す
    (3) count == 2: 文字を流すのをやめる
    */
    private fun flowText() {
        mCustomTextView!!.tranY = firstPosition
        if (mCustomTextView!!.count == 0) newStr = str
        if (mCustomTextView!!.count == 1) newStr = str2
        if (mCustomTextView!!.count == 2) mCustomTextView!!.value = mCustomTextView!!.value0
        mCustomTextView!!.count += 1
        mCustomTextView!!.setTargetText(newStr!!)
    }

    override fun onResume(){
        super.onResume()

        // 文字を流すアニメーションを再開する
        if (initialNum != 0) {
            mHandler.postDelayed(updateText, 100)
            customTextView!!.restartAnim()
        }
        initialNum = 1
        // プレイヤーの処理
        bgmId = R.raw.bgm_opening
        val intent = Intent(applicationContext, MyService::class.java)
        intent.putExtra("id", bgmId)
        if (bgmFlag == 0) {
            // プレイヤーの初期化
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
    }

    override fun onPause() {
        super.onPause()

        // 文字を流すアニメーションを一時停止する
        mHandler.removeCallbacks(updateText)
        customTextView!!.stopAnim()

        if (!isFinishing) {
            val intent = Intent(applicationContext, MyService::class.java)
            intent.putExtra("id", bgmId)
            intent.putExtra("flag", 1)
            startService(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mCustomTextView!!.setBackgroundResource(0)
        mCustomTextView = null
        newStr = null
        data = null
        am = null
        mVol = 0f
        ringVolume = 0f
    }

    override fun onBackPressed() {}
}
