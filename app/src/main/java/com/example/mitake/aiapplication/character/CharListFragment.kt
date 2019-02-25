package com.example.mitake.aiapplication.character

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import com.example.mitake.aiapplication.GlideAnim
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.bgm.EffectList
import com.example.mitake.aiapplication.custom_layout.*
import com.example.mitake.aiapplication.custom_layout.character.CharData
import com.example.mitake.aiapplication.custom_layout.character.CharListAdapter
import com.example.mitake.aiapplication.custom_layout.character.CustomCharList
import com.example.mitake.aiapplication.custom_layout.character.CustomCharStatus
import com.example.mitake.aiapplication.data.DataManagement


@Suppress("DEPRECATION")
class CharListFragment : Fragment() {
    private var customCharList: CustomCharStatus? = null
    private var listView: ListView? = null
    private var adapter: CharListAdapter? = null
    private var parser = CsvReader()
    private var touchFlag: Int = 1
    private var listPosition: Int = 1
    private var presentNum: Int = 1

    private var glideAnim: GlideAnim? = GlideAnim()

    /** BGM再生 */
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null

    /** プリファレンス */
    private var data: DataManagement? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_char_list, container, false)

        // プリファレンスの呼び出し
        data = DataManagement(context!!)

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
        effectBgm = EffectList(activity!!.applicationContext, soundPool)
        effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
        effectBgm!!.getList("other_button")

        // csvデータを読み込み
        parser.reader(activity!!.applicationContext)
        // レイアウトからリストビューを取得
        listView = root.findViewById(R.id.char_list) as ListView
        listView!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        // リストビューに表示する要素を設定
        val listItems = arrayListOf<CustomCharList>()
        // csvファイルの最後の行はnoneキャラクターなので省く
        for (i in 1..(parser.objects.size-2)) {
            val drawName: String = "char" + i.toString() + "12"
            val drawId: Int = resources.getIdentifier(drawName, "drawable", activity!!.packageName)
            var no = parser.objects[i].id
            if (i < 10){
                no = "0" + parser.objects[i].id
            }
            val item = CustomCharList(drawId, "No. $no")
            listItems.add(item)
        }
        // 出力結果をリストビューに表示
        adapter = CharListAdapter(activity!!, R.layout.char_list, listItems)
        listView!!.adapter = adapter
        listView!!.onItemClickListener = onItemClickListener

        // ステータス画面のid取得
        customCharList = root.findViewById(R.id.custom_left_char_status)
        statusView(parser.objects[1], 1)

        // キャラクターのタッチイベント
        customCharList!!.charImg.setOnClickListener { imgTouch() }

        return root
    }

    companion object {
        fun newInstance(): CharListFragment {
            // Fragment インスタンス生成
            val charListFragment = CharListFragment()
            return charListFragment
        }
    }

    private fun statusView(data: CharData, no: Int){
        // タッチフラグのリセット
        touchFlag = 1
        // キャラ名
        customCharList!!.charName.text= data.name
        // キャラクターのタイプ
        var typeColor = resources.getColor(R.color.organization_char1_color2)
        when (data.type){
            getString(R.string.charType2) -> {
                typeColor = resources.getColor(R.color.organization_char2_color2)
            }
            getString(R.string.charType3) -> {
                typeColor = resources.getColor(R.color.organization_char3_color2)
            }
            getString(R.string.charType4) -> {
                typeColor = resources.getColor((R.color.organization_char4_color2))
            }
        }
        customCharList!!.charType.text = data.type
        customCharList!!.charType.setBackgroundColor(typeColor)
        // キャラ画像
        presentNum = no
        val bitmapName: String = "char" + presentNum.toString() + touchFlag.toString() + "1"
        val drawName: String = "char" + presentNum.toString() + "_1"
        glideAnim!!.animation(context!!, customCharList!!.charImg, bitmapName, drawName)
        // ステータス
        customCharList!!.customStatus!!.hp.text = data.HP
        customCharList!!.customStatus!!.mp.text = data.MP
        customCharList!!.customStatus!!.attack.text = data.attack
        customCharList!!.customStatus!!.defense.text = data.defense
        customCharList!!.customStatus!!.move.text = data.move
        customCharList!!.customStatus!!.attackRange.text = data.attackRange
    }

    /** タップしたキャラのステータス取得 */
    private val onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
        // AudioManagerを取得する
        val am = activity!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // 最大音量値を取得
        val mVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
        // 現在の音量を取得する
        val ringVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
        effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
        effectBgm!!.play("other_button")
        statusView(parser.objects[position+1], position+1)
        listPosition = position + 1
    }

    private fun imgTouch(){
        touchFlag += 1
        if (touchFlag > 4) touchFlag = 1
        val bitmapName: String = "char" + presentNum.toString() + touchFlag.toString() + "1"
        val drawName: String = "char" + (listPosition).toString() + "_" + touchFlag.toString()
        glideAnim!!.animation(context!!, customCharList!!.charImg, bitmapName, drawName)
    }

    override fun onDestroy() {
        super.onDestroy()

        effectBgm!!.release()
        listView!!.onItemClickListener = null
        adapter = null
        listView!!.adapter = null
        listView!!.setBackgroundResource(0)
        listView = null
        customCharList!!.charImg.setOnClickListener(null)
        customCharList!!.charImg.setImageResource(0)
        customCharList!!.charImg.setImageDrawable(null)
        customCharList = null

        glideAnim = null
        data = null
    }

}
