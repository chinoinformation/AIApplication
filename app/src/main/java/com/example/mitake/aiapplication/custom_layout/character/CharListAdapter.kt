package com.example.mitake.aiapplication.custom_layout.character

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.example.mitake.aiapplication.R

class CharListAdapter(context: Context, private val mResource: Int, private val mItems: List<CustomCharList>) : ArrayAdapter<CustomCharList>(context, mResource, mItems) {
    /*
     * コンストラクタ
     * @param context コンテキスト
     * @param resource リソースID
     * @param items リストビューの要素
     */

    private val mInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: mInflater.inflate(mResource, null)

        // リストビューに表示する要素を取得
        val item = mItems[position]

        // サムネイル画像を設定
        val thumbnail = view.findViewById(R.id.char_image) as ImageView
        thumbnail.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        thumbnail.setImageDrawable(null)
        Glide.with(context).load(item.getThumbnail()).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(thumbnail)

        // タイトルを設定
        val title = view.findViewById(R.id.char_name) as TextView
        title.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        title.text = item.getTitle()

        return view
    }
}