package com.example.mitake.aiapplication.battle

import android.widget.ImageView

class MoveLocation(CenterList: Array<Array<Array<Int>>>) {
    // move座標計算変数
    private val centerList = CenterList

    // move関数の詳細
    // return ->  MutableList(flag, moveX, localMoveX, moveY, localMoveY)
    // (1) flag: 移動判定（キャラを移動させるか）
    // (2) moveX: 移動先X座標
    // (3) localMoveX: マップX座標
    // (4) moveY: 移動先Y座標
    // (5) localMoveY: マップY座標

    fun moveMap(touchX: Int, touchY: Int): MutableList<Int>{
        val moveX = centerList[touchX][0][0]
        val localMoveX = touchX
        val moveY = centerList[0][touchY][1]
        val localMoveY = touchY
        return mutableListOf(0, moveX, localMoveX, moveY, localMoveY)
    }

    // タッチイベント
    // return -> List(flag, X, Y, preStartX, preStartY)
    // (1) flag: 判定（アニメーションさせるべきか）
    // (2) X: X座標終点
    // (3) Y: Y座標終点
    // (4) preStartX: 一個前のX座標終点
    // (5) preStartY: 一個前のY座標終点

    fun onMoveEvent(img: ImageView, startX: Int, startY: Int, flagList: MutableList<Int>): List<Int> {
        // 現在位置取得
        val location = IntArray(2)
        img.getLocationInWindow(location)
        val presentX = location[0] + img.width/2
        val presentY = location[1] + img.height/2
        // move座標と現在位置の差分
        val diffX = flagList[1] - presentX
        val diffY = flagList[3] - presentY

        // 終点
        val X = startX + diffX
        val Y = startY + diffY

        // 一個前の座標を保持
        val preStartX = diffX
        val preStartY = diffY
        return listOf(0, X, Y, preStartX, preStartY)
    }

}