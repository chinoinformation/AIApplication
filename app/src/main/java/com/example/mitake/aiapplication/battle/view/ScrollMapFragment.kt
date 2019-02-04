package com.example.mitake.aiapplication.battle.view

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.mitake.aiapplication.GlideAnim
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.battle.BattleActivity
import com.example.mitake.aiapplication.battle.data.Battle
import com.example.mitake.aiapplication.custom_layout.WaveAnimationLayout
import kotlinx.android.synthetic.main.fragment_scroll_map.*

class ScrollMapFragment : Fragment() {

    /** View */
    var charImgList: MutableList<ImageView?> = mutableListOf()
    var damageText: WaveAnimationLayout? = null

    /** private定数 */
    private var boardSize: Int = Battle.BoardSize.Value
    private var charNum: Int = Battle.CharNum.Value

    /** マップ */
    var placeList: List<List<ImageView>>? = null

    /** クラス */
    private var mainActivity: BattleActivity? = null
    private var glideAnim: GlideAnim? = GlideAnim()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_scroll_map, container, false)

        for (i in 0 until charNum){
            val id = resources.getIdentifier("battleChar1_" + (i+1).toString(), "id", activity!!.packageName)
            charImgList.add(root.findViewById(id))
        }
        for (i in 0 until charNum){
            val id = resources.getIdentifier("battleChar2_" + (i+1).toString(), "id", activity!!.packageName)
            charImgList.add(root.findViewById(id))
        }
        for (i in 0 until charImgList.size){
            charImgList[i]!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

        damageText = root.findViewById(R.id.damage_text)
        damageText!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        damageText!!.setAnimId(R.animator.damage_popup)
        mainActivity = (activity as BattleActivity)
    }

    fun initMap(): List<List<ImageView>>{
        // 二次元配列にマッピングしながらGridLayoutにマスを設定
        placeList = arrayOfNulls<List<ImageView>>(boardSize).mapIndexed { x, _ ->
            arrayOfNulls<ImageView>(boardSize).mapIndexed { y, _ ->
                // ランダムに地形を選択
                val random = Math.random()
                val placeType: Int = when {
                    random <= 0.10 -> R.layout.mountain
                    random > 0.10 && random <= 0.15 -> R.layout.poison
                    random >= 0.80 -> R.layout.pond
                    else -> R.layout.plain
                }
                val place = layoutInflater.inflate(placeType, null)
                place.setOnClickListener { mainActivity!!.onClickEvent(x, y) }
                // マップの種類を記録
                mainActivity!!.mapType[y][x] = when (placeType){
                    R.layout.mountain -> 1
                    R.layout.poison -> 2
                    R.layout.pond -> 3
                    else -> 0
                }
                gamePlacesGrid.addView(place)
                place.findViewById(R.id.gamePlaceImageView) as ImageView
            }
        }
        return placeList!!
    }

    /** キャラ画像アニメーション */
    fun charImgAnimation(imgList: MutableList<ImageView?>, numberList: MutableList<Int>){
        for (i in 0..(charNum-1)){
            if (imgList[i] != null){
                imgList[i]!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                val bitmapName: String = "char" + numberList[i].toString() + "11"
                val drawName: String = "char" + numberList[i].toString() + "_3"
                glideAnim!!.animation(context!!, imgList[i]!!, bitmapName, drawName)
            }
        }
        for (i in 0..(charNum-1)){
            if (imgList[i+4] != null){
                imgList[i+4]!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                val bitmapName: String = "char" + numberList[i+4].toString() + "11"
                val drawName: String = "char" + numberList[i+4].toString() + "_1"
                glideAnim!!.animation(context!!, imgList[i+4]!!, bitmapName, drawName)
            }
        }
    }

    fun charDisappearEffect(condition: Int, index: Int){
        mainActivity!!.imgList[index]!!.setColorFilter(Color.parseColor("#80ff4500"))
        // alphaプロパティを0fから1fに変化
        val objectAnimator = ObjectAnimator.ofFloat(mainActivity!!.imgList[index]!!, "alpha", 1f, 0f)
        objectAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                mainActivity!!.imgList[index]!!.setImageResource(0)
                mainActivity!!.imgList[index]!!.setImageDrawable(null)
                mainActivity!!.imgList[index] = null
                mainActivity!!.charMap[mainActivity!!.charPlaceList[index].Y][mainActivity!!.charPlaceList[index].X] = 0
                mainActivity!!.charPlaceList[index] = mainActivity!!.notCharPlace
                for (i in 0 until (2*charNum)){
                    if (mainActivity!!.imgList[i] != null){
                        mainActivity!!.firstTurnCharNum = i
                        break
                    }
                }
                if (!mainActivity!!.changeImagesFragment!!.judgeBattleEnd()){
                    mainActivity!!.battleFinish()
                }
                mainActivity!!.damageAnimationFinish(condition)
            }
        })

        // 0.8秒かけて実行
        objectAnimator.duration = 800
        // アニメーションを開始
        objectAnimator.start()
    }

    /** ダメージテキスト表示 */
    fun setDamageText(damage: String, offSet: Int){
        damageText!!.setText(damage)
        damageText!!.setStartOffset(offSet)
    }

    /** ダメージテキスト非表示 */
    fun resetDamageText(){
        damageText!!.setText("")
    }

    /** 全ての変数を初期化 */
    fun allInit() {
        for (i in 0 until charImgList.size){
            if (charImgList[i] != null) {
                charImgList[i]!!.setImageResource(0)
                charImgList[i]!!.setImageDrawable(null)
            }
        }
        charImgList = mutableListOf()
        for (i in 0 until placeList!!.size){
            for (j in 0 until placeList!![0].size){
                placeList!![i][j].setOnClickListener(null)
                placeList!![i][j].setImageResource(0)
                placeList!![i][j].setImageDrawable(null)
            }
        }
        damageText!!.setBackgroundResource(0)
        damageText = null
        placeList = null
        boardSize = 0
        charNum = 0
        mainActivity = null
        glideAnim = null
    }

}