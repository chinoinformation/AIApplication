package com.example.mitake.aiapplication.character

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Bundle
import android.view.*
import com.bumptech.glide.Glide
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.bgm.MyService
import com.example.mitake.aiapplication.data.DataManagement
import com.example.mitake.aiapplication.home.MainButtonFragment
import kotlinx.android.synthetic.main.activity_character.*


class CharacterActivity : AppCompatActivity() {

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    /** BGM再生 */
    private var bgmId = R.raw.bgm_home
    private var bgmFlag = 0
    private var am: AudioManager? = null
    private var mVol: Float = 0f
    private var ringVolume: Float = 0f

    /** プリファレンス */
    private var data: DataManagement? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character)

        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        char_container.adapter = mSectionsPagerAdapter
        char_container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(char_tabs))
        char_tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(char_container))

        // タブ間のスペース
        val betweenSpace = 15
        val slidingTabStrip = char_tabs.getChildAt(0) as ViewGroup
        for (i in 0 until slidingTabStrip.childCount - 1) {
            val v = slidingTabStrip.getChildAt(i)
            val params = v.layoutParams as ViewGroup.MarginLayoutParams
            params.rightMargin = betweenSpace
        }

        // ボタンのイラスト変更
        val mainButton = MainButtonFragment()
        val t = supportFragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putInt("musicId", bgmId)
        bundle.putString("activity", "Character")
        mainButton.arguments = bundle
        t.add(R.id.char_main_button, mainButton, "main_button")
        t.commit()
    }

    /** 音量設定 */
    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {

        if (event.keyCode === KeyEvent.KEYCODE_VOLUME_UP) {
            // 現在の音量を取得する
            ringVolume = am!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
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
            val bgmLevel = data!!.readData("bgmLevel", "1")[0].toFloat()
            val bgmVol = bgmLevel * ringVolume
            val intent = Intent(applicationContext, MyService::class.java)
            intent.putExtra("flag", 3)
            intent.putExtra("bgmLevel", bgmVol)
            startService(intent)
        }
        return super.dispatchKeyEvent(event)
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return when (position){
                0 -> OrganizationFragment.newInstance()
                else -> CharListFragment.newInstance()
            }
        }

        override fun getCount(): Int {
            // Show 2 total pages.
            return 2
        }
    }

    override fun onResume() {
        super.onResume()
        // プレイヤーの処理
        bgmId = R.raw.bgm_home
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
            // プリファレンスの呼び出し
            data = DataManagement(this)
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

        data = null
        am = null
        mVol = 0f
        ringVolume = 0f
        Glide.get(this).clearMemory()
    }

    override fun onBackPressed() {}
}
