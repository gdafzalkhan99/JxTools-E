package com.picfix.tools.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.picfix.tools.R
import com.picfix.tools.callback.Callback
import com.picfix.tools.callback.HttpCallback
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.ImageManager
import com.picfix.tools.controller.LogReportManager
import com.picfix.tools.controller.PayManager
import com.picfix.tools.utils.AppUtil
import com.picfix.tools.utils.FileUtil
import com.picfix.tools.utils.ToastUtil
import com.picfix.tools.view.base.BaseActivity
import com.picfix.tools.view.views.ActivityDialog
import com.picfix.tools.view.views.SaveSuccessDialog
import com.tencent.mmkv.MMKV
import java.io.File


class PhotoZipActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var from: ImageView
    private lateinit var add: ImageView
    private lateinit var imageWidth: TextView
    private lateinit var imageHeight: TextView
    private lateinit var begin: Button
    private lateinit var size: TextView
    private lateinit var percent: AppCompatSeekBar
    private lateinit var percentText: TextView
    private var value = ""
    private var mImageUri: Uri? = null
    private var progressValue = 70
    private var mWidth = 0
    private var mHeight = 0

    override fun setLayout(): Int {
        return R.layout.a_photo_zip
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        from = findViewById(R.id.image_from)
        size = findViewById(R.id.image_size)
        percent = findViewById(R.id.image_zip_percent)
        percentText = findViewById(R.id.zip_percent)
        begin = findViewById(R.id.begin_fix)
        add = findViewById(R.id.image_add)
        imageWidth = findViewById(R.id.image_width)
        imageHeight = findViewById(R.id.image_height)

        back.setOnClickListener { finish() }
        from.setOnClickListener { chooseAlbum() }
        begin.setOnClickListener { beginFix() }

        percent.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                progressValue = progress
                if (progress < 10) {
                    progressValue = 10
                }
                val text = "${progressValue}%"
                percentText.text = text
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

    }

    override fun initData() {
        LogReportManager.logReport("图片压缩", "访问页面", LogReportManager.LogType.OPERATION)
        showActivity()
    }

    private fun showActivity() {
        when (AppUtil.getChannelId()) {
            Constant.CHANNEL_VIVO, Constant.CHANNEL_HUAWEI -> {
                val versionCode = AppUtil.getPackageVersionCode(this, packageName)
                if (versionCode == Constant.APP_VERSION.toInt()) {
                    val times = MMKV.defaultMMKV()?.decodeString("activity_times")
                    if (times == null || times == "true") {
                        ActivityDialog(this).show()
                    }
                }
            }
        }
    }

    private fun chooseAlbum() {
        val intent = Intent()
        intent.action = Intent.ACTION_PICK
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        intent.type = "image/*"
        startActivityForResult(intent, 0x1001)
    }

    @SuppressLint("SetTextI18n")
    private fun checkFileSize(uri: Uri) {
        val length = contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0
        if (length > 0) {
            add.visibility = View.GONE
            size.text = "$length b"

            Glide.with(this).asBitmap().load(uri).into(object : CustomTarget<Bitmap>() {

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    from.setImageBitmap(resource)
                    mWidth = resource.width
                    mHeight = resource.height

                    imageWidth.text = "$mWidth"
                    imageHeight.text = "$mHeight"

                    size.text = "${length / 1024}KB"
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
        }

    }

    private fun beginFix() {
        if (Constant.USER_NAME == "") {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            return
        }

        PayManager.getInstance().checkFixPay(this) {
            if (!it) {
                toPay()
            } else {
                if (mImageUri == null) {
                    ToastUtil.showShort(this, "请选择一张图片")
                    return@checkFixPay
                }

                begin.isEnabled = false
                begin.text = getString(R.string.fix_access)
                begin.background = ContextCompat.getDrawable(this, R.drawable.shape_corner_grey)

                val width = imageWidth.text.toString().toInt()
                val height = imageHeight.text.toString().toInt()
                if (width != mWidth && height != mHeight) {
                    Glide.with(this).asBitmap().load(mImageUri).override(width, height).into(object : CustomTarget<Bitmap>() {

                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            //save bitmap
                            FileUtil.saveImage(this@PhotoZipActivity, resource, progressValue, object : HttpCallback {
                                override fun onSuccess() {
                                    resetStatus()
                                    initDialog()
                                    ImageManager.report()
                                }

                                override fun onFailed(msg: String) {

                                }
                            })
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    })
                } else {
                    val path = FileUtil.getRealPathFromUri(this, mImageUri)
                    FileUtil.saveImage(this, File(path), progressValue, object : HttpCallback {
                        override fun onSuccess() {
                            resetStatus()
                            initDialog()
                            ImageManager.report()
                        }

                        override fun onFailed(msg: String) {

                        }
                    })
                }
            }
        }
    }

    private fun toPay() {
        val intent = Intent(this, FixPayActivity::class.java)
        intent.putExtra("serviceId", 7)
        startActivity(intent)
    }

    private fun initDialog() {
        SaveSuccessDialog(this, "相册", object : Callback {
            override fun onSuccess() {
                finish()
            }

            override fun onCancel() {
            }
        }).show()

        when (AppUtil.getChannelId()) {
            Constant.CHANNEL_VIVO, Constant.CHANNEL_HUAWEI -> {
                val mmkv = MMKV.defaultMMKV()
                val times = mmkv?.decodeString("activity_times")
                if (times == "true") {
                    mmkv.encode("activity_times", "false")
                }
            }
        }

        LogReportManager.logReport("图片压缩", "使用成功", LogReportManager.LogType.OPERATION)
    }

    private fun resetStatus() {
        begin.isEnabled = true
        begin.text = getString(R.string.fix_begin)
        begin.background = ContextCompat.getDrawable(this, R.drawable.shape_corner_blue)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x1001) {
            if (data != null) {
                val uri = data.data
                if (uri != null) {
                    checkFileSize(uri)
                    mImageUri = uri
                }
            }
        }
    }

}
