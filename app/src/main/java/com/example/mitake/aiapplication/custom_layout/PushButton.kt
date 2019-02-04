package com.example.mitake.aiapplication.custom_layout

import android.content.Context
import android.util.AttributeSet
import android.widget.Button

/*** プッシュするエフェクトのボタン  */
class PushButton : Button {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun setPressed(pressed: Boolean) {
        if (pressed) {
            this.scaleX = 0.92f
            this.scaleY = 0.96f
        } else {
            this.scaleY = 1.0f
            this.scaleX = 1.0f
        }
        super.setPressed(pressed)
    }

}