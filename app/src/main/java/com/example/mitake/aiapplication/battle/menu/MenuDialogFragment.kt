package com.example.mitake.aiapplication.battle.menu

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DialogFragment
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.View
import android.widget.Button
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.battle.BattleActivity
import com.example.mitake.aiapplication.bgm.EffectList
import com.example.mitake.aiapplication.home.IntentActivity
import kotlinx.android.synthetic.main.menu.view.*


@Suppress("DEPRECATION")
class MenuDialogFragment: DialogFragment() {
    private var dialog: AlertDialog? = null
    private var alert: AlertDialog.Builder? = null

    private var mainActivity: BattleActivity? = null

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

        mainActivity = (activity as BattleActivity)

        val args = arguments
        val questName = args.getString("questName")
        val maxTurn = args.getInt("maxTurn")
        val turn = args.getInt("turn")
        val log = args.getString("log")

        // カスタムレイアウトの生成
        val alertView = activity.layoutInflater.inflate(R.layout.menu, null)
        alertView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        // テキストの更新
        alertView.quest_name.text = questName
        alertView.menu_turn.text = Html.fromHtml(getString(R.string.menu_turn, turn, maxTurn))

        // 戦闘ログボタンを押す
        val battleLog: Button = alertView.findViewById(R.id.battle_log)
        battleLog.setOnClickListener {
            effectBgm!!.play("other_button")

            battleLog.isEnabled = false
            val newFragment = BattleLogDialogFragment.newInstance(log)
            newFragment.isCancelable = false
            newFragment.show(activity!!.fragmentManager, "dialog")

            getDialog().dismiss()
        }

        // リタイアボタンを押す
        val retire: Button = alertView.findViewById(R.id.retire)
        retire.setOnClickListener {
            effectBgm!!.play("other_button")
            // 画面遷移
            val intent = Intent(activity!!, IntentActivity::class.java)
            intent.putExtra("musicId", mainActivity!!.bgmId)
            intent.putExtra("Name", "Home")
            mainActivity = null
            startActivity(intent)
            activity!!.finish()
            // Dialogを消す
            getDialog().dismiss()
        }

        // キャンセルボタンを押す
        val cancel: Button = alertView.findViewById(R.id.cancel)
        cancel.setOnClickListener {
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
        fun newInstance(questName: String, maxTurn: Int, turn: Int, log: String): MenuDialogFragment {
            // Fragment インスタンス生成
            val menuDialogFragment = MenuDialogFragment()

            // Bundle にパラメータを設定
            val args = Bundle()
            args.putString("questName", questName)
            args.putInt("maxTurn", maxTurn)
            args.putInt("turn", turn)
            args.putString("log", log)
            menuDialogFragment.arguments = args

            return menuDialogFragment
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        mainActivity = null
        dialog = null
        alert = null
        Handler().postDelayed({
            effectBgm!!.release()
        }, 2000)
    }

}