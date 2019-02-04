package com.example.mitake.aiapplication.battle

import android.content.Context
import android.widget.Button
import android.widget.ImageView
import com.example.mitake.aiapplication.GlideAnim
import com.example.mitake.aiapplication.battle.data.Place
import com.example.mitake.aiapplication.battle.data.Unit

class StatusChange {
    /** ボタン無効化 */
    fun ButtonNotEnabled(button: Button){
        button.isEnabled = false
        button.alpha = 0.5f
    }

    /** ボタン有効化 */
    fun ButtonEnabled(button: Button){
        button.isEnabled = true
        button.alpha = 1.0f
    }

    /** 状態異常のリソース */
    fun getStateRes(context: Context, attackState: ImageView, defenseState: ImageView, spState: ImageView, otherState: ImageView, position: Place, unit: Unit, mapHeight: Array<Array<Int>>){
        when(mapHeight[position.Y][position.X]){
            1 -> {
                var bitmapName = "attack_up1"
                var drawName = "attack_up"
                stateChangeStart(context, attackState, bitmapName, drawName)
                bitmapName = "defense_down1"
                drawName = "defense_down"
                stateChangeStart(context, defenseState, bitmapName, drawName)
                stateChangeStop(otherState)
            }
            2 -> {
                var bitmapName = "defense_down1"
                var drawName = "defense_down"
                stateChangeStart(context, attackState, bitmapName, drawName)
                stateChangeStart(context, defenseState, bitmapName, drawName)
                bitmapName = "pain1"
                drawName = "pain"
                stateChangeStart(context, otherState, bitmapName, drawName)
            }
            3 -> {
                var bitmapName = "defense_down1"
                var drawName = "defense_down"
                stateChangeStart(context, attackState, bitmapName, drawName)
                bitmapName = "attack_up1"
                drawName = "attack_up"
                stateChangeStart(context, defenseState, bitmapName, drawName)
                stateChangeStop(otherState)
            }
            else -> {
                stateChangeStop(attackState)
                stateChangeStop(defenseState)
                stateChangeStop(otherState)
            }
        }
        when (unit.SP) {
            0 -> {
                stateChangeStop(spState)
            }
            else -> {
                val bitmapName = "sp1"
                val drawName = "sp"
                stateChangeStart(context, spState, bitmapName, drawName)
            }
        }
    }

    private fun stateChangeStart(context: Context, image: ImageView, bitmapName: String, drawName: String){
        GlideAnim().animation(context, image, bitmapName, drawName)
    }

    fun stateChangeStop(image: ImageView){
        image.setImageResource(0)
        image.setImageDrawable(null)
    }

}