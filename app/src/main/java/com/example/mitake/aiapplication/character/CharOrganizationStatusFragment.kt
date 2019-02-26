package com.example.mitake.aiapplication.character

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.custom_layout.CsvReader
import kotlinx.android.synthetic.main.fragment_char_organization_status.view.*
import android.support.v7.app.AlertDialog
import android.view.KeyEvent
import android.widget.ToggleButton
import com.bumptech.glide.Glide
import com.example.mitake.aiapplication.bgm.EffectList
import com.example.mitake.aiapplication.data.DataManagement
import kotlinx.android.synthetic.main.fragment_char_status.view.*
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.example.mitake.aiapplication.bgm.MyService


@Suppress("DEPRECATION")
class CharOrganizationStatusFragment : Fragment() {
    private var partyNum: Int = 0
    private var f1: View? = null
    private var f2: View? = null
    private var f3: View? = null
    private var f4: View? = null

    private var parser: CsvReader? = CsvReader()
    private var charId: MutableList<Int> = mutableListOf(1,5,9,13)
    private var aiButtonCheckList: MutableList<Int> = mutableListOf(0,0,0,0)

    private var data: DataManagement? = null
    private val key0 = "char_organization_status"

    /** BGM再生 */
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null
    private var am: AudioManager? = null
    private var mVol: Float = 0f
    private var ringVolume: Float = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_char_organization_status, container, false)

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

        f1 = root.findViewById(R.id.char1) as View
        f1!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        f1!!.setBackgroundColor(resources.getColor(R.color.organization_char1_color1))

        f2 = root.findViewById(R.id.char2) as View
        f2!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        f2!!.setBackgroundColor(resources.getColor(R.color.organization_char2_color1))

        f3 = root.findViewById(R.id.char3) as View
        f3!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        f3!!.setBackgroundColor(resources.getColor(R.color.organization_char3_color1))

        f4 = root.findViewById(R.id.char4) as View
        f4!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        f4!!.setBackgroundColor(resources.getColor(R.color.organization_char4_color1))

        // csvデータを読み込み
        parser!!.reader(activity!!.applicationContext)

        return root
    }

    companion object {
        fun newInstance(num: Int): CharOrganizationStatusFragment {
            // Fragment インスタンス生成
            val charOrganizationFragment = CharOrganizationStatusFragment()

            // Bundle にパラメータを設定
            val args = Bundle()
            args.putInt("Number", num)
            charOrganizationFragment.arguments = args

            return charOrganizationFragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments

        if (args != null) {
            val num = args.getInt("Number")
            partyNum = num
            view.party.text = Html.fromHtml(getString(R.string.party, partyNum))

            // AI適用ボタンの初期設定
            f1!!.ai_button.setOnClickListener {
                ringVolume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
                effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
                effectBgm!!.play("other_button")
                isChecked(f1!!.ai_button, 0)
            }
            f2!!.ai_button.setOnClickListener {
                ringVolume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
                effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
                effectBgm!!.play("other_button")
                isChecked(f2!!.ai_button, 1)
            }
            f3!!.ai_button.setOnClickListener {
                ringVolume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
                effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
                effectBgm!!.play("other_button")
                isChecked(f3!!.ai_button, 2)
            }
            f4!!.ai_button.setOnClickListener {
                ringVolume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
                effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
                effectBgm!!.play("other_button")
                isChecked(f4!!.ai_button, 3)
            }

            var key = key0 + "_ai_button" + partyNum.toString()
            var values = ""
            for (i in 0 until aiButtonCheckList.size){
                values += when {
                    i < aiButtonCheckList.size-1 -> aiButtonCheckList[i].toString() + ","
                    else -> aiButtonCheckList[i].toString()
                }
            }
            val aiButtonStatus = data!!.readData(key, values)
            toggleButtonCheck(f1!!.ai_button, aiButtonStatus[0].toInt())
            toggleButtonCheck(f2!!.ai_button, aiButtonStatus[1].toInt())
            toggleButtonCheck(f3!!.ai_button, aiButtonStatus[2].toInt())
            toggleButtonCheck(f4!!.ai_button, aiButtonStatus[3].toInt())

            // キャラ編成の初期設定
            // プリファレンスでデータ読み込み
            // キャラ名
            key = key0 + partyNum.toString()
            values = ""
            for (i in 0 until charId.size){
                values += when {
                    i < charId.size-1 -> charId[i].toString() + ","
                    else -> charId[i].toString()
                }
            }
            renameChar(f1!!, data!!.readData(key, values)[0].toInt())
            renameChar(f2!!, data!!.readData(key, values)[1].toInt())
            renameChar(f3!!, data!!.readData(key, values)[2].toInt())
            renameChar(f4!!, data!!.readData(key, values)[3].toInt())
            //キャラ画像
            imgChar(f1!!, parser!!.objects[data!!.readData(key, values)[0].toInt()].name, data!!.readData(key, values)[0].toInt())
            imgChar(f2!!, parser!!.objects[data!!.readData(key, values)[1].toInt()].name, data!!.readData(key, values)[1].toInt())
            imgChar(f3!!, parser!!.objects[data!!.readData(key, values)[2].toInt()].name, data!!.readData(key, values)[2].toInt())
            imgChar(f4!!, parser!!.objects[data!!.readData(key, values)[3].toInt()].name, data!!.readData(key, values)[3].toInt())
            // ステータス
            statusChar(f1!!, data!!.readData(key, values)[0].toInt())
            statusChar(f2!!, data!!.readData(key, values)[1].toInt())
            statusChar(f3!!, data!!.readData(key, values)[2].toInt())
            statusChar(f4!!, data!!.readData(key, values)[3].toInt())

            f1!!.char_img.setOnClickListener { selectChar(f1!!, 1, 4,0, false) }
            f2!!.char_img.setOnClickListener { selectChar(f2!!, 5, 8,1, true) }
            f3!!.char_img.setOnClickListener { selectChar(f3!!, 9, 12,2, true) }
            f4!!.char_img.setOnClickListener { selectChar(f4!!, 13, 16,3, true) }
        }
    }

    // キャラ選択
    private fun selectChar(v: View, s: Int, g: Int, type: Int, noneFlag: Boolean) {
        effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
        effectBgm!!.play("other_button")
        // リストを作成
        var charList: Array<String> = arrayOf()
        var charIdList: Array<Int> = arrayOf()
        val charType: Array<String> = arrayOf("＜バランス型＞", "＜攻撃型＞", "＜防御型＞", "＜スピード型＞")
        for (i in s..g){
            charList += parser!!.objects[i].name
            charIdList += i
        }
        if (noneFlag) {
            charList += parser!!.objects[parser!!.objects.size-1].name
            charIdList += parser!!.objects.size-1
        }
        charId[type] = charIdList[0]

        // ダイアログの処理
        val builder = AlertDialog.Builder(activity!!, R.style.MyAlertDialogStyle)
        val title: String = "キャラ選択 " + charType[type]
        builder.setTitle(title)
                .setCancelable(false)
                .setSingleChoiceItems(charList, 0, { dialog, whichButton ->
                    //⇒アイテムを選択した時のイベント処理
                    charId[type] = charIdList[whichButton]
                })
                .setNegativeButton("OK", { dialog, whichButton ->
                    //⇒OKボタンを押下した時のイベント処理
                    ringVolume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
                    effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
                    effectBgm!!.play("other_button")
                    // キャラ名
                    renameChar(v, charId[type])
                    // キャラ画像
                    imgChar(v, parser!!.objects[charId[type]].name, charId[type])
                    // ステータス
                    statusChar(v, charId[type])
                })
                .setPositiveButton("キャンセル", { dialog, whichButton ->
                    ringVolume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
                    effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
                    effectBgm!!.play("other_button")
                })
        val dialog = builder.create()
        dialog.setOnKeyListener({ _, keyCode, _ ->
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    // 現在の音量を取得する
                    ringVolume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
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
                    ringVolume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
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
        dialog.show()
        dialog.getButton(Dialog.BUTTON_POSITIVE).isSoundEffectsEnabled = false
        dialog.getButton(Dialog.BUTTON_NEGATIVE).isSoundEffectsEnabled = false
    }

    // キャラ名変更
    private fun renameChar(v:View, no: Int){
        v.char_text.text = parser!!.objects[no].name
    }

    // キャラ画像変更
    private fun imgChar(v:View, charName: String, no: Int){
        var drawId = 0
        if (charName != parser!!.objects[parser!!.objects.size-1].name) {
            val drawName: String = "char" + (no).toString() + "12"
            drawId = resources.getIdentifier(drawName, "drawable", activity!!.packageName)
        }
        Glide.with(context!!).load(drawId).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).into(v.char_img)
    }

    // ステータス変更
    private fun statusChar(v:View, no: Int){
        v.custom_char_organization_status.hp.text = parser!!.objects[no].HP
        v.custom_char_organization_status.mp.text = parser!!.objects[no].MP
        v.custom_char_organization_status.attack.text = parser!!.objects[no].attack
        v.custom_char_organization_status.defense.text = parser!!.objects[no].defense
        v.custom_char_organization_status.move.text = parser!!.objects[no].move
        v.custom_char_organization_status.attackRange.text = parser!!.objects[no].attackRange
    }


    private fun toggleButtonCheck(aiButton: ToggleButton, check: Int){
        aiButton.isChecked =
                when (check){
                    0 -> false
                    else -> true
                }
    }

    // トグルボタンのチェック確認
    private fun isChecked(aiButton: ToggleButton, no: Int){
        aiButtonCheckList[no] = when {
            aiButton.isChecked -> 1
            else -> 0
        }
    }

    override fun onPause() {
        super.onPause()

        // データ保存(1) キャラクター
        var key = key0 + partyNum.toString()
        var values = ""
        for (i in 0 until charId.size){
            values += when {
                i < charId.size-1 -> charId[i].toString() + ","
                else -> charId[i].toString()
            }
        }
        data!!.saveData(key, values)

        // データ保存(2) AI適用ボタン
        key = key0 + "_ai_button" + partyNum.toString()
        values = ""
        for (i in 0 until aiButtonCheckList.size){
            values += when {
                i < aiButtonCheckList.size-1 -> aiButtonCheckList[i].toString() + ","
                else -> aiButtonCheckList[i].toString()
            }
        }
        data!!.saveData(key, values)

    }

    override fun onDestroy() {
        super.onDestroy()

        effectBgm!!.release()

        f1!!.char_img.setImageResource(0)
        f1!!.char_img.setImageDrawable(null)
        f1!!.ai_button.setOnClickListener(null)
        f1!!.char_text.setBackgroundResource(0)
        f1!!.custom_char_organization_status.setBackgroundResource(0)
        f1!!.setBackgroundResource(0)
        f1 = null

        f2!!.char_img.setImageResource(0)
        f2!!.char_img.setImageDrawable(null)
        f2!!.ai_button.setOnClickListener(null)
        f2!!.char_text.setBackgroundResource(0)
        f2!!.custom_char_organization_status.setBackgroundResource(0)
        f2!!.setBackgroundResource(0)
        f2 = null

        f3!!.char_img.setImageResource(0)
        f3!!.char_img.setImageDrawable(null)
        f3!!.ai_button.setOnClickListener(null)
        f3!!.char_text.setBackgroundResource(0)
        f3!!.custom_char_organization_status.setBackgroundResource(0)
        f3!!.setBackgroundResource(0)
        f3 = null

        f4!!.char_img.setImageResource(0)
        f4!!.char_img.setImageDrawable(null)
        f4!!.ai_button.setOnClickListener(null)
        f4!!.char_text.setBackgroundResource(0)
        f4!!.custom_char_organization_status.setBackgroundResource(0)
        f4!!.setBackgroundResource(0)
        f4 = null

        parser = null
        data = null
        charId = mutableListOf()
        aiButtonCheckList = mutableListOf()
        am = null
        mVol = 0f
        ringVolume = 0f
    }

}
