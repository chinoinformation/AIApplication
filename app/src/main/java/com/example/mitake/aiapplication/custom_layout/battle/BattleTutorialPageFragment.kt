package com.example.mitake.aiapplication.custom_layout.battle

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.example.mitake.aiapplication.R

class BattleTutorialPageFragment: Fragment(){
    /** チュートリアル画像 */
    private var tutorialImage: ImageView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_battle_tutorial_page, container, false)

        tutorialImage = root.findViewById(R.id.battle_tutorial_image)
        tutorialImage!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments

        if (args != null) {
            val typeNum = args.getInt("Number")
            val imgId = resources.getIdentifier("battle_tutorial$typeNum", "drawable", activity!!.packageName)
            Glide.with(context!!).load(imgId).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(tutorialImage!!)
        }
    }

    companion object {
        fun newInstance(num: Int): BattleTutorialPageFragment {
            // Fragment インスタンス生成
            val battleTutorialPageFragment = BattleTutorialPageFragment()

            // Bundle にパラメータを設定
            val args = Bundle()
            args.putInt("Number", num)
            battleTutorialPageFragment.arguments = args

            return battleTutorialPageFragment
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        tutorialImage!!.setImageResource(0)
        tutorialImage!!.setImageDrawable(null)
        tutorialImage = null
        Glide.get(context!!).clearMemory()
    }

}