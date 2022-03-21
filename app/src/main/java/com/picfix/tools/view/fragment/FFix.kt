package com.picfix.tools.view.fragment

import android.content.Intent
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.picfix.tools.R
import com.picfix.tools.adapter.DataAdapter
import com.picfix.tools.bean.Resource
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.ImageManager
import com.picfix.tools.utils.*
import com.picfix.tools.view.activity.*
import com.picfix.tools.view.base.BaseFragment
import kotlinx.android.synthetic.main.item_heart.view.*
import kotlinx.android.synthetic.main.item_home_pic.view.*
import kotlinx.android.synthetic.main.item_other.view.*
import kotlinx.coroutines.*
import java.util.*

open class FFix : BaseFragment() {
    private lateinit var rv: RecyclerView
    private lateinit var otherRv: RecyclerView
    private lateinit var mainAdapter: DataAdapter<Resource>
    private lateinit var otherAdapter: DataAdapter<Resource>
    private lateinit var fix: ImageView
    private lateinit var removeWatermark: ImageView
    private lateinit var customer: ImageView
    private var mainPics = mutableListOf<Resource>()
    private var otherPics = mutableListOf<Resource>()
    private var lastClickTime = 0L

    override fun initView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.f_fix, container, false)
        rv = rootView.findViewById(R.id.recommend_tools)
        otherRv = rootView.findViewById(R.id.other_tools)

        fix = rootView.findViewById(R.id.photo_fix)
        removeWatermark = rootView.findViewById(R.id.photo_remove_watermark)
        customer = rootView.findViewById(R.id.user_service)

        fix.setOnClickListener { checkPermissions { toPhotoDefinitionPage() } }
        removeWatermark.setOnClickListener { checkPermissions { toPhotoWatermarkPage() } }
        customer.setOnClickListener { toCustomerServicePage() }

        return rootView
    }

    override fun initData() {
        mainPics.clear()
        mainPics.add(Resource("cartoon", R.drawable.home_case_3, "人像动漫画"))
        mainPics.add(Resource("face_merge", R.drawable.home_case_5, "人脸融合"))
        mainPics.add(Resource("matting", R.drawable.home_more5, "一键去背景"))
        mainPics.add(Resource("colour", R.drawable.home_case_2, "照片上色"))
        mainPics.add(Resource("format_trans", R.drawable.home_more8, "格式转换"))
        mainPics.add(Resource("zip", R.drawable.home_more9, "图片压缩"))

        otherPics.clear()
        otherPics.add(Resource("contrast", R.drawable.home_more1, "对比度增强"))
        otherPics.add(Resource("resize", R.drawable.home_more2, "无损放大"))
        otherPics.add(Resource("defogging", R.drawable.home_more3, "图片去雾"))
        otherPics.add(Resource("style_trans", R.drawable.home_more10, "风格转化"))
        otherPics.add(Resource("age_trans", R.drawable.home_more11, "年龄转换"))
        otherPics.add(Resource("gender_trans", R.drawable.home_more12, "性别转换"))
        otherPics.add(Resource("morph", R.drawable.home_more13, "人像渐变"))
        otherPics.add(Resource("stretch", R.drawable.home_more4, "拉伸图像恢复"))

        initMainRecycleView()
        initOtherRecycleView()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity!!.window.statusBarColor = ContextCompat.getColor(activity!!, R.color.color_white)
        }
    }


    override fun click(v: View?) {
    }

    private fun initMainRecycleView() {
        mainAdapter = DataAdapter.Builder<Resource>()
            .setData(mainPics)
            .setLayoutId(R.layout.item_fix_pic)
            .addBindView { itemView, itemData ->
                itemView.pic_src.setImageDrawable(ResourcesCompat.getDrawable(resources, itemData.icon, null))

                itemView.setOnClickListener {
                    if (lastClickTime == 0L) {
                        lastClickTime = System.currentTimeMillis()
                    } else {
                        if (System.currentTimeMillis() - lastClickTime < 1000) return@setOnClickListener
                    }

                    lastClickTime = System.currentTimeMillis()

                    checkPermissions {
                        when (itemData.type) {
                            "cartoon" -> toPhotoCartoonPage()
                            "face_merge" -> toPhotoFaceMergePage()
                            "matting" -> toPhotoMattingPage()
                            "colour" -> toPhotoColourPage()
                            "format_trans" -> toPhotoFormatTransPage()
                            "zip" -> toPhotoZipPage()
                        }
                    }
                }
            }
            .create()

        rv.layoutManager = GridLayoutManager(activity, 2)
        rv.adapter = mainAdapter
        mainAdapter.notifyItemRangeChanged(0, mainPics.size)
    }

    private fun initOtherRecycleView() {

        otherAdapter = DataAdapter.Builder<Resource>()
            .setData(otherPics)
            .setLayoutId(R.layout.item_other)
            .addBindView { itemView, itemData ->
                Glide.with(this).load(itemData.icon).into(itemView.iv_other_icon)
                itemView.tv_other_name.text = itemData.name

                itemView.setOnClickListener {
                    if (lastClickTime == 0L) {
                        lastClickTime = System.currentTimeMillis()
                    } else {
                        if (System.currentTimeMillis() - lastClickTime < 1000) return@setOnClickListener
                    }

                    checkPermissions {
                        when (itemData.type) {
                            "style_trans" -> toPhotoTransStylePage()
                            "contrast" -> toPhotoContrastPage()
                            "resize" -> toPhotoResizePage()
                            "defogging" -> toPhotoDefoggingPage()
                            "stretch" -> toPhotoStretchPage()
                            "age_trans" -> toPhotoAgeTransPage()
                            "gender_trans" -> toPhotoGenderTransPage()
                            "morph" -> toPhotoMorphPage()
                        }
                    }
                }
            }
            .create()

        otherRv.layoutManager = GridLayoutManager(activity, 4)
        otherRv.adapter = otherAdapter
        otherAdapter.notifyItemRangeChanged(0, otherPics.size)
    }


    private fun toCustomerServicePage() {
        val intent = Intent()
        intent.setClass(activity!!, CustomerServiceActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoMattingPage() {
        val intent = Intent()
        intent.setClass(activity!!, PhotoMattingActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoColourPage() {
        val intent = Intent()
        intent.setClass(activity!!, PhotoColourActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoWatermarkPage() {
        val intent = Intent()
        intent.setClass(activity!!, PhotoWatermarkActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoResizePage() {
        val intent = Intent()
        intent.setClass(activity!!, PhotoResizeActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoDefinitionPage() {
        val intent = Intent()
        intent.setClass(activity!!, PhotoDefinitionActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoContrastPage() {
        val intent = Intent()
        intent.setClass(activity!!, PhotoContrastActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoCartoonPage() {
        val intent = Intent()
        intent.setClass(activity!!, PhotoCartoonActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoTransStylePage() {
        val intent = Intent()
        intent.setClass(activity!!, PhotoTransStyleActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoDefoggingPage() {
        val intent = Intent()
        intent.setClass(activity!!, PhotoDefoggingActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoStretchPage() {
        val intent = Intent()
        intent.setClass(activity!!, PhotoStretchActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoZipPage() {
        val intent = Intent()
        intent.setClass(activity!!, PhotoZipActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoAgeTransPage() {
        val intent = Intent()
        intent.setClass(activity!!, PhotoAgeTransActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoGenderTransPage() {
        val intent = Intent()
        intent.setClass(activity!!, PhotoGenderTransActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoMorphPage() {
        val intent = Intent()
        intent.setClass(activity!!, PhotoMorphActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoFormatTransPage() {
        val intent = Intent()
        intent.setClass(activity!!, PhotoFormatTransActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoFaceMergePage() {
        val intent = Intent()
        intent.setClass(activity!!, PhotoFaceMergeActivity::class.java)
        startActivity(intent)
    }
}