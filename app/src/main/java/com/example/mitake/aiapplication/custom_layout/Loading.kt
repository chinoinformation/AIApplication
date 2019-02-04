@file:Suppress("DEPRECATION")

package com.example.mitake.aiapplication.custom_layout

import android.app.ProgressDialog
import android.content.Context
import com.example.mitake.aiapplication.R




@Suppress("DEPRECATION")
class Loading(mContext: Context) {
    private var mProgressDialog: ProgressDialog? = ProgressDialog(mContext, R.style.Theme_MyDialog)

    fun show() {
        mProgressDialog!!.setTitle("Please wait")
        mProgressDialog!!.setMessage("Loading...")
        mProgressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        mProgressDialog!!.setCancelable(false)
        mProgressDialog!!.show()
    }

    fun close() {
        mProgressDialog!!.dismiss()
        mProgressDialog = null
    }
}