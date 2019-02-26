package com.example.mitake.aiapplication.start

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
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.bgm.EffectList
import com.example.mitake.aiapplication.data.DataManagement

class InputUserNameFragment: DialogFragment() {
    private var dialog: AlertDialog? = null
    private var alert: AlertDialog.Builder? = null

    /** BGM再生 */
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null
    private var am: AudioManager? = null
    private var mVol: Float = 0f
    private var ringVolume: Float = 0f

    /** ユーザ名 */
    private var userName: String = ""

    private var data: DataManagement? = null

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
        alert = AlertDialog.Builder(activity)

        // プリファレンスの呼び出し
        data = DataManagement(activity!!.applicationContext)

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
        effectBgm = EffectList(activity!!, soundPool)
        effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
        effectBgm!!.getList("other_button")

        // カスタムレイアウトの生成
        val alertView = activity.layoutInflater.inflate(R.layout.fragment_input_user_name, null)
        alertView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        // ユーザ名設定
        val editUser: EditText = alertView.findViewById(R.id.input_user_name)

        // OKボタンを押す
        val OKButton: Button = alertView.findViewById(R.id.OK)
        OKButton.setOnClickListener {
            ringVolume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
            effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
            effectBgm!!.play("other_button")
            if (editUser.length() > 0) {
                getDialog().dismiss()
            }
        }

        // キャンセルボタンを押す
        val cancelButton: Button = alertView.findViewById(R.id.cancel)
        cancelButton.setOnClickListener {
            ringVolume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
            effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
            effectBgm!!.play("other_button")
            editUser.editableText.clear()
        }


        // ViewをAlertDialog.Builderに追加
        alert!!.setView(alertView)

        // Dialogを生成
        dialog = alert!!.create()
        dialog!!.show()

        return dialog
    }

    companion object {
        fun newInstance(): InputUserNameFragment {
            // Fragment インスタンス生成
            val inputUserNameFragment = InputUserNameFragment()
            return inputUserNameFragment
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        dialog = null
        alert = null
        data = null
        am = null
        mVol = 0f
        ringVolume = 0f
        Handler().postDelayed({
            effectBgm!!.release()
        }, 2000)
    }

}