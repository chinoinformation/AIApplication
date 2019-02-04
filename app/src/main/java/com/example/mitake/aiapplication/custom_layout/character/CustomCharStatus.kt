package com.example.mitake.aiapplication.custom_layout.character

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import com.example.mitake.aiapplication.R

class CustomCharStatus(context: Context, attr: AttributeSet): GridLayout(context, attr) {
    val charName: TextView
    val charType: TextView
    val charImg: ImageView
    var customStatus: CustomStatusName? = null

    init{
        val layout = LayoutInflater.from(context).inflate(R.layout.char_status, this)

        charName = layout.findViewById(R.id.char_name)
        charName.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        charType = layout.findViewById(R.id.char_type)
        charType.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        charImg = layout.findViewById(R.id.char_image)
        charImg.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        customStatus = layout.findViewById(R.id.custom_status)
    }
}