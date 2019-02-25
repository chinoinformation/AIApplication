package com.example.mitake.aiapplication.home


import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.bgm.EffectList
import com.example.mitake.aiapplication.custom_layout.CsvReader
import com.example.mitake.aiapplication.data.DataManagement

class SettingUserFragment: Fragment() {

    /** BGM再生 */
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null
    private var am: AudioManager? = null
    private var mVol: Float = 0f
    private var ringVolume: Float = 0f

    private var mainChar: ImageView? = null

    private var userButton: EditText? = null
    private var commentButton: EditText? = null
    private var OKButton: Button? = null

    private var parser: CsvReader? = null
    private var partyNum: Int = 0
    private var charId: MutableList<Int> = mutableListOf(1,5,9,13)
    private var charList: MutableList<ImageView?> = mutableListOf(null, null, null, null)

    /** プリファレンス */
    private var data: DataManagement? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_setting_user, container, false)

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

        mainChar = root.findViewById(R.id.main_char)
        mainChar!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        userButton = root.findViewById(R.id.user_name)
        commentButton = root.findViewById(R.id.comment)
        OKButton = root.findViewById(R.id.OK)

        // プリファレンス
        data = DataManagement(activity!!)
        // csvデータを読み込み
        parser = CsvReader()
        parser!!.reader(activity!!)

        for (i in 0 until charList.size){
            val id = root.resources.getIdentifier("char" + (i+1).toString(), "id", activity!!.packageName)
            charList[i] = root.findViewById(id)
            charList[i]!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // キャラデータ読み込み
        partyNum = data!!.readData("organization_page", "0")[0].toInt()
        val key = "char_organization_status" + (partyNum+1).toString()
        var values = ""
        for (i in 0 until charId.size){
            values += when {
                i < charId.size-1 -> charId[i].toString() + ","
                else -> charId[i].toString()
            }
        }
        for (i in 0 until charId.size){
            val id = parser!!.objects[data!!.readData(key, values)[i].toInt()].id
            val charName = "char" + id + "12"
            val charId = view.resources.getIdentifier(charName, "drawable", activity!!.packageName)
            Glide.with(activity!!.applicationContext).load(charId).into(charList[i]!!)
        }

        // 枠線作成
        Glide.with(activity!!.applicationContext).load(R.drawable.ririi_1).apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565)).apply(RequestOptions().transform(CircleCrop())).into(mainChar!!)
        mainChar!!.setBackgroundResource(R.drawable.icon_round)

        OKButton!!.setOnClickListener {
            effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
            effectBgm!!.play("other_button")
            OKButton!!.setOnClickListener(null)
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

        for (i in 0 until charList.size){
            charList[i]!!.setImageResource(0)
            charList[i]!!.setImageDrawable(null)
        }
        mainChar!!.setImageResource(0)
        mainChar!!.setImageDrawable(null)
        mainChar = null
        userButton!!.setOnClickListener(null)
        userButton!!.setBackgroundResource(0)
        userButton = null
        commentButton!!.setOnClickListener(null)
        commentButton!!.setBackgroundResource(0)
        commentButton = null
        OKButton!!.setOnClickListener(null)
        OKButton!!.setBackgroundResource(0)
        OKButton = null

        data= null
        parser = null
        partyNum = 0
        charId = mutableListOf()
        charList = mutableListOf()
        am = null
        mVol = 0f
        ringVolume = 0f
    }

}