package com.example.mitake.aiapplication.quest

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.util.Log
import android.view.KeyEvent
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.battle.BattleActivity
import com.example.mitake.aiapplication.bgm.EffectList
import com.example.mitake.aiapplication.bgm.MyService
import com.example.mitake.aiapplication.custom_layout.quest.CustomQuestList
import com.example.mitake.aiapplication.custom_layout.quest.QuestListAdapter
import com.example.mitake.aiapplication.data.DataManagement
import com.example.mitake.aiapplication.home.HomeActivity
import com.example.mitake.aiapplication.home.IntentActivity


class MainQuestActivity : AppCompatActivity() {
    private var backHome: Button? = null
    private var backQuest: Button? = null

    private var listView: ListView? = null
    private var questAdapter: QuestListAdapter? = null

    // BGM再生
    private var bgmId: Int = 0
    private var bgmFlag = 0
    private var soundPool: SoundPool? = null
    private var audioAttributes: AudioAttributes? = null
    private var effectBgm: EffectList? = null
    private var am: AudioManager? = null
    private var mVol: Float = 0f
    private var ringVolume: Float = 0f

    /** プリファレンス */
    private var data: DataManagement? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_quest)

        // ホームボタン
        backHome = findViewById(R.id.back_home)
        backHome!!.setOnClickListener {
            effectBgm!!.play("button")
            backHome!!.setOnClickListener(null)
            val intent = Intent(this, IntentActivity::class.java)
            intent.putExtra("Name", "Home")
            intent.putExtra("musicId", bgmId)
            startActivity(intent)
            finish()
        }

        // ワールドマップボタン
        backQuest = findViewById(R.id.back_world)
        backQuest!!.setOnClickListener {
            effectBgm!!.play("button")
            backQuest!!.setOnClickListener(null)
            val intent = Intent(this, IntentActivity::class.java)
            intent.putExtra("Name", "Quest")
            intent.putExtra("musicId", bgmId)
            startActivity(intent)
            finish()
        }

        // レイアウトからリストビューを取得
        listView = findViewById(R.id.listview_main_quest) as ListView
        // リストビューに表示する要素を設定
        val listItems = arrayListOf<CustomQuestList>()
        for (i in 0..10) {
            val questName: String = "クエスト名"
            var questType: String = "Battle"
            val questConstraint: String = "クエスト条件"
            val victoryCondition: String = "クリア条件"
            var questBackground: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.drawable_quest_battle_background, null)
            if (i == 3 || i == 8){
                questType = "Story"
                questBackground = ResourcesCompat.getDrawable(resources, R.drawable.drawable_quest_story_background, null)
            }
            val item = CustomQuestList(questName, questType, questConstraint, victoryCondition, questBackground)
            listItems.add(item)
        }
        questAdapter = QuestListAdapter(this, R.layout.quest_view, listItems)
        listView!!.adapter = questAdapter
    }

    /** 音量設定 */
    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {

        if (event.keyCode === KeyEvent.KEYCODE_VOLUME_UP) {
            // 現在の音量を取得する
            ringVolume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
            effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
            val bgmLevel = data!!.readData("bgmLevel", "1")[0].toFloat()
            val bgmVol = bgmLevel * ringVolume
            val intent = Intent(applicationContext, MyService::class.java)
            intent.putExtra("flag", 3)
            intent.putExtra("bgmLevel", bgmVol)
            startService(intent)
        }

        if (event.keyCode === KeyEvent.KEYCODE_VOLUME_DOWN) {
            // 現在の音量を取得する
            ringVolume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
            effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
            val bgmLevel = data!!.readData("bgmLevel", "1")[0].toFloat()
            val bgmVol = bgmLevel * ringVolume
            val intent = Intent(applicationContext, MyService::class.java)
            intent.putExtra("flag", 3)
            intent.putExtra("bgmLevel", bgmVol)
            startService(intent)
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onResume() {
        super.onResume()
        // プレイヤーの処理
        bgmId = R.raw.bgm_world
        val intent = Intent(applicationContext, MyService::class.java)
        intent.putExtra("id", bgmId)
        if (bgmFlag == 0) {
            //プレイヤーの初期化
            intent.putExtra("flag", 0)
            startService(intent)
            bgmFlag = 1

            // AudioManagerを取得する
            am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
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
            effectBgm = EffectList(applicationContext, soundPool)

            // プリファレンスの呼び出し
            data = DataManagement(this)
            effectBgm!!.setVol(data!!.readData("effectLevel", "1")[0].toFloat()*ringVolume)
            effectBgm!!.getList("button")
        } else {
            intent.putExtra("flag", 2)
            startService(intent)
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isFinishing) {
            val intent = Intent(applicationContext, MyService::class.java)
            intent.putExtra("id", bgmId)
            intent.putExtra("flag", 1)
            startService(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        backHome!!.setOnClickListener(null)
        backHome!!.setBackgroundResource(0)
        backQuest!!.setOnClickListener(null)
        backQuest!!.setBackgroundResource(0)
        backHome = null
        backQuest = null

        listView!!.adapter = null
        questAdapter = null
        listView = null

        effectBgm!!.release()
        data = null
        am = null
        mVol = 0f
        ringVolume = 0f
    }

    override fun onBackPressed() {}

}
