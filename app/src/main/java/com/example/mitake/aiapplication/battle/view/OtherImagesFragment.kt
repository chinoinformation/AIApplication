package com.example.mitake.aiapplication.battle.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.battle.menu.BattleTutorialFragment

class OtherImagesFragment : Fragment() {
    /** View */
    var layoutStartFinish: RelativeLayout? = null
    var sword1: ImageView? = null
    var sword2: ImageView? = null
    var textStartFinish: TextView? = null
    var dummyImage: ImageView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_other_images, container, false)

        layoutStartFinish = root.findViewById(R.id.layout_start_or_finish)
        sword1 = layoutStartFinish!!.findViewById(R.id.sword1)
        sword1!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        sword2 = layoutStartFinish!!.findViewById(R.id.sword2)
        sword2!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        textStartFinish = layoutStartFinish!!.findViewById(R.id.start_or_finish)
        textStartFinish!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        dummyImage = layoutStartFinish!!.findViewById(R.id.dummy_image)
        dummyImage!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        battleStartAnimationInit()
    }

    /** 戦闘開始アニメーション画像の初期位置設定 */
    @SuppressLint("ResourceType")
    private fun battleStartAnimationInit(){
        val swordAnim1 = AnimationUtils.loadAnimation(context!!, R.animator.sword_left_init)
        val swordAnim2 = AnimationUtils.loadAnimation(context!!, R.animator.sword_right_init)
        val textAnim = AnimationUtils.loadAnimation(context!!, R.animator.zoom_init)
        textStartFinish!!.startAnimation(textAnim)
        sword1!!.startAnimation(swordAnim1)
        sword2!!.startAnimation(swordAnim2)
        textStartFinish!!.text = resources.getString(R.string.battle_start)
        Glide.with(context!!).load(R.drawable.sword1).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(sword1!!)
        Glide.with(context!!).load(R.drawable.sword2).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(sword2!!)
    }

    /** バトル開始アニメーション(1) */
    @SuppressLint("ResourceType")
    fun battleStartAnimation1(){
        val swordAnim1 = AnimationUtils.loadAnimation(context!!, R.animator.sword_left_in)
        val swordAnim2 = AnimationUtils.loadAnimation(context!!, R.animator.sword_right_in)
        sword1!!.startAnimation(swordAnim1)
        sword2!!.startAnimation(swordAnim2)
        swordAnim1.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(arg0: Animation) {
                val anim = AnimationUtils.loadAnimation(context!!, R.animator.zoom_in)
                textStartFinish!!.startAnimation(anim)
            }
            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                val newFragment = BattleTutorialFragment.newInstance()
                newFragment.isCancelable = false
                newFragment.show(activity!!.supportFragmentManager, "dialog")
            }
        })
    }

    /** バトル開始アニメーション(2) */
    @SuppressLint("ResourceType")
    fun battleStartAnimation2(): List<Animation> {
        val swordAnim1 = AnimationUtils.loadAnimation(context!!, R.animator.sword_left_out)
        val swordAnim2 = AnimationUtils.loadAnimation(context!!, R.animator.sword_right_out)
        return listOf(swordAnim1, swordAnim2)
    }

    /** バトル終了アニメーション */
    @SuppressLint("ResourceType")
    fun battleEndAnimation(): List<Animation> {
        textStartFinish!!.text = resources.getString(R.string.battle_finish)
        val swordAnim1 = AnimationUtils.loadAnimation(context!!, R.animator.sword_left_in)
        val swordAnim2 = AnimationUtils.loadAnimation(context!!, R.animator.sword_right_in)
        return listOf(swordAnim1, swordAnim2)
    }

    /** 全ての変数を初期化 */
    fun allInit() {
        sword1!!.setImageResource(0)
        sword1!!.setImageDrawable(null)
        sword1 = null
        sword2!!.setImageResource(0)
        sword2!!.setImageDrawable(null)
        sword2 = null
        textStartFinish = null
        dummyImage!!.setImageResource(0)
        dummyImage!!.setImageDrawable(null)
        dummyImage = null
        layoutStartFinish = null
    }

}
