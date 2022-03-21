package com.picfix.tools.view.activity

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.picfix.tools.R
import com.picfix.tools.adapter.DataAdapter
import com.picfix.tools.bean.Resource
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.ImageManager
import com.picfix.tools.utils.ToastUtil
import com.picfix.tools.view.base.BaseActivity
import kotlinx.android.synthetic.main.item_pic_with_shadow.view.*


class PhotoTransStyleActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var bigPic: ImageView
    private lateinit var camera: Button
    private lateinit var album: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var styleName: TextView

    private var mList = arrayListOf<Bitmap>()
    private var uploadList = arrayListOf<Uri>()
    private var value = ""
    private var mCameraUri: Uri? = null
    private var picList = arrayListOf<Resource>()

    override fun setLayout(): Int {
        return R.layout.a_photo_trans_style
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        bigPic = findViewById(R.id.big_pic)
        recyclerView = findViewById(R.id.rv_pic_style)
        styleName = findViewById(R.id.style_name)

        back.setOnClickListener { finish() }

        camera = findViewById(R.id.open_camera)
        album = findViewById(R.id.open_album)

        camera.setOnClickListener { takePhoto() }
        album.setOnClickListener { chooseAlbum() }

    }

    override fun initData() {
        initRecyclerView()
    }

    private fun initRecyclerView() {
        picList.clear()
        picList.add(Resource("1", R.drawable.ic_colour_after_1, "原画风"))
        picList.add(Resource("2", R.drawable.iv_trans_qianbi, "铅笔风"))
        picList.add(Resource("2", R.drawable.iv_trans_caiseqianbi, "彩色铅笔风"))
        picList.add(Resource("3", R.drawable.iv_trans_caisetangkuai, "彩色糖块风"))
        picList.add(Resource("4", R.drawable.iv_trans_shenhuchuanchonglang, "神奈川冲浪里油画风"))
        picList.add(Resource("5", R.drawable.iv_trans_xunyicao, "薰衣草油画风"))
        picList.add(Resource("6", R.drawable.iv_trans_qiyi, "奇异油画风"))
        picList.add(Resource("7", R.drawable.iv_trans_nahan, "呐喊油画风"))
        picList.add(Resource("8", R.drawable.iv_trans_geteyou, "哥特式油画风"))
        picList.add(Resource("9", R.drawable.iv_trans_katong, "卡通风"))

        val adapter = DataAdapter.Builder<Resource>()
            .setData(picList)
            .setLayoutId(R.layout.item_pic_with_shadow)
            .addBindView { itemView, itemData ->
                Glide.with(this).load(itemData.icon).into(itemView.img)
                itemView.setOnClickListener {
                    bigPic.setImageDrawable(ResourcesCompat.getDrawable(resources, itemData.icon, null))
                    styleName.text = itemData.name
                }
            }
            .create()

        recyclerView.layoutManager = GridLayoutManager(this, 5)
        recyclerView.adapter = adapter
        adapter.notifyItemRangeChanged(0, picList.size)
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
        intent.putExtra("from", "style_trans")
        intent.putExtra("uri", uri.toString())
        startActivity(intent)
    }

    private fun checkFileSize(uri: Uri) {
        val length = contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0
        if (length > 5 * 1024 * 1024) {
            ToastUtil.show(this, "上传图片不要大于4MB")
        } else {
            toImagePage(uri)
        }
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