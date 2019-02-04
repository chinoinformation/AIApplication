package com.example.mitake.aiapplication.character

import android.content.Intent
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.bgm.MyService
import com.example.mitake.aiapplication.home.MainButtonFragment
import kotlinx.android.synthetic.main.activity_character.*


class CharacterActivity : AppCompatActivity() {

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    /** BGM再生 */
    private var bgmId = R.raw.bgm_home
    private var bgmFlag = 0

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


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_character, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
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

    class PlaceholderFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_character, container, false)
            return rootView
        }

        companion object {
            private val ARG_SECTION_NUMBER = "section_number"
            fun newInstance(sectionNumber: Int): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
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

        Glide.get(this).clearMemory()
    }

    override fun onBackPressed() {}
}
