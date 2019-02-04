package com.example.mitake.aiapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions

@Suppress("DEPRECATION")
class GlideAnim() {

    fun animation(context: Context, image: ImageView, bmpName: String, drawName: String){
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.RGB_565
            inPurgeable = true
        }
        val bitmap = BitmapFactory.decodeResource(context.resources,
                context.resources.getIdentifier(bmpName, "drawable",
                        context.packageName), options)
        val drawId: Int = context.resources.getIdentifier(drawName, "drawable", context.packageName)
        image.setImageResource(0)
        image.setImageDrawable(null)
        Glide.with(context).load(drawId).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).apply(RequestOptions().override(bitmap.width,bitmap.height)).into(image)
        bitmap.recycle()
    }
}