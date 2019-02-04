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
import kotlinx.android.synthetic.main.attack_ability_dialog.view.*

class AttackAbilityDialogFragment: DialogFragment() {
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
        val attackName = args.getString("attackName")
        val attackType = args.getString("attackType")
        val charType = args.getString("charType")
        val effectName = args.getString("effectName")

        // カスタムレイアウトの生成
        val alertView = activity.layoutInflater.inflate(R.layout.attack_ability_dialog, null)
        alertView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        // テキストの更新
        alertView.attack_name.text = attackName
        alertView.attack_type.text = attackType
        alertView.char_type.text = charType
        alertView.effect_name.text = effectName

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
        fun newInstance(attackName: String, attackType: String, charType: String, effectName: String): AttackAbilityDialogFragment {
            // Fragment インスタンス生成
            val attackAbilityDialogFragment = AttackAbilityDialogFragment()

            // Bundle にパラメータを設定
            val args = Bundle()
            args.putString("attackName", attackName)
            args.putString("attackType", attackType)
            args.putString("charType", charType)
            args.putString("effectName", effectName)
            attackAbilityDialogFragment.arguments = args

            return attackAbilityDialogFragment
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