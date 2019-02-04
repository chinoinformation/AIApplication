package com.example.mitake.aiapplication.home


import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.bgm.MyService

class SettingActivity : AppCompatActivity() {
    /** BGM再生 */
    private var bgmId = R.raw.bgm_home
    private var bgmFlag = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        // ボタンのイラスト変更
        val mainButton = MainButtonFragment()
        val t = supportFragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putInt("musicId", bgmId)
        bundle.putString("activity", "Setting")
        mainButton.arguments = bundle
        t.add(R.id.setting_main_button, mainButton, "main_button")
        t.commit()

        // FragmentManagerからFragmentTransactionを作成
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        // 実際に使用するFragmentの作成
        val newFragment = SettingButtonFragment()
        // Fragmentを組み込む
        transaction.replace(R.id.setting_container, newFragment)
        // backstackに追加
        transaction.addToBackStack(null)
        // 上記の変更を反映する
        transaction.commit()

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
        if (!this.isFinishing) {
            val intent = Intent(applicationContext, MyService::class.java)
            intent.putExtra("id", bgmId)
            intent.putExtra("flag", 1)
            startService(intent)
        }
    }

    override fun onBackPressed() {}

}
