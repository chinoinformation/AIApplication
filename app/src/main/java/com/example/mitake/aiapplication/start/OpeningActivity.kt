package com.example.mitake.aiapplication.start

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.text.Spanned
import android.view.MotionEvent
import android.view.View
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.bgm.MyService
import com.example.mitake.aiapplication.custom_layout.start.UpTextView
import com.example.mitake.aiapplication.home.IntentActivity
import kotlinx.android.synthetic.main.activity_opening.*

class OpeningActivity : AppCompatActivity() {

    /**
     * mCustomTextView: 文字が流れるテキストビュー
     * initialNum: 初期設定変数
     * str: 初めに流れるテキスト
     * str2: 最後に流れるテキスト
     * newStr: 現在流れるテキスト
     */
    private var mCustomTextView: UpTextView? = null
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opening)

        str = Html.fromHtml(getString(R.string.story1))
        str2 = Html.fromHtml(getString(R.string.story2))

        // 文字が流れるカスタムテキストビューを設定
        mCustomTextView = findViewById(R.id.customTextView)
        mCustomTextView!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        mCustomTextView!!.setTargetText(str!!)
        mCustomTextView!!.startCharByCharAnim()

        // 初めにstrを流す
        newStr = str

        /**
         * (1) mCustomTextViewの位置が-2000よりも低い場所にあるとき -> 流すテキストを変更する
         * (2) 流すテキストが無い場合は画面遷移する
         */
        updateText = Runnable {
            if(mCustomTextView!!.tranY < -2000){
                flowText()
            }
            if (mCustomTextView!!.count > 2) {
                mHandler.removeCallbacks(updateText)
                val intent = Intent(this, IntentActivity::class.java)
                intent.putExtra("Name", "OpeningStory")
                intent.putExtra("musicId", bgmId)
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()
            }
            mHandler.removeCallbacks(updateText)
            mHandler.postDelayed(updateText, 100)
        }
        mHandler.postDelayed(updateText, 100)
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

    /*
    流れるテキストを変更する関数
    (1) count == 0: strを流す
    (2) count == 1: str2を流す
    (3) count == 2: 文字を流すのをやめる
    */
    private fun flowText() {
        mCustomTextView!!.tranY = 2000f
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
    }

}
