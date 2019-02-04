package com.example.mitake.aiapplication.battle.record

class DataLog {
    var log: String? = "戦闘開始<br></br><br></br>"

    fun addLog(message: String){
        log += "$message<br></br><br></br>"
    }

    fun release(){
        log = null
    }

}