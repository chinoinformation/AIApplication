package com.example.mitake.aiapplication.home

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.bgm.EffectList
import com.example.mitake.aiapplication.bgm.MyService
import com.example.mitake.aiapplication.data.DataManagement
import kotlinx.android.synthetic.main.site.view.*

@Suppress("DEPRECATION")
class SiteDialogFragment: DialogFragment() {
    private var dialog: AlertDialog? = null
    private var alert: AlertDialog.Builder? = null

    /** BGM再生 */
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null

    /** プリファレンス */
    private var data: DataManagement? = null

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
        alert = AlertDialog.Builder(activity)

        // プリファレンスの呼び出し
        data = DataManagement(activity!!.applicationContext)

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

        // カスタムレイアウトの生成
        val alertView = activity.layoutInflater.inflate(R.layout.site, null)
        alertView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        // テキストの更新
        alertView.site.text = Html.fromHtml(resources.getString(R.string.site))

        // OKボタンを押す
        val OKButton: Button = alertView.findViewById(R.id.OK)
        OKButton.setOnClickListener {
            ringVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
            effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
            effectBgm!!.play("other_button")
            getDialog().dismiss()
        }


        // ViewをAlertDialog.Builderに追加
        alert!!.setView(alertView)

        // Dialogを生成
        dialog = alert!!.create()
        dialog!!.setOnKeyListener({ _, keyCode, _ ->
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
        dialog!!.show()

        return dialog
    }

    companion object {
        fun newInstance(): SiteDialogFragment {
            // Fragment インスタンス生成
            val siteDialogFragment = SiteDialogFragment()
            return siteDialogFragment
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Handler().postDelayed({
            effectBgm!!.release()
        }, 1000)

        alert = null
        dialog = null
        data = null
    }

}