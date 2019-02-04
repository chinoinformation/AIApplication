package com.example.mitake.aiapplication.custom_layout.quest

import android.graphics.drawable.Drawable


class CustomQuestList(name: String, type: String, constraint: String, victoryCondition: String, background: Drawable?){
    var mQuestName: String = name
    var mQuestType: String = type
    var mQuestConstraint: String = constraint
    var mVictoryCondition: String = victoryCondition
    var mQuestBackground: Drawable? = background

    /**
     * 空のコンストラクタ
     */
    fun QuestListItem() {}

    /**
     * コンストラクタ
     * @param questName クエスト名
     * @param questType クエストタイプ
     * @param questConstraint クエスト条件
     * @param victoryCondition 勝利条件
     * @param questBackground クエスト背景
     */
    fun QuestListItem(questName: String, questType: String, questConstraint: String, victoryCondition: String, questBackground: Drawable) {
        mQuestName = questName
        mQuestType = questType
        mQuestConstraint = questConstraint
        mVictoryCondition = victoryCondition
        mQuestBackground = questBackground
    }

    /**
     * クエスト名を設定
     * @param questName クエスト名
     */
    fun setName(questName: String) {
        mQuestName = questName
    }

    /**
     * クエストタイプを設定
     * @param questType クエストタイプ
     */
    fun setType(questType: String) {
        mQuestType = questType
    }

    /**
     * クエスト条件を設定
     * @param questConstraint クエスト条件
     */
    fun setConstraint(questConstraint: String) {
        mQuestConstraint = questConstraint
    }

    /**
     * 勝利条件を設定
     * @param victoryCondition 勝利条件
     */
    fun setVictoryConcdition(victoryCondition: String) {
        mVictoryCondition = victoryCondition
    }

    /**
     * クエスト背景を設定
     * @param questBackground クエスト背景
     */
    fun setbackground(questBackground: Drawable?) {
        mQuestBackground = questBackground
    }

    /**
     * クエスト名を取得
     * @return クエスト名
     */
    fun getQuestName(): String? {
        return mQuestName
    }

    /**
     * クエストタイプを取得
     * @return クエストタイプ
     */
    fun getQuestType(): String? {
        return mQuestType
    }

    /**
     * クエスト条件を取得
     * @return クエスト条件
     */
    fun getQuestConstraint(): String? {
        return mQuestConstraint
    }

    /**
     * 勝利条件を取得
     * @return 勝利条件
     */
    fun getVictoryCondition(): String? {
        return mVictoryCondition
    }

    /**
     * クエスト背景を取得
     * @return クエスト背景
     */
    fun getBackground(): Drawable? {
        return mQuestBackground
    }

}