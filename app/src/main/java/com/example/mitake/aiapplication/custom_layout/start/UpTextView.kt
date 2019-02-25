package com.example.mitake.aiapplication.custom_layout.start

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Message
import android.text.Spanned
import android.util.AttributeSet
import android.widget.TextView

internal class UpTextView : TextView {
    var defautText: Spanned? = null

    var value: Long = 0xffffffff
    var value0: Long = 0x00ffffff

    var tranY: Float = 2000.0f

    var flag = 1

    var count = 1

    var trans = 5f

    /** アニメーション制御変数 */
    var canStart = true

    /** 文字列が上へ流れるハンドラ */
    private val handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun dispatchMessage(msg: Message) {
            val interval = INTERVAL

            if (canStart) {
                if (msg.what == TIMEOUT_MESSAGE) {
                    text = defautText
                    tranY -= trans
                    translationY = tranY
                    this.sendEmptyMessageDelayed(TIMEOUT_MESSAGE, (interval).toLong())
                } else {
                    super.dispatchMessage(msg)
                }
            }
        }
    }

    fun startCharByCharAnim() {
        canStart = true
        handler.sendEmptyMessage(TIMEOUT_MESSAGE)
    }

    fun stopAnim(){
        canStart = false
    }

    fun restartAnim(){
        canStart = true
        handler.sendEmptyMessage(TIMEOUT_MESSAGE)
    }

    fun setTargetText(target: Spanned) {
        this.defautText = target
        this.setTextColor(value.toInt())
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    companion object {
        const val TIMEOUT_MESSAGE = 1
        const val INTERVAL = 40
    }
}