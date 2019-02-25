package com.example.mitake.aiapplication.bgm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.media.AudioManager
import java.io.IOException
import android.media.MediaPlayer.OnCompletionListener
import com.example.mitake.aiapplication.data.DataManagement


@Suppress("DEPRECATION")
class MyService : Service() {
    var bgmId: Int = 0
    private var preBgmId: Int = 0
    var flag: Int = 0
    private var start: Int = 0
    private var startFlag: Int = 0

    // 現在の状態
    var state: Int = STATE_STOP
        private set

    // 現在の mediaPlayer のインデックス
    private var mediaPlayerIndex: Int = -1
    // media player の配列
    private val mp = arrayOfNulls<MediaPlayer>(3)
    // 現在の音量
    private var vol: Float = 0f

    /** プリファレンス */
    private var data: DataManagement? = null

    /**
     * internal listener which handles looping thing
     */
    private val completionListener = object : OnCompletionListener {

        override fun onCompletion(curmp: MediaPlayer) {
            var mpPlaying = 0
            var mpNext = 0
            // mp3 is already playing release it
            // at listener to mp3
            // set vol
            // set nextMediaPlayer
            // set nextMediaPlayer vol
            when {
                curmp === mp[0] -> {
                    mpPlaying = 1
                    mpNext = 2
                }
                curmp === mp[1] -> {
                    mpPlaying = 2
                    mpNext = 0  // corrected, else index out of range
                }
                curmp === mp[2] -> {
                    mpPlaying = 0 // corrected, else index out of range
                    mpNext = 1 // corrected, else index out of range
                }

            // as we have set mp2 mp1's next, so index will be 1
            }

            // as we have set mp2 mp1's next, so index will be 1
            mediaPlayerIndex = mpPlaying
            try {
                // mp3 is already playing release it
                if (mp[mpNext] != null) {
                    mp[mpNext]!!.release()
                }
                mp[mpNext] = MediaPlayer.create(applicationContext, bgmId)
                // at listener to mp3
                mp[mpNext]!!.setOnCompletionListener(this)
                // set vol
                mp[mpNext]!!.setVolume(vol, vol)
                // set nextMediaPlayer
                if (mpPlaying != mpNext){
                    if (mp[mpPlaying] != null && mp[mpNext] != null) {
                        mp[mpPlaying]!!.setNextMediaPlayer(mp[mpNext])
                    }
                }
                // set nextMediaPlayer vol
                mp[mpPlaying]!!.setVolume(vol, vol)
            } catch (e: Exception) {}
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            flag = intent.getIntExtra("flag", 0)
            if (flag == 3){
                vol = intent.getFloatExtra("bgmLevel", 0.0f)
                mp[mediaPlayerIndex]!!.setVolume(vol, vol)
            } else {
                bgmId = intent.getIntExtra("id", 0)
                when (flag) {
                    1 -> pause()
                    2 -> play()
                    else -> if (preBgmId != bgmId) {
                        startPlay()
                    }
                }
            }
        }
        preBgmId = bgmId

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        preBgmId = 0
        stop()
        data = null
    }

    /** start */
    private fun startPlay(){
        // プリファレンス
        data = DataManagement(this)
        // AudioManagerを取得する
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // 最大音量値を取得
        val mVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
        // 現在の音量を取得する
        val ringVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / mVol
        val volData = data!!.readData("bgmLevel", "-1")[0].toFloat()
        vol = if (volData == -1f){
            ringVolume
        } else {
            if (volData in 0f..1f) {
                ringVolume * volData
            } else {
                ringVolume
            }
        }
        play(bgmId)
        flag = 1
        startFlag = 1
    }

    /** 初期化 */
    private fun play(resourceId: Int) {
        bgmId = resourceId
        if (state == STATE_PLAYING) stop()
        for (i in mp.indices) {
            mp[i] = MediaPlayer.create(applicationContext, resourceId)
            mp[i]!!.setOnCompletionListener(completionListener)
        }

        mp[0]!!.setNextMediaPlayer(mp[1])
        mp[1]!!.setNextMediaPlayer(mp[2])

        mp[0]!!.setVolume(vol, vol)
        mp[1]!!.setVolume(vol, vol)
        mp[0]!!.start()

        mediaPlayerIndex = 0
        state = STATE_PLAYING
    }

    /** BGMの再生を再開 */
    private fun play(){
        if (state == STATE_PAUSED){
            when {
                startFlag != 0 -> {
                    try { mp[mediaPlayerIndex]!!.prepare() } catch (e: IllegalStateException) { } catch (e: IOException) { }
                    mp[mediaPlayerIndex]!!.setOnPreparedListener { mp: MediaPlayer? ->
                        run {
                            mp!!.seekTo(start)
                            mp.start()
                            state = STATE_PLAYING
                        }
                    }
                }
                else -> {
                    startPlay()
                }
            }
        } else {
            if (startFlag == 0 && mp[0] == null && mp[1] == null && mp[2] == null){
                startPlay()
            }
        }
    }

    /** 一時停止 */
    private fun pause() {
        if (state == STATE_PLAYING) {
            start = mp[mediaPlayerIndex]!!.currentPosition
            mp[mediaPlayerIndex]!!.stop()
            state = STATE_PAUSED
        }
    }

    /** 停止 */
    private fun stop() {
        for (i in mp.indices) {
            if (mp[i] != null) {
                if (mp[i]!!.isPlaying) {
                    mp[i]!!.stop()
                }
                if (!mp[i]!!.isPlaying) {
                    mp[i]!!.release()
                }
            }
        }
        state = STATE_STOP
    }

    companion object {
        // media player の状態
        private const val STATE_PLAYING = 1
        private const val STATE_PAUSED = 2
        private const val STATE_STOP = 3
    }

}