package com.example.mitake.aiapplication.battle.menu

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DialogFragment
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Button
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.bgm.EffectList
import kotlinx.android.synthetic.main.char_status_dialog.view.*

class CharStatusDialogFragment: DialogFragment() {
    private var dialog: AlertDialog? = null
    private var alert: AlertDialog.Builder? = null

    /** BGM再生 */
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
        alert = AlertDialog.Builder(activity)

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

        val args = arguments
        val charName = args.getString("charName")
        val charType = args.getString("charType")
        val mapType = args.getString("mapType")
        val HP = args.getString("HP")
        val MP = args.getString("MP")
        val attack = args.getString("attack")
        val defense = args.getString("defense")
        val SP = args.getString("SP")
        val other = args.getString("other")

        // カスタムレイアウトの生成
        val alertView = activity.layoutInflater.inflate(R.layout.char_status_dialog, null)
        alertView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        // テキストの更新
        alertView.target_char_name.text = charName
        alertView.map_type.text = mapType
        alertView.char_type.text = charType
        alertView.hp.text = HP
        alertView.mp.text = MP
        alertView.attack.text = attack
        alertView.defense.text = defense
        alertView.SP.text = SP
        alertView.other.text = other

        // OKボタンを押す
        val OKButton: Button = alertView.findViewById(R.id.OK)
        OKButton.setOnClickListener {
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
        fun newInstance(charName: String, mapType: String, charType: String, HP: String, MP: String, attack: String, defense: String, SP: String, other: String): CharStatusDialogFragment {
            // Fragment インスタンス生成
            val charStatusDialogFragment = CharStatusDialogFragment()

            // Bundle にパラメータを設定
            val args = Bundle()
            args.putString("charName", charName)
            args.putString("charType", charType)
            args.putString("mapType", mapType)
            args.putString("HP", HP)
            args.putString("MP", MP)
            args.putString("attack", attack)
            args.putString("defense", defense)
            args.putString("SP", SP)
            args.putString("other", other)
            charStatusDialogFragment.arguments = args

            return charStatusDialogFragment
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        dialog = null
        alert = null
        Handler().postDelayed({
            effectBgm!!.release()
        }, 2000)
    }

}