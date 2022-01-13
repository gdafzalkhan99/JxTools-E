package com.picfix.tools.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.picfix.tools.R
import com.picfix.tools.adapter.DataAdapter
import com.picfix.tools.bean.UserInfo
import com.picfix.tools.callback.UploadCallback
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.OSSManager
import com.picfix.tools.controller.PayManager
import com.picfix.tools.http.exception.ApiException
import com.picfix.tools.http.loader.ComplaintLoader
import com.picfix.tools.http.loader.OssLoader
import com.picfix.tools.http.response.ResponseTransformer
import com.picfix.tools.http.schedulers.SchedulerProvider
import com.picfix.tools.utils.AppUtil
import com.picfix.tools.utils.JLog
import com.picfix.tools.utils.ToastUtil
import com.picfix.tools.view.base.BaseActivity
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.item_pic_feedback.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FeedbackActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var content: EditText
    private lateinit var contact: EditText
    private lateinit var submit: Button
    private lateinit var from: AppCompatSpinner
    private lateinit var account: EditText
    private var type = ""
    private lateinit var picRv: RecyclerView
    private lateinit var mAdapter: DataAdapter<Bitmap>
    private var mList = arrayListOf<Bitmap>()
    private var uploadList = arrayListOf<Uri>()
    private var value = ""

    override fun setLayout(): Int {
        return R.layout.a_feedback
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        back.setOnClickListener { finish() }

        content = findViewById(R.id.content)
        submit = findViewById(R.id.submit)
        submit.setOnClickListener { submit() }

        from = findViewById(R.id.spinner_question)
        contact = findViewById(R.id.contact)

        account = findViewById(R.id.pay_account)
        picRv = findViewById(R.id.pics_recyclerview)

        spinnerListener()
        initRecyclerView()

    }

    override fun initData() {
        value = MMKV.defaultMMKV()?.decodeString("report") ?: ""
        if (value == "success") {
            ToastUtil.show(this, "您已经提交过投诉与退款申请了，请耐心等待我们与您联系")
            submit.isEnabled = false
            submit.text = "已提交"
            submit.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_grey, null)
        }
    }

    private fun spinnerListener() {
        from.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        type = ""
                    }

                    1 -> {
                        type = "1"
                    }

                    2 -> {
                        type = "2"
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initRecyclerView() {
        mList.clear()
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_add_pic)
        mList.add(bitmap)

        val width = AppUtil.getScreenWidth(this)
        mAdapter = DataAdapter.Builder<Bitmap>()
            .setData(mList)
            .setLayoutId(R.layout.item_pic_feedback)
            .addBindView { itemView, itemData, position ->
                val layout = itemView.layoutParams
                layout.width = width / 5
                layout.height = width / 5
                itemView.layoutParams = layout

                if (position == 3) {
                    itemView.visibility = View.GONE
                }

                Glide.with(this).load(itemData).into(itemView.rv_pic)

                itemView.setOnClickListener {
                    if (position == mList.size - 1) {
                        chooseAlbum()
                    }
                }
            }
            .create()

        picRv.layoutManager = GridLayoutManager(this, 3)
        picRv.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
    }

    private fun submit() {

        if (value == "success") {
            ToastUtil.show(this, "您已经提交过投诉与退款申请了，请耐心等待我们与您联系")
            return
        }

        if (type.isEmpty()) {
            ToastUtil.show(this, "请选择问题类型")
            return
        }

        val account = account.text.trim()
        if (account.isEmpty()) {
            ToastUtil.show(this, "支付宝账号不能为空")
            return
        }

        val contact = contact.text.trim()
        if (contact.isEmpty() || contact.length != 11) {
            ToastUtil.show(this, "手机号输入有误")
            return
        }

        val text = content.text.trim()
        if (text.length <= 10) {
            ToastUtil.show(this, "描述内容不少于10个字")
            return
        }

        if (uploadList.isEmpty()) {
            ToastUtil.show(this, "请添加图片")
            return
        }

        PayManager.getInstance().checkFixPay(this) {
            if (it) {
                submit.isEnabled = false
                submit.text = "提交中"
                submit.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_grey, null)

                uploadPics(type, account.toString(), contact.toString(), text.toString())
            } else {
                ToastUtil.showShort(this, "很抱歉，此功能仅向付费用户开放！")
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

    private fun uploadPics(type: String, account: String, phone: String, description: String) {
        launch(Dispatchers.IO) {
            OssLoader.getOssToken(Constant.CLIENT_TOKEN)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({ ossParam ->
                    val list = arrayListOf<String>()
                    for ((position, item) in uploadList.withIndex()) {
                        OSSManager.get().uploadFileToComplaint(this@FeedbackActivity, ossParam, item, object : UploadCallback {
                            override fun onSuccess(path: String) {
                                list.add(path)
                                if (list.size > 0 && position == uploadList.size - 1) {
                                    val gson = Gson()
                                    val json = gson.toJson(list)

                                    val userInfo = MMKV.defaultMMKV()?.decodeParcelable("userInfo", UserInfo::class.java)
                                    if (userInfo != null) {
                                        report(userInfo.id.toString(), type, account, phone, description, json)
                                    }
                                }
                            }

                            override fun onFailed(msg: String) {
                                launch(Dispatchers.Main) {
                                    ToastUtil.showShort(this@FeedbackActivity, "上传图片失败")
                                }
                            }
                        })
                    }
                }, {
                    ToastUtil.show(this@FeedbackActivity, "请求失败，请检查网络")
                })
        }
    }


    private fun report(uid: String, type: String, account: String, phone: String, description: String, pic: String) {
        launch(Dispatchers.IO) {
            ComplaintLoader.reportComplaint(uid, type, phone, account, description, pic)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    MMKV.defaultMMKV()?.encode("report", "success")
                    ToastUtil.show(this@FeedbackActivity, "提交成功")
                    submit.text = "已提交"
                }, {
                    submit.isEnabled = true
                    submit.text = "提交"
                    submit.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_green, null)
                    val error = it as ApiException
                    ToastUtil.show(this@FeedbackActivity, error.displayMessage)
                })
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x1001) {
            if (data != null) {
                val uri = data.data
                if (uri != null) {
                    val length = contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0
                    val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                    JLog.i("bitmap size = $length")
                    if (length > 3 * 1024 * 1024) {
                        ToastUtil.show(this, "上传图片不要大于3MB")
                    } else {
                        mList.add(0, bitmap)
                        uploadList.add(0, uri)
                        mAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        mList.clear()
        uploadList.clear()

    }

}