package com.example.mitake.aiapplication.custom_layout.quest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.mitake.aiapplication.R


class QuestListAdapter(context: Context, private val mResource: Int, private val mItems: List<CustomQuestList>) : ArrayAdapter<CustomQuestList>(context, mResource, mItems) {
    /*
     * コンストラクタ
     * @param context コンテキスト
     * @param resource リソースID
     * @param items リストビューの要素
     */

    val mInflater: LayoutInflater

    init {
        mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View

        if (convertView != null) {
            view = convertView
        } else {
            view = mInflater.inflate(mResource, null)
        }

        // リストビューに表示する要素を取得
        val item = mItems[position]

        // クエスト名を設定
        val questName = view.findViewById(R.id.quest_view_image) as TextView
        questName.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        questName.text = item.getQuestName()

        // クエストタイプを設定
        val questType = view.findViewById(R.id.quest_type) as TextView
        questName.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        questType.text = item.getQuestType()

        // クエスト条件を設定
        val questConstraint = view.findViewById(R.id.quest_constraint) as TextView
        questConstraint.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        questConstraint.text = item.getQuestConstraint()

        // 勝利条件を設定
        val victoryCondition = view.findViewById(R.id.victory_condition) as Button
        victoryCondition.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        victoryCondition.text = item.getVictoryCondition()

        val questBackground: LinearLayout = view.findViewById(R.id.background_quest_name)
        questBackground.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        questBackground.background = item.getBackground()

        return view
    }
}
