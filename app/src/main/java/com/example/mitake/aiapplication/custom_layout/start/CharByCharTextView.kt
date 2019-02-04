package com.example.mitake.aiapplication.custom_layout.start

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.widget.TextView

internal class CharByCharTextView : TextView {
    var defautText = "文字列を１文字ずつ出力するテスト"

    /** Meta Data */
    var i = 0
    var putWord = ""
    var putText = ""

    var flag = 0

    /** アニメーション制御変数 */
    var canStart = true

    /** 文字列を一文字ずつ出力するハンドラ */
    private val handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun dispatchMessage(msg: Message) {

            // 文字列を配列に１文字ずつセット
            val data = defautText.toCharArray()
            // 配列数を取得
            val arrNum = data.size
            if (i < arrNum) {
                // flagの値によってflagの更新やintervalの値を変更
                var interval = INTERVAL
                if (flag == 0) {
                    flag = 1
                }
                if (flag == 2){
                    interval = 0
                }

                if (msg.what == TIMEOUT_MESSAGE) {
                    if (canStart) {
                        putWord = data[i].toString()
                        putText += putWord
                        text = putText
                        // flag == 2で全ての文字を出力
                        if (flag == 2) {
                            text = defautText
                            i = arrNum - 1
                        }
                        this.sendEmptyMessageDelayed(TIMEOUT_MESSAGE, (interval).toLong())
                        i++
                    }
                } else {
                    super.dispatchMessage(msg)
                }
            }else{
                flag = 0
            }
        }
    }

    fun startCharByCharAnim() {
        initMetaData()
        handler.sendEmptyMessage(TIMEOUT_MESSAGE)
    }

    fun setTargetText(target: String ) {
        this.defautText = target
    }

    fun stopAnim(){
        canStart = false
    }

    fun restartAnim(){
        canStart = true
        handler.sendEmptyMessage(TIMEOUT_MESSAGE)
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    private fun initMetaData() {
        i = 0
        putWord = ""
        putText = ""
    }

    companion object {
        const val TIMEOUT_MESSAGE = 1
        const val INTERVAL = 20
    }
}