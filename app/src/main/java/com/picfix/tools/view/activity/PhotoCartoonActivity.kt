package com.picfix.tools.view.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.picfix.tools.R
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.ImageManager
import com.picfix.tools.utils.ToastUtil
import com.picfix.tools.view.base.BaseActivity


class PhotoCartoonActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var bigPic: ImageView
    private lateinit var firstPic: ImageView
    private lateinit var secondPic: ImageView
    private lateinit var thirdPic: ImageView
    private lateinit var camera: Button
    private lateinit var album: Button
    private var mList = arrayListOf<Bitmap>()
    private var uploadList = arrayListOf<Uri>()
    private var value = ""
    private var mCameraUri: Uri? = null

    override fun setLayout(): Int {
        return R.layout.a_photo_cartoon
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        bigPic = findViewById(R.id.big_pic)
        firstPic = findViewById(R.id.first_pic)
        secondPic = findViewById(R.id.second_pic)
        thirdPic = findViewById(R.id.third_pic)

        back.setOnClickListener { finish() }

        camera = findViewById(R.id.open_camera)
        album = findViewById(R.id.open_album)

        camera.setOnClickListener { takePhoto() }
        album.setOnClickListener { chooseAlbum() }
        firstPic.setOnClickListener { choosePic(0) }
        secondPic.setOnClickListener { choosePic(1) }
        thirdPic.setOnClickListener { choosePic(2) }

    }

    override fun initData() {

    }

    private fun choosePic(index: Int) {
        when (index) {
            0 -> Glide.with(this).load(R.drawable.iv_cartoon_after_4).into(bigPic)
            1 -> Glide.with(this).load(R.drawable.iv_cartoon_after_2).into(bigPic)
            2 -> Glide.with(this).load(R.drawable.iv_cartoon_after_1).into(bigPic)
        }
    }

    private fun takePhoto() {
        ImageManager.checkPermission(this) { that ->
            if (that) {
                ImageManager.openCamera(this) {
                    mCameraUri = it
                }
            } else {
                ToastUtil.showShort(this, "请允许打开相机权限用于拍照")
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

    private fun toImagePage(uri: Uri) {
        val intent = Intent(this, ImageActivity::class.java)
        intent.putExtra("from", "cartoon")
        intent.putExtra("uri", uri.toString())
        startActivity(intent)
    }

    private fun checkFileSize(uri: Uri) {
        val length = contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0
        if (length > 4 * 1024 * 1024) {
            ToastUtil.show(this, "上传图片不要大于4MB")
            return
        }

        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
        val width = bitmap.width
        val height = bitmap.height
        if (width > height) {
            if (width >= 4000 || height <= 10 || width / height > 4) {
                ToastUtil.showShort(this, "图片尺寸不符合要求")
                return
            }
        } else {
            if (height >= 4000 || width <= 10 || height / width > 4) {
                ToastUtil.showShort(this, "图片尺寸不符合要求")
                return
            }
        }

        toImagePage(uri)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x1001) {
            if (data != null) {
                val uri = data.data
                if (uri != null) {
                    checkFileSize(uri)
                }
            }
        }

        if (requestCode == Constant.CAMERA_REQUEST_CODE) {
            if (mCameraUri != null) {
                checkFileSize(mCameraUri!!)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        mList.clear()
        uploadList.clear()

    }

}