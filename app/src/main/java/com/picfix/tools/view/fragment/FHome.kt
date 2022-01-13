package com.picfix.tools.view.fragment

import android.content.Intent
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.picfix.tools.R
import com.picfix.tools.adapter.DataAdapter
import com.picfix.tools.bean.Resource
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.IMManager
import com.picfix.tools.utils.*
import com.picfix.tools.view.activity.*
import com.picfix.tools.view.base.BaseFragment
import com.picfix.tools.view.views.FixTipsDialog
import com.picfix.tools.view.views.ScaleInTransformer
import kotlinx.android.synthetic.main.item_heart.view.*
import kotlinx.android.synthetic.main.item_home_pic.view.*
import kotlinx.android.synthetic.main.item_home_pic.view.pic_src
import kotlinx.android.synthetic.main.item_other.view.*
import kotlinx.android.synthetic.main.item_recommend.view.*
import kotlinx.coroutines.*
import java.util.*

open class FHome : BaseFragment() {
    private lateinit var rv: RecyclerView
    private lateinit var pics: RecyclerView
    private lateinit var mine: ImageView
    private lateinit var pager: ViewPager2
    private lateinit var ruleTips: ImageView
    private lateinit var recommend: RecyclerView
    private lateinit var ask: ImageView
    private lateinit var pay: ImageView

    private lateinit var pagerAdapter: DataAdapter<Resource>
    private lateinit var picAdapter: DataAdapter<Resource>
    private var mainPics = mutableListOf<Resource>()
    private var pagerPics = mutableListOf<Resource>()
    private var picsList = mutableListOf<Resource>()

    private var firstLoad = true

    override fun initView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.f_home, container, false)
        mine = rootView.findViewById(R.id.mine)
        pager = rootView.findViewById(R.id.pager)
        pics = rootView.findViewById(R.id.show_recyclerview)
        ruleTips = rootView.findViewById(R.id.rule_tips)
        recommend = rootView.findViewById(R.id.recommend)
        ask = rootView.findViewById(R.id.ask)
        pay = rootView.findViewById(R.id.pay)

        mine.setOnClickListener { toMinePage() }
        ruleTips.setOnClickListener { showTipsDialog() }
        ask.setOnClickListener { startConversation() }
        pay.setOnClickListener { openPay() }

        initPager()
        initPicsShow()
        initRecommend()

        return rootView
    }

    override fun initData() {
        mainPics.clear()
        mainPics.add(Resource("pic", R.drawable.banner_01, "图片恢复"))
        mainPics.add(Resource("audio", R.drawable.banner_02, "语音恢复"))
        mainPics.add(Resource("video", R.drawable.banner_03, "视频恢复"))

        pagerPics.clear()
        pagerPics.add(Resource("pic", R.drawable.bg_home_01, "人像精修"))
        pagerPics.add(Resource("audio", R.drawable.bg_home_02, "证件照"))
        pagerPics.add(Resource("video", R.drawable.bg_home_03, "图片编辑"))
        pagerPics.add(Resource("doc", R.drawable.bg_home_04, "创意修图"))

        picsList.clear()
        picsList.add(Resource("pic", R.drawable.pic_home_case1, "人像精修"))
        picsList.add(Resource("audio", R.drawable.pic_home_case2, "证件照"))
        picsList.add(Resource("video", R.drawable.pic_home_case3, "图片编辑"))
        picsList.add(Resource("doc", R.drawable.pic_home_case4, "创意修图"))
        picsList.add(Resource("doc", R.drawable.pic_home_case5, "创意修图"))
        picsList.add(Resource("doc", R.drawable.pic_home_case6, "创意修图"))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity!!.window.statusBarColor = ContextCompat.getColor(activity!!, R.color.color_pink)
        }

    }

    override fun onResume() {
        super.onResume()
        initCustomerService()
    }


    override fun click(v: View?) {
    }


    private fun initCustomerService() {
        IMManager.setMessageListener {
            AppUtil.sendNotification(activity, Constant.Notification_title, Constant.Notification_content)
        }
    }

    private fun initPager() {

        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (firstLoad) {
                    pager.setCurrentItem(1, true)
                    firstLoad = false
                }
            }

        })

        pagerAdapter = DataAdapter.Builder<Resource>()
            .setData(mainPics)
            .setLayoutId(R.layout.item_heart)
            .addBindView { itemView, itemData ->
                Glide.with(this).load(itemData.icon).into(itemView.img)
            }
            .create()

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(ScaleInTransformer())
        compositePageTransformer.addTransformer(MarginPageTransformer(resources.getDimension(R.dimen.dp_5).toInt()))

        pager.apply {
            adapter = pagerAdapter
            setPageTransformer(compositePageTransformer)
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            offscreenPageLimit = 3
            (getChildAt(0) as RecyclerView).apply {
                val padding = resources.getDimensionPixelOffset(R.dimen.dp_20)
                setPadding(padding, 0, padding, 0)
                clipToPadding = false
            }
        }
    }

    private fun initPicsShow() {
        val width = AppUtil.getScreenWidth(activity!!)
        picAdapter = DataAdapter.Builder<Resource>()
            .setData(picsList)
            .setLayoutId(R.layout.item_home_long_size_pic)
            .addBindView { itemView, itemData ->
                val layout = itemView.layoutParams
                layout.width = width / 3 - 20
                itemView.layoutParams = layout

                Glide.with(this).load(itemData.icon).into(itemView.pic_src)
            }
            .create()

        pics.adapter = picAdapter
        pics.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        picAdapter.notifyItemRangeInserted(0, picsList.size)
    }

    private fun initRecommend() {
        val list = mutableListOf<Resource>()
        list.add(Resource("用户xf*****的评论：", R.drawable.ic_avtar_01, "客服很耐心，都是按照要求修的，而且修的效果很好很满意，必须给工作人员加鸡腿。"))
        list.add(Resource("用户流*****的评论", R.drawable.ic_avtar_02, "修复后很清晰，脸上很自然，非常不错的修图师"))
        list.add(Resource("用户An*****的评论", R.drawable.ic_avtar_03, "人工修复的效果很好，价格也很实惠。比去照相馆便宜太多了！"))
        list.add(Resource("用户阿*****的评论", R.drawable.ic_avtar_04, "创意修图，提的要求设计师都给满足了，棒棒哒"))

        val width = AppUtil.getScreenWidth(activity!!)
        val adapter = DataAdapter.Builder<Resource>()
            .setData(list)
            .setLayoutId(R.layout.item_recommend)
            .addBindView { itemView, itemData ->
                val layout = itemView.layoutParams
                layout.width = (width / 1.5f).toInt()
                itemView.layoutParams = layout

                Glide.with(this).load(itemData.icon).into(itemView.pic_src)
                itemView.recommend_title.text = itemData.type
                itemView.recommend_content.text = itemData.name
            }
            .create()

        recommend.adapter = adapter
        recommend.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        adapter.notifyItemRangeInserted(0, list.size)
    }


    private fun showTipsDialog() {
        FixTipsDialog(activity!!).show()
    }


    private fun toMinePage() {
        val intent = Intent()
        intent.setClass(activity!!, MineActivity::class.java)
        startActivity(intent)
    }

    private fun openPay() {
        if (Constant.USER_ID != "") {
            val intent = Intent()
            intent.setClass(activity!!, HandFixPayActivity::class.java)
            startActivity(intent)
        } else {
            startActivity(Intent(activity, LoginActivity::class.java))
        }
    }

    private fun startConversation() {
        if (Constant.USER_ID != "") {
            IMManager.register(Constant.USER_ID, {
                IMManager.startConversation(activity!!, Constant.USER_ID, "人工修图", false)
            }, { JLog.i("error = " + it!!) })
        } else {
            startActivity(Intent(activity, LoginActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
        IMManager.removeMessageListener()
        IMManager.logout()
    }
}