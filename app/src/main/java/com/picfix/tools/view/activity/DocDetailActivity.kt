package com.picfix.tools.view.activity

import android.content.Context
import android.content.Intent
import android.widget.*
import com.bumptech.glide.Glide
import com.picfix.tools.R
import com.picfix.tools.bean.FileStatus
import com.picfix.tools.bean.FileWithType
import com.picfix.tools.callback.FileWithTypeCallback
import com.picfix.tools.controller.WxManager
import com.picfix.tools.utils.AppUtil
import com.picfix.tools.view.base.BaseActivity
import com.picfix.tools.view.views.ExportFileDialog
import com.tencent.smtt.export.external.TbsCoreSettings
import com.tencent.smtt.sdk.QbSdk
import kotlinx.android.synthetic.main.item_doc.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DocDetailActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var style: ImageView
    private lateinit var docName: TextView
    private lateinit var docDesc: TextView
    private lateinit var export: Button
    private lateinit var delete: Button
    private val mainList = arrayListOf<FileWithType>()
    private var file: FileWithType? = null

    override fun setLayout(): Int {
        return R.layout.a_doc_detail
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        style = findViewById(R.id.style)
        back.setOnClickListener { finish() }

        docName = findViewById(R.id.doc_name)
        docDesc = findViewById(R.id.doc_description)

        export = findViewById(R.id.recovery)
        delete = findViewById(R.id.delete)

        export.setOnClickListener { nextStep(this) }
        delete.setOnClickListener { deleteDocs() }
        style.setOnClickListener { openFile(file!!) }
    }

    override fun initData() {
        initX5Environment()
        initTBS()

        file = intent.getParcelableExtra("file")
        if (file != null) {
            mainList.add(file!!)
            val date = AppUtil.timeStamp2Date(file!!.date.toString(), null)
            docName.text = file!!.name
            var size = file!!.size
            if (size > 1024) {
                size /= 1024
                docDesc.text = date + " / " + size + "KB"
            } else {
                docDesc.text = date + " / " + size + "B"
            }

            if (file!!.name.endsWith(".doc") || file!!.name.endsWith(".docx")) {
                Glide.with(this).load(R.drawable.file_word).into(style)
                return
            }

            if (file!!.name.endsWith(".txt")) {
                Glide.with(this).load(R.drawable.file_txt).into(style)
                return
            }

            if (file!!.name.endsWith(".ppt") || file!!.name.endsWith(".pptx")) {
                Glide.with(this).load(R.drawable.file_ppt).into(style)
                return
            }

            if (file!!.name.endsWith(".xls") || file!!.name.endsWith(".xlsx") || file!!.name.endsWith(".csv")) {
                Glide.with(this).load(R.drawable.file_excel).into(style)
                return
            }

            if (file!!.name.endsWith(".pdf")) {
                Glide.with(this).load(R.drawable.file_pdf).into(style)
                return
            }

            if (file!!.name.endsWith(".html") || file!!.name.endsWith(".xhtml")) {
                Glide.with(this).load(R.drawable.file_html).into(style)
                return
            }

            if (file!!.name.endsWith(".ai")) {
                Glide.with(this).load(R.drawable.file_ai).into(style)
                return
            }

            if (file!!.name.endsWith(".cad")) {
                Glide.with(this).load(R.drawable.file_cad).into(style)
                return
            }

            if (file!!.name.endsWith(".psd")) {
                Glide.with(this).load(R.drawable.file_ps).into(style)
                return
            }

            if (file!!.name.endsWith(".java") || file!!.name.endsWith(".php") || file!!.name.endsWith(".exe")
                || file!!.name.endsWith(".python") || file!!.name.endsWith(".class") || file!!.name.endsWith(".config")
                || file!!.name.endsWith(".json") || file!!.name.endsWith(".xml") || file!!.name.endsWith(".kt")
            ) {
                Glide.with(this).load(R.drawable.file_html).into(style)
                return
            }

            if (file!!.name.endsWith(".zip") || file!!.name.endsWith(".rar") || file!!.name.endsWith(".tar")) {
                Glide.with(this).load(R.drawable.file_rar).into(style)
            } else {
                Glide.with(this).load(R.drawable.file_default).into(style)
            }
        }
    }


    private fun nextStep(context: Context) {
        if (mainList.isEmpty()) return
        //pay success , do something
        ExportFileDialog(context, mainList, "wx_doc").show()
    }

    private fun openFile(file: FileWithType) {
//        val intent = Intent()
//        intent.setClass(this, FileReaderActivity::class.java)
//        intent.putExtra("title", file.name)
//        intent.putExtra("page", file.path)
//        startActivity(intent)

        val params = HashMap<String, String>()
        params["style"] = "2"
        params["local"] = "true"
        QbSdk.openFileReader(this, file.path, params, { })
    }

    /**
     * 文档预览
     */
    private fun initX5Environment() {
        val cb = object : QbSdk.PreInitCallback {
            override fun onCoreInitFinished() {
            }

            override fun onViewInitFinished(p0: Boolean) {
            }
        }

        QbSdk.initX5Environment(this, cb)
    }

    /**
     * TBS文档在线预览
     */
    private fun initTBS() {
        val map = HashMap<String, Any>()
        map[TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER] = true
        map[TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE] = true
        QbSdk.initTbsSettings(map)
    }

    private fun deleteDocs() {

        launch(Dispatchers.IO) {
            WxManager.getInstance(this@DocDetailActivity).deleteFile(mainList, object : FileWithTypeCallback {
                override fun onSuccess(step: Enum<FileStatus>) {
                    launch(Dispatchers.Main) {
                    }
                }

                override fun onProgress(step: Enum<FileStatus>, file: FileWithType) {
                    launch(Dispatchers.Main) {
                        val intent = Intent()
                        intent.putExtra("file", file)
                        setResult(0x301, intent)
                        finish()
                    }
                }

                override fun onFailed(step: Enum<FileStatus>, message: String) {
                    launch(Dispatchers.Main) {
                    }
                }
            })
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        QbSdk.closeFileReader(this)
    }

}