package com.example.mitake.aiapplication.home

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.mitake.aiapplication.character.CharacterActivity
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.battle.BattleActivity
import com.example.mitake.aiapplication.bgm.MyService
import com.example.mitake.aiapplication.custom_layout.Loading
import com.example.mitake.aiapplication.quest.*
import com.example.mitake.aiapplication.start.MainActivity
import com.example.mitake.aiapplication.start.OpeningActivity
import com.example.mitake.aiapplication.start.OpeningStoryActivity

class IntentActivity : AppCompatActivity() {

    /** BGM再生 */
    private var bgmId: Int = 0
    private var bgmFlag: Int = 0

    private var loading: Loading? = null
    private lateinit var activityName: String
    private var time: Long = 1500

    private val mHandler = Handler()
    private var updateText: Runnable? = null
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intent)
        loading = Loading(this)
        activityName = intent.getStringExtra("Name")
        bgmId = intent.getIntExtra("musicId", R.raw.bgm_home)
    }

    /** 画面遷移する関数 */
    private fun ActivityTransition(name: String){
        if (name == "Battle"){
            time = 3500
        }
        if (name == "Main" || name == "OpeningStory" || name == "Battle") loading!!.show()
        // 画面遷移処理
        updateText = Runnable {
            if (count > 0){
                mHandler.removeCallbacks(updateText)
            } else {
                count = 1
                when (name) {
                // Loading画面とともに HomeActivity に遷移させる
                    "Main" -> {
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.animator.open_enter_fade_in, R.animator.open_exit_fade_out)
                        finish()
                    }
                // Loading画面とともに OpeningActivity に遷移させる
                    "Opening" -> {
                        val intent = Intent(this, OpeningActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.animator.open_enter_fade_in, R.animator.open_exit_fade_out)
                        finish()
                    }
                // Loading画面とともに OpeningStoryActivity に遷移させる
                    "OpeningStory" -> {
                        val intent = Intent(this, OpeningStoryActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.animator.open_enter_fade_in, R.animator.open_exit_fade_out)
                        finish()
                    }
                // HomeActivity に遷移させる
                    "Home" -> {
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                // CharacterActivity に遷移させる
                    "Character" -> {
                        val intent = Intent(this, CharacterActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                // SettingActivity に遷移させる
                    "Menu" -> {
                        val intent = Intent(this, SettingActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                // QuestActivity に遷移させる
                    "Quest" -> {
                        val intent = Intent(this, QuestActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                // MainQuestActivity に遷移させる
                    "MainQuest" -> {
                        val intent = Intent(this, MainQuestActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                // TutorialActivity に遷移させる
                    "TutorialQuest" -> {
                        val intent = Intent(this, TutorialActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                // TutorialActivity に遷移させる
                    "Practice" -> {
                        val intent = Intent(this, PracticeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                // BattleActivity に遷移させる
                    "Battle" -> {
                        // 画面遷移
                        val intent = Intent(this, BattleActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.animator.open_enter_fade_in, R.animator.open_exit_fade_out)
                        finish()
                    }
                // StoryActivity に遷移させる
                    "Story" -> {
                        val intent = Intent(this, StoryActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.animator.open_enter_fade_in, R.animator.open_exit_fade_out)
                        finish()
                    }
                // 例外処理：Loading画面とともに MainActivity に遷移させる
                    else -> {
                        loading!!.show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.animator.open_enter_fade_in, R.animator.open_exit_fade_out)
                        finish()
                    }
                }
                mHandler.removeCallbacks(updateText)
                mHandler.postDelayed(updateText, time)
            }
        }
        mHandler.postDelayed(updateText, time)
    }

    override fun onPause() {
        super.onPause()

        if (!isFinishing) {
            val intent = Intent(applicationContext, MyService::class.java)
            intent.putExtra("id", bgmId)
            intent.putExtra("flag", 1)
            startService(intent)
        }

        mHandler.removeCallbacks(updateText)
    }

    override fun onResume() {
        super.onResume()

        // プレイヤーの処理
        val intent = Intent(applicationContext, MyService::class.java)
        intent.putExtra("id", bgmId)
        if (bgmFlag == 0) {
            //プレイヤーの初期化
            intent.putExtra("flag", 0)
            startService(intent)
            bgmFlag = 1
        } else {
            intent.putExtra("flag", 2)
            startService(intent)
        }

        count = 0
        ActivityTransition(activityName)
    }

    override fun onDestroy() {
        super.onDestroy()
        loading!!.close()
        loading = null
    }

    override fun onBackPressed() {}
}
