package com.picfix.tools.view.activity

import android.widget.ImageView
import android.widget.LinearLayout
import com.picfix.tools.R
import com.picfix.tools.controller.DBManager
import com.picfix.tools.utils.AppUtil
import com.picfix.tools.utils.FileUtil
import com.picfix.tools.utils.ToastUtil
import com.picfix.tools.view.base.BaseActivity
import kotlinx.coroutines.*

class SettingActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var clear: LinearLayout
    private lateinit var about: LinearLayout


    override fun setLayout(): Int {
        return R.layout.a_setting
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        back.setOnClickListener { finish() }

        clear = findViewById(R.id.clear)
        about = findViewById(R.id.about)

        clear.setOnClickListener { clearCache() }
        about.setOnClickListener { aboutUs() }
    }

    override fun initData() {


    }

    private fun clearCache() {
        launch(Dispatchers.IO) {
            DBManager.deleteFiles(this@SettingActivity)
            FileUtil.clearAllCache(this@SettingActivity)
        }

        launch(Dispatchers.Main) {
            ToastUtil.showShort(this@SettingActivity, "清除成功")
        }

    }

    private fun aboutUs() {
        val packName = AppUtil.getPackageVersionName(this, packageName)
        val appName = getString(R.string.app_name)
        ToastUtil.show(this, "$appName $packName")
    }
}