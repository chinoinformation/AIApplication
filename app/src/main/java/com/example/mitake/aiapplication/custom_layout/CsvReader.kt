package com.example.mitake.aiapplication.custom_layout

import android.content.Context
import com.example.mitake.aiapplication.custom_layout.character.CharData
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class CsvReader {
    var objects: MutableList<CharData> = ArrayList()
    fun reader(context: Context) {
        val assetManager = context.resources.assets
        try {
            // CSVファイルの読み込み
            val inputStream = assetManager.open("char_data.csv")
            val inputStreamReader = InputStreamReader(inputStream, "SJIS")
            val bufferReader = BufferedReader(inputStreamReader)
            val lines: List<String> = bufferReader.readLines()
            for (line in lines) {

                //カンマ区切りで１つづつ配列に入れる
                val data = CharData()
                val rowData = line.split(",")

                //CSVの左([0]番目)から順番にセット
                data.id = rowData[0].trim()
                data.type = rowData[1].trim()
                data.name = rowData[2].trim()
                data.HP = rowData[3].trim()
                data.MP = rowData[4].trim()
                data.attack = rowData[5].trim()
                data.defense = rowData[6].trim()
                data.move = rowData[7].trim()
                data.attackRange = rowData[8].trim()

                objects.add(data)
            }
            bufferReader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}