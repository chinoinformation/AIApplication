package com.example.mitake.aiapplication.battle.menu

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.app.DialogFragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.bumptech.glide.Glide
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.battle.BattleActivity
import com.example.mitake.aiapplication.bgm.EffectList
import com.example.mitake.aiapplication.custom_layout.battle.CustomBattleTutorialAdapter
import kotlinx.android.synthetic.main.tutorial.view.*

class BattleTutorialFragment: DialogFragment() {
    private var mViewPager: ViewPager? = null
    private var OKButton: Button? = null

    private var mainActivity: BattleActivity? = null

    /** BGM再生 */
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.tutorial, container, false)

        // SoundPool の設定
        audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        soundPool = SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(2)
                .build()
        effectBgm = EffectList(activity!!, soundPool)
        effectBgm!!.getList("other_button")

        mViewPager = root.findViewById(R.id.battle_tutorial_pager) as ViewPager
        mViewPager!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        mViewPager!!.adapter = CustomBattleTutorialAdapter(childFragmentManager)
        val tabLayout = root.battle_tutorial_indicator as TabLayout
        tabLayout.setupWithViewPager(mViewPager!!, true)

        mainActivity = (activity as BattleActivity)

        // OKボタン
        OKButton = root.findViewById(R.id.OK)
        OKButton!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // OKボタン
        OKButton!!.setOnClickListener {
            effectBgm!!.play("other_button")
            OKButton!!.setOnClickListener(null)
            mainActivity!!.initialCount = 2
            mainActivity = null
            dialog.dismiss()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setCanceledOnTouchOutside(false)
    }

    companion object {
        fun newInstance(): BattleTutorialFragment {
            // Fragment インスタンス生成
            val battleTutorialFragment = BattleTutorialFragment()
            return battleTutorialFragment
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        OKButton!!.setOnClickListener(null)
        OKButton!!.setBackgroundResource(0)
        mViewPager!!.adapter = null
        mViewPager!!.setBackgroundResource(0)
        mViewPager = null
        mainActivity = null

        Glide.get(context!!).clearMemory()
        Handler().postDelayed({
            effectBgm!!.release()
        }, 2000)
    }

}