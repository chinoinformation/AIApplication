package com.example.mitake.aiapplication.home

import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.bgm.EffectList

class SettingButtonFragment: Fragment() {
    private var bgmId = R.raw.bgm_home

    /** BGM再生 */
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null

    /** ボタン */
    private var userSettingButton: Button? = null
    private var titleButton: Button? = null
    private var otherButton: Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_setting_button, container, false)

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
        effectBgm!!.getList("other_button")

        userSettingButton = root.findViewById(R.id.user)
        titleButton = root.findViewById(R.id.title)
        otherButton = root.findViewById(R.id.others)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ユーザ設定画面へ
        userSettingButton!!.setOnClickListener {
            effectBgm!!.play("other_button")
            userSettingButton!!.setOnClickListener(null)
            // FragmentManagerからFragmentTransactionを作成
            val fragmentManager = activity!!.supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.setCustomAnimations(
                    R.animator.act_open_enter_anim,
                    R.animator.act_open_exit_anim)
            // 実際に使用するFragmentの作成
            val newFragment = SettingUserFragment()
            // Fragmentを組み込む
            transaction.replace(R.id.setting_container, newFragment)
            // backstackに追加
            transaction.addToBackStack(null)
            // 上記の変更を反映する
            transaction.commit()
        }

        // タイトル画面に戻る
        titleButton!!.setOnClickListener {
            effectBgm!!.play("other_button")
            titleButton!!.setOnClickListener(null)
            val intent = Intent(activity!!, IntentActivity::class.java)
            intent.putExtra("Name", "else")
            intent.putExtra("musicId", bgmId)
            startActivity(intent)
            activity!!.overridePendingTransition(0, 0)
            activity!!.finish()
        }

        // その他画面
        otherButton!!.setOnClickListener {
            effectBgm!!.play("other_button")
            otherButton!!.isEnabled = false
            val newFragment = SiteDialogFragment.newInstance()
            newFragment.isCancelable = false
            newFragment.show(activity!!.fragmentManager, "dialog")
            Handler().postDelayed({
                otherButton!!.isEnabled = true
            }, 750)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        effectBgm!!.release()
        userSettingButton!!.setOnClickListener(null)
        userSettingButton!!.setBackgroundResource(0)
        userSettingButton = null
        titleButton!!.setOnClickListener(null)
        titleButton!!.setBackgroundResource(0)
        titleButton = null
        otherButton!!.setOnClickListener(null)
        otherButton!!.setBackgroundResource(0)
        otherButton = null
        effectBgm!!.release()
    }

}