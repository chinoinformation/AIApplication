package com.example.mitake.aiapplication.custom_layout.quest

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.app.DialogFragment
import android.support.v4.view.ViewPager
import android.view.*
import android.widget.Button
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.bgm.EffectList
import com.example.mitake.aiapplication.bgm.MyService
import com.example.mitake.aiapplication.data.DataManagement
import kotlinx.android.synthetic.main.detail_ai.view.*

class DetailAIDialogFragment: DialogFragment() {
    private var mViewPager: ViewPager? = null
    private var OKButton: Button? = null

    /** BGM再生 */
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null

    /** プリファレンス */
    private var data: DataManagement? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.detail_ai, container, false)

        // プリファレンスの呼び出し
        data = DataManagement(context!!)

        // AudioManagerを取得する
        val am = activity!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // 最大音量値を取得
        val mVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
        // 現在の音量を取得する
        var ringVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
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
        effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
        effectBgm!!.getList("other_button")

        mViewPager = root.findViewById(R.id.ai_pager) as ViewPager
        mViewPager!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        mViewPager!!.adapter = CustomPointAIPagerAdapter(childFragmentManager)
        val tabLayout = root.ai_indicator as TabLayout
        tabLayout.setupWithViewPager(mViewPager!!, true)

        // OKボタン
        OKButton = root.findViewById(R.id.OK)
        OKButton!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        // OKボタンを押す
        OKButton!!.setOnClickListener {
            OKButton!!.setOnClickListener(null)
            ringVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
            effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
            effectBgm!!.play("other_button")
            OKButton!!.setOnClickListener(null)
            dialog.dismiss()
        }

        root.setOnKeyListener({ _, keyCode, _ ->
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    // 現在の音量を取得する
                    ringVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
                    effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat() * ringVolume)
                    val bgmLevel = data!!.readData("bgmLevel", "1")[0].toFloat()
                    val bgmVol = bgmLevel * ringVolume
                    val intent = Intent(activity!!.applicationContext, MyService::class.java)
                    intent.putExtra("flag", 3)
                    intent.putExtra("bgmLevel", bgmVol)
                    activity!!.startService(intent)
                    false
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    // 現在の音量を取得する
                    ringVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
                    effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
                    val bgmLevel = data!!.readData("bgmLevel", "1")[0].toFloat()
                    val bgmVol = bgmLevel * ringVolume
                    val intent = Intent(activity!!.applicationContext, MyService::class.java)
                    intent.putExtra("flag", 3)
                    intent.putExtra("bgmLevel", bgmVol)
                    activity!!.startService(intent)
                    false
                }
                else -> true
            }
        })
        root.isFocusableInTouchMode = true

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setCanceledOnTouchOutside(false)
    }

    override fun onDestroy() {
        super.onDestroy()

        OKButton!!.setOnClickListener(null)
        OKButton!!.setBackgroundResource(0)
        mViewPager!!.adapter = null
        mViewPager!!.setBackgroundResource(0)
        mViewPager = null
        data = null
        Handler().postDelayed({
            effectBgm!!.release()
        }, 2000)
    }

    companion object {
        fun newInstance(): DetailAIDialogFragment {
            // Fragment インスタンス生成
            val detailAIDialogFragment = DetailAIDialogFragment()
            return detailAIDialogFragment
        }
    }

}