package com.example.mitake.aiapplication.custom_layout

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton

/*** プッシュするエフェクトのボタン  */
class PushImageButton : ImageButton {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun setPressed(pressed: Boolean) {
        if (pressed) {
            scaleX = 0.92f
            scaleY = 0.96f
        } else {
            scaleY = 1.0f
            scaleX = 1.0f
        }
        super.setPressed(pressed)
    }

}