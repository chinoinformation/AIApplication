package com.example.mitake.aiapplication.custom_layout

import android.annotation.SuppressLint
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.util.TypedValue
import android.widget.TextView
import android.widget.LinearLayout
import android.view.ViewGroup
import android.view.Gravity
import android.os.Build
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import com.example.mitake.aiapplication.R


class WaveAnimationLayout : LinearLayout {

    var color: Int = 0 // テキストカラー
    var textSizePx = DEFAULT_TEXT_SIZE
        private set //テキストサイズ
    private var speed = DEFAULT_SPEED //次のアニメーションが実行するスピード
    private var text: String? = null
    private var startOffset: Int = 0 //最初のアニメーション開始までのoffset
    private var linearLayouts: ArrayList<LinearLayout>? = null

    var anim: Int = R.animator.popup

    //アニメーションの終了を通知するリスナー
    interface EndAnimationListener {
        fun onEnd()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        parseAttribute(attrs)
        init()
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        parseAttribute(attrs)
        init()
    }

    fun setAnimId(id: Int){
        anim = id
    }

    fun setTextSizePx(textSizePx: Int) {
        this.textSizePx = textSizePx.toFloat()
    }

    fun getText(): String? {
        return text
    }

    fun setText(text: String) {
        this.text = text
        init()
    }

    @SuppressLint("Recycle")
            /**
     * layoutのattrsパラメーターが設定されていれば取得し、設定する
     *
     * @param attrs
     */
    fun parseAttribute(attrs: AttributeSet) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.WaveAnimationLayout)
        if (ta != null) {
            text = ta.getString(R.styleable.WaveAnimationLayout_waveText)
            color = ta.getColor(R.styleable.WaveAnimationLayout_waveTextColor, DEFAULT_COLOR)
            textSizePx = ta.getDimension(R.styleable.WaveAnimationLayout_waveTextSize, DEFAULT_TEXT_SIZE)
        }
    }

    fun init() {
        if (linearLayouts != null) {
            for (i in 0 until linearLayouts!!.size) {
                linearLayouts!![i].removeAllViews()
            }
        }
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        linearLayouts = arrayListOf()
        addTextViews()
    }

    fun createLinearLayout(): LinearLayout {
        val linearLayout = LinearLayout(context, null)
        linearLayout.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        linearLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        linearLayouts!!.add(linearLayout)
        addView(linearLayout)
        return linearLayout
    }

    fun setStartOffset(offset: Int) {
        startOffset = offset
    }

    fun setSpeed(speed: Int) {
        this.speed = speed
    }

    @SuppressLint("SetTextI18n")
            /**
     * 設定されているtextの一文字ずつ取り出し、TextViewにして
     * LinearLayoutに追加していく
     */
    fun addTextViews() {
        var linearLayout = createLinearLayout()
        for (i in 0 until text!!.length) {
            //改行が見つかれば、新しいLinearLayout(horizontal)を追加する
            if (text!![i] == '\n') {
                linearLayout = createLinearLayout()
                continue
            }
            val textView = TextView(context)
            textView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            textView.setTextColor(color)
            textView.setShadowLayer(7.0f, 1.5f, 1.5f, Color.BLACK)
            textView.typeface = Typeface.DEFAULT_BOLD
            textView.textSize = textSizePx
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx)
            textView.text = text!![i] + ""
            textView.visibility = View.INVISIBLE
            linearLayout.addView(textView)
        }
    }

    @SuppressLint("ResourceType")
            /**
     * 内包している全てのViewに対してAnimationを実行していく
     *
     * @param endAnimationListener
     */
    fun startWaveAnimation(endAnimationListener: EndAnimationListener?) {

        var offset = startOffset
        for (i in 0 until linearLayouts!!.size) {
            for (j in 0 until linearLayouts!![i].childCount) {
                val animation = AnimationUtils.loadAnimation(context, anim)
                animation.startOffset = offset.toLong()
                //最後のアニメーションに対してListenerを設定する
                if (j == linearLayouts!![i].childCount - 1 && i == linearLayouts!!.size - 1) {
                    animation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation) {

                        }

                        override fun onAnimationEnd(animation: Animation) {
                            endAnimationListener?.onEnd()
                        }

                        override fun onAnimationRepeat(animation: Animation) {}
                    })
                }
                linearLayouts!![i].getChildAt(j).visibility = View.VISIBLE
                linearLayouts!![i].getChildAt(j).startAnimation(animation)
                offset += speed
            }
        }
    }

    companion object {

        private val DEFAULT_COLOR = -0x1
        private val DEFAULT_TEXT_SIZE = 25f
        private val DEFAULT_SPEED = 50
    }
}