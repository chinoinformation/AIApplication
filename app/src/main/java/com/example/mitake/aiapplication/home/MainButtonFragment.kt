package com.example.mitake.aiapplication.home

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.AudioAttributes
import android.media.SoundPool
import android.support.v4.content.res.ResourcesCompat
import android.widget.LinearLayout
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.bgm.EffectList
import com.example.mitake.aiapplication.character.CharacterActivity
import kotlinx.android.synthetic.main.fragment_main_button.view.*


@Suppress("DEPRECATION")
class MainButtonFragment : Fragment() {
    /** BGM再生 */
    private var bgmId: Int = 0
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null

    var density: Float = 0f
    var offDrawable: GradientDrawable? = GradientDrawable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val root = inflater.inflate(R.layout.fragment_main_button, container, false)

        // 枠線作成
        density = resources.displayMetrics.density
        offDrawable!!.setStroke((2.0f * density + 0.5f).toInt(), resources.getColor(R.color.colorIcon)) // 2dp
        offDrawable!!.setColor(Color.parseColor("#d3d3d3"))
        offDrawable!!.cornerRadius = 6.0f * density + 0.5f // 6dp

        root.main_home_button.background = offDrawable
        root.main_char_button.background = offDrawable
        root.main_ai_button.background = offDrawable
        root.main_menu_button.background = offDrawable

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
        effectBgm!!.getList("button")

        bgmId = arguments!!.getInt("musicId", 0)
        val activityName = arguments!!.getString("activity")
        val btnColor = ResourcesCompat.getDrawable(resources, R.drawable.button_on_background, null)
        when (activityName){
            "Home" -> {
                root.main_home_button.background = btnColor
                root.main_home_button.setTextColor(Color.WHITE)
                root.main_home_button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_on_home,0,0)
            }
            "Character" -> {
                root.main_char_button.background = btnColor
                root.main_char_button.setTextColor(Color.WHITE)
                root.main_char_button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_on_char,0,0)
            }
            "AI" -> {
                root.main_ai_button.background = btnColor
                root.main_ai_button.setTextColor(Color.WHITE)
                root.main_ai_button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_on_ai,0,0)
            }
            else -> {
                root.main_menu_button.background = btnColor
                root.main_menu_button.setTextColor(Color.WHITE)
                root.main_menu_button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_on_menu,0,0)
            }
        }

        /** 各種ボタンのリスナーを設定 */
        /** AI部分のみ削除 */
        root.main_home_button.setOnClickListener {
            if (activity!!::class.java != HomeActivity::class.java) {
                effectBgm!!.play("button")
                val intent = Intent(activity, IntentActivity::class.java)
                intent.putExtra("Name", "Home")
                intent.putExtra("musicId", bgmId)
                startActivity(intent)
                activity!!.finish()
            }
        }
        root.main_char_button.setOnClickListener {
            if (activity!!::class.java != CharacterActivity::class.java) {
                effectBgm!!.play("button")
                val intent = Intent(activity, IntentActivity::class.java)
                intent.putExtra("Name", "Character")
                intent.putExtra("musicId", bgmId)
                startActivity(intent)
                activity!!.finish()
            }
        }
        root.main_menu_button.setOnClickListener {
            if (activity!!::class.java != SettingActivity::class.java) {
                effectBgm!!.play("button")
                val intent = Intent(activity, IntentActivity::class.java)
                intent.putExtra("Name", "Menu")
                intent.putExtra("musicId", bgmId)
                startActivity(intent)
                activity!!.finish()
            }
        }
        return root
    }

    /** ボタンを無効化 */
    fun buttonNotEnabled(view: LinearLayout){
        view.main_home_button.isEnabled = false
        view.main_char_button.isEnabled = false
        view.main_ai_button.isEnabled = false
        view.main_menu_button.isEnabled = false
    }

    /** ボタンを有効化 */
    fun buttonEnabled(view: LinearLayout){
        view.main_home_button.isEnabled = true
        view.main_char_button.isEnabled = true
        view.main_ai_button.isEnabled = true
        view.main_menu_button.isEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()


        effectBgm!!.release()
        offDrawable = null
        density = 0f
    }

}
