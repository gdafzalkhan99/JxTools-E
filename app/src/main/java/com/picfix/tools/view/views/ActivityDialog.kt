package com.picfix.tools.view.views

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.picfix.tools.R
import com.picfix.tools.adapter.DataAdapter
import com.picfix.tools.bean.FileBean
import com.picfix.tools.callback.DialogCallback
import com.picfix.tools.utils.AppUtil
import kotlinx.android.synthetic.main.d_backup_files_item.view.*


class ActivityDialog(context: Context) : Dialog(context, R.style.app_dialog) {
    private val mContext: Context = context
    private lateinit var cancel: TextView


    init {
        initVew()
    }

    private fun initVew() {
        val dialogContent = LayoutInflater.from(mContext).inflate(R.layout.d_backup_files, null)
        setContentView(dialogContent)
        setCancelable(true)

        cancel = dialogContent.findViewById(R.id.dialog_cancel)
        cancel.setOnClickListener { cancel() }

    }


    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = AppUtil.getScreenWidth(context) - 50
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }

        super.show()
    }


}