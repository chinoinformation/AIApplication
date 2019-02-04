package com.example.mitake.aiapplication.bgm

import android.content.Context
import android.media.SoundPool

class EffectList(context: Context, soundPool: SoundPool?) {
    private var soundId: Int = 0
    private val mContext = context
    private val mSoundPool = soundPool
    private var soundIdList: Map<String, Int> = mapOf()
    var soundList: MutableMap<String, Int> = mutableMapOf()

    fun getList(name: String){
        soundIdList = mapOf("start" to mContext.resources.getIdentifier("bgm_effect_start", "raw", mContext.packageName),
                "battle_start" to mContext.resources.getIdentifier("bgm_effect_battle_start", "raw", mContext.packageName),
                "battle_finish" to mContext.resources.getIdentifier("bgm_effect_battle_finish", "raw", mContext.packageName),
                "normal_attack" to mContext.resources.getIdentifier("bgm_effect_attack", "raw", mContext.packageName),
                "sp_attack" to mContext.resources.getIdentifier("bgm_effect_sp_attack", "raw", mContext.packageName),
                "damage" to mContext.resources.getIdentifier("bgm_effect_damage", "raw", mContext.packageName),
                "critical" to mContext.resources.getIdentifier("bgm_effect_critical", "raw", mContext.packageName),
                "miss" to mContext.resources.getIdentifier("bgm_effect_attack_miss", "raw", mContext.packageName),
                "down" to mContext.resources.getIdentifier("bgm_effect_down", "raw", mContext.packageName),
                "button" to mContext.resources.getIdentifier("bgm_effect_button", "raw", mContext.packageName),
                "other_button" to mContext.resources.getIdentifier("bgm_effect_other_button", "raw", mContext.packageName),
                "update_ai" to mContext.resources.getIdentifier("bgm_effect_update_ai", "raw", mContext.packageName),
                "go_battle" to mContext.resources.getIdentifier("bgm_effect_go_battle", "raw", mContext.packageName))
        soundId = soundIdList[name]?.let { mSoundPool!!.load(mContext, it, 1) }!!
        soundList[name] = soundId
    }

    fun play(name: String){
        soundList[name]?.let { mSoundPool!!.play(it, 1f, 1f, 0, 0, 1.0f) }
    }

    fun release(){
        mSoundPool!!.release()
    }

    fun unload(name: String){
        mSoundPool!!.unload(soundIdList[name]!!)
    }
}