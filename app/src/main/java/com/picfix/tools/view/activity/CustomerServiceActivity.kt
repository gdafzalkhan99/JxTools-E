package com.picfix.tools.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bytedance.sdk.openadsdk.*
import com.picfix.tools.R
import com.picfix.tools.adapter.DataAdapter
import com.picfix.tools.bean.Resource
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.IMManager
import com.picfix.tools.http.loader.CheckPayLoader
import com.picfix.tools.http.response.ResponseTransformer
import com.picfix.tools.http.schedulers.SchedulerProvider
import com.picfix.tools.utils.AppUtil
import com.picfix.tools.utils.JLog
import com.picfix.tools.utils.ToastUtil
import com.picfix.tools.view.base.BaseActivity
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.item_customer.view.*
import kotlin.concurrent.thread
import kotlin.random.Random

class CustomerServiceActivity : BaseActivity() {
    private lateinit var customer: RecyclerView
    private lateinit var ad: FrameLayout
    private lateinit var back: ImageView
    private var descrption = ""
    private var mmkv = MMKV.defaultMMKV()
    private var isPay = false

    override fun setLayout(): Int {
        return R.layout.a_customer_service
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        customer = findViewById(R.id.customer_service)
        ad = findViewById(R.id.layout_ad)

        back.setOnClickListener { finish() }
    }

    override fun initData() {
        loadCustomerService()
        if (Constant.AD_OPENNING) {
            loadExpressAd()
            return
        }
    }

    private fun loadCustomerService() {
        val list = arrayListOf<Resource>()
        list.add(Resource("wechat", R.drawable.customer_tel, "在线客服"))
        list.add(Resource("doc", R.drawable.customer_gd, "提交反馈"))
        list.add(Resource("doc", R.drawable.common_qs, "常见问题"))

        val mAdapter = DataAdapter.Builder<Resource>()
            .setData(list)
            .setLayoutId(R.layout.item_customer)
            .addBindView { itemView, itemData, position ->
                Glide.with(this).load(itemData.icon).into(itemView.service_icon)
                itemView.service_name.text = itemData.name
                when (position) {
                    0 -> {
                        val text = "在线客服(10:00-22:00)"
                        itemView.tv_service_title.text = text
                        itemView.tv_service_descrition.text = getString(R.string.vip_service_des)
                    }

                    1 -> {
                        itemView.tv_service_title.text = "投诉与退款"
                        itemView.tv_service_descrition.text = getString(R.string.visitor_service_des)
                    }

                    2 -> {
                        itemView.tv_service_title.text = "常见问题回复"
                        itemView.tv_service_descrition.text = getString(R.string.common_questions_des)
                    }
                }

                itemView.setOnClickListener {
                    when (position) {
                        0 -> {
                            if (Constant.USER_NAME == "") {
                                startActivity(Intent(this, LoginActivity::class.java))
                                return@setOnClickListener
                            }

                            checkUserStatus()
                        }

                        1 -> {
                            if (Constant.USER_NAME == "") {
                                startActivity(Intent(this, LoginActivity::class.java))
                                return@setOnClickListener
                            }

                            startActivity(Intent(this, FeedbackActivity::class.java))
                        }

                        2 -> {
                            val intent = Intent()
                            intent.setClass(this, QuestionActivity::class.java)
                            startActivity(intent)
                        }
                    }

                }
            }
            .create()

        customer.layoutManager = LinearLayoutManager(this)
        customer.adapter = mAdapter
        mAdapter.notifyItemRangeChanged(0, list.size)
    }

    private fun loadExpressAd() {
        val width = AppUtil.getScreenWidth(this)
        val mTTAdNative = TTAdSdk.getAdManager().createAdNative(this)
        val adSlot = AdSlot.Builder()
            .setCodeId("946885595")
            .setSupportDeepLink(true)
            .setAdCount(3) //请求广告数量为1到3条
            .setExpressViewAcceptedSize(width * 1.0f, 0f)
            .setAdLoadType(TTAdLoadType.LOAD)
            .build()

        mTTAdNative.loadNativeExpressAd(adSlot, object : TTAdNative.NativeExpressAdListener {
            override fun onError(p0: Int, p1: String?) {
                JLog.i("load ad error code = $p0")
            }

            override fun onNativeExpressAdLoad(p0: MutableList<TTNativeExpressAd>?) {
                if (p0 != null) {
                    JLog.i("ad count is ${p0.size}")
                    if (p0.size > 0) {
                        val index = Random.nextInt(0, 3)
                        val adView = p0[index]
                        adView.render()
                        val view = adView.expressAdView
                        if (view != null) {
                            ad.addView(view)
                        } else {
                            JLog.i("ad view is null")
                        }
                    }
                }
            }
        })
    }

    private fun checkUserStatus() {
        if (Constant.USER_ID != "") {
            ToastUtil.showShort(this, "开启会话中，请稍等...")
            IMManager.register(Constant.USER_ID, { startConversation() }, { JLog.i("error = " + it!!) })
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun startConversation() {
        //防止进入会话发送消息还在回调
        IMManager.removeMessageListener()

        checkPay({
            IMManager.startConversation(this, Constant.USER_ID, descrption, true)
        }) { IMManager.startConversation(this, Constant.USER_ID, descrption, false) }
    }

    /**
     * 检查顾客是否开通了Vip
     */
    @SuppressLint("CheckResult")
    private fun checkPay(pay: () -> Unit, notPay: () -> Unit) {
        if (Constant.CLIENT_TOKEN == "") return

        thread {
            CheckPayLoader.checkPay()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    if (it.msg == "true") {
                        isPay = true
                        pay()
                        descrption = "需要核实具体支付套餐"
                    } else {
                        when (AppUtil.getChannelId()) {
                            Constant.CHANNEL_VIVO, Constant.CHANNEL_HUAWEI -> {
                                val versionCode = AppUtil.getPackageVersionCode(this, packageName)
                                if (versionCode == Constant.APP_VERSION.toInt()) {
                                    pay()
                                    descrption = "参与免费活动进线"
                                    return@subscribe
                                }
                            }
                        }

                        notPay()
                    }
                }, {
                    notPay()
                })
        }
    }

}