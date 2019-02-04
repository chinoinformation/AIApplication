package com.example.mitake.aiapplication.data

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity

class DataManagement(context: Context) {
    /** プリファレンス */
    private val prefs: SharedPreferences = context.getSharedPreferences("DataStore", AppCompatActivity.MODE_PRIVATE)

    /** データ書き込み */
    fun saveData(key: String, value: String){
        prefs.edit().putString(key, value).apply()
    }

    /** データ削除 */
    fun removeData(key: String){
        prefs.edit().remove(key).apply()
    }

    /** データクリア */
    fun clearData(){
        prefs.edit().clear().apply()
    }

    /** データ読み込み */
    fun readData(key: String, def: String): List<String>{
        val res = prefs.getString(key, def)
        return res.split(",")
    }

}