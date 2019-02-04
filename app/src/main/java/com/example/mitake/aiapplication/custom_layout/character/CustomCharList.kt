package com.example.mitake.aiapplication.custom_layout.character


class CustomCharList(bmp: Int, s: String) {
    var mThumbnail: Int = bmp
    var mTitle: String = s

    /**
     * 空のコンストラクタ
     */
    fun SampleListItem() {}

    /**
     * コンストラクタ
     * @param thumbnail サムネイル画像
     * @param title タイトル
     */
    fun SampleListItem(thumbnail: Int, title: String) {
        mThumbnail = thumbnail
        mTitle = title
    }

    /**
     * サムネイル画像を設定
     * @param thumbnail サムネイル画像
     */
    fun setThumbnail(thumbnail: Int) {
        mThumbnail = thumbnail
    }

    /**
     * タイトルを設定
     * @param title タイトル
     */
    fun setmTitle(title: String) {
        mTitle = title
    }

    /**
     * サムネイル画像を取得
     * @return サムネイル画像
     */
    fun getThumbnail(): Int? {
        return mThumbnail
    }

    /**
     * タイトルを取得
     * @return タイトル
     */
    fun getTitle(): String? {
        return mTitle
    }
}