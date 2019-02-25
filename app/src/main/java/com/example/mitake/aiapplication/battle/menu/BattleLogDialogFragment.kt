package com.example.mitake.aiapplication.battle.menu

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.bgm.EffectList
import com.example.mitake.aiapplication.data.DataManagement
import kotlinx.android.synthetic.main.log.view.*

@Suppress("DEPRECATION")
class BattleLogDialogFragment: DialogFragment() {
    private var dialog: AlertDialog? = null
    private var alert: AlertDialog.Builder? = null

    /** BGM再生 */
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null

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
        val ringVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol

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

        val args = arguments
        val log = args.getString("log")

        // カスタムレイアウトの生成
        val alertView = activity.layoutInflater.inflate(R.layout.log, null)

        // テキストの更新
        alertView.log.text = Html.fromHtml(log)
        alertView.scroll_log.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        alertView.scroll_log.post({
            run{ alertView.scroll_log.fullScroll(ScrollView.FOCUS_DOWN) }
        })

        // OKボタンを押す
        val OKButton: Button = alertView.findViewById(R.id.OK)
        OKButton.setOnClickListener {
            effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
            effectBgm!!.play("other_button")
            getDialog().dismiss()
        }

        // ViewをAlertDialog.Builderに追加
        alert!!.setView(alertView)

        // Dialogを生成
        dialog = alert!!.create()
        dialog!!.show()

        return dialog
    }

    companion object {
        fun newInstance(log: String): BattleLogDialogFragment {
            // Fragment インスタンス生成
            val battleLogDialogFragment = BattleLogDialogFragment()

            // Bundle にパラメータを設定
            val args = Bundle()
            args.putString("log", log)
            battleLogDialogFragment.arguments = args

            return battleLogDialogFragment
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        dialog = null
        alert = null
        data = null
        Handler().postDelayed({
            effectBgm!!.release()
        }, 2000)
    }

}