package com.example.mitake.aiapplication.custom_layout.quest

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class StoryReader {
    var objects: MutableList<TutorialStoryData> = ArrayList()
    fun reader(context: Context, num: Int) {
        val assetManager = context.resources.assets
        try {
            // CSVファイルの読み込み
            val csvName = "tutorial_story" + num.toString() + ".csv"
            val inputStream = assetManager.open(csvName)
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferReader = BufferedReader(inputStreamReader)
            val lines: List<String> = bufferReader.readLines()
            for (line in lines) {
                //カンマ区切りで１つづつ配列に入れる
                val data = TutorialStoryData()
                val rowData = line.split(",")

                //CSVの左([0]番目)から順番にセット
                data.textWords = rowData[0].trim()
                data.characterName = rowData[1].trim()
                data.ID = rowData[2].trim()
                data.animation = rowData[3].trim()
                data.resource = rowData[4].trim()

                objects.add(data)
            }
            bufferReader.close()
        } catch (e: IOException) {}
    }
}