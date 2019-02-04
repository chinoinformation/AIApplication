package com.example.mitake.aiapplication.custom_layout.quest

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.mitake.aiapplication.R

@Suppress("DEPRECATION")
class PointAIFragment: Fragment(){
    private var typeAI: TextView? = null
    private var characterAI: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_point_ai, container, false)

        typeAI = root.findViewById(R.id.type_ai)
        typeAI!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        characterAI = root.findViewById(R.id.character_ai)
        characterAI!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments

        if (args != null) {
            val typeNum = args.getInt("Number")
            val typeAIColor = if (typeNum == 0) "#00bfff" else "#ff4500"
            val textTypeAI = if (typeNum == 0) activity!!.resources.getString(R.string.enemy_type_strongAI) else activity!!.resources.getString(R.string.enemy_type_weakAI)
            val textCharacterAI = if (typeNum == 0) activity!!.resources.getString(R.string.detail_strongAI) else activity!!.resources.getString(R.string.detail_weakAI)
            typeAI!!.text = Html.fromHtml(textTypeAI)
            typeAI!!.setTextColor(android.graphics.Color.parseColor(typeAIColor))
            characterAI!!.text = Html.fromHtml(textCharacterAI)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        typeAI!!.setBackgroundResource(0)
        typeAI = null
        characterAI!!.setBackgroundResource(0)
        characterAI = null
    }

    companion object {
        fun newInstance(num: Int): PointAIFragment {
            // Fragment インスタンス生成
            val fragmentPointAI = PointAIFragment()

            // Bundle にパラメータを設定
            val args = Bundle()
            args.putInt("Number", num)
            fragmentPointAI.arguments = args

            return fragmentPointAI
        }
    }

}