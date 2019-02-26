package com.example.mitake.aiapplication.home

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.bgm.EffectList
import android.widget.SeekBar.OnSeekBarChangeListener
import com.example.mitake.aiapplication.bgm.MyService
import com.example.mitake.aiapplication.data.DataManagement


class SettingAudioFragment: Fragment() {

    /** BGM再生 */
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null
    private var am: AudioManager? = null
    private var mVol: Float = 0f
    private var ringVolume: Float = 0f

    private var bgmSettingBar: SeekBar? = null
    private var bgmProgress: Int = 0
    private var effectSettingBar: SeekBar? = null
    private var effectProgress: Int = 0
    private var backButton: Button? = null

    private var data: DataManagement? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_setting_audio, container, false)

        // プリファレンスの呼び出し
        data = DataManagement(context!!)

        // AudioManagerを取得する
        am = activity!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // 最大音量値を取得
        mVol = am!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
        // 現在の音量を取得する
        ringVolume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
        // SoundPool の設定
        audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        soundPool = SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(2)
                .build()
        effectBgm = EffectList(activity!!.applicationContext, soundPool)
        effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
        effectBgm!!.getList("other_button")

        bgmSettingBar = root.findViewById(R.id.bgmVolSeekBar)
        bgmSettingBar!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        effectSettingBar = root.findViewById(R.id.effectVolSeekBar)
        effectSettingBar!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        backButton = root.findViewById(R.id.OK)
        backButton!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bgmSettingBar!!.progress = (data!!.readData("bgmLevel", "1")[0].toFloat() * 100.0).toInt()
        effectSettingBar!!.progress = (data!!.readData("effectLevel", "1")[0].toFloat() * 100.0).toInt()

        // BGMの音量設定
        bgmSettingBar!!.setOnSeekBarChangeListener(
                object : OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar,
                                                   progress: Int, fromUser: Boolean) {
                        bgmProgress = progress
                        val bgmLevel = bgmProgress.toFloat() / 100
                        val bgmVol = bgmLevel * ringVolume
                        data!!.saveData("bgmLevel", bgmLevel.toString())
                        val intent = Intent(activity!!.applicationContext, MyService::class.java)
                        intent.putExtra("flag", 3)
                        intent.putExtra("bgmLevel", bgmVol)
                        activity!!.startService(intent)
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                }
        )

        // エフェクトの音量設定
        effectSettingBar!!.setOnSeekBarChangeListener(
                object : OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar,
                                                   progress: Int, fromUser: Boolean) {
                        effectProgress = progress
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        val effectLevel = effectProgress.toFloat() / 100
                        val effectVol = effectLevel * ringVolume
                        data!!.saveData("effectLevel", effectLevel.toString())
                        effectBgm!!.setVol(effectVol)
                    }
                }
        )

        backButton!!.setOnClickListener {
            ringVolume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
            effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
            effectBgm!!.play("other_button")
            backButton!!.setOnClickListener(null)
            // FragmentManagerからFragmentTransactionを作成
            val fragmentManager = activity!!.supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.setCustomAnimations(
                    R.animator.act_open_enter_anim,
                    R.animator.act_open_exit_anim)
            // 実際に使用するFragmentの作成
            val newFragment = SettingButtonFragment()
            // Fragmentを組み込む
            transaction.replace(R.id.setting_container, newFragment)
            // backstackに追加
            transaction.addToBackStack(null)
            // 上記の変更を反映する
            transaction.commit()
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        effectBgm!!.release()
        backButton!!.setOnClickListener(null)
        backButton!!.setBackgroundResource(0)
        backButton = null
        data = null
        am = null
        mVol = 0f
        ringVolume = 0f
    }

}