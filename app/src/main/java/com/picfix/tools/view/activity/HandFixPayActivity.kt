package com.picfix.tools.view.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.picfix.tools.R
import com.picfix.tools.adapter.DataAdapter
import com.picfix.tools.bean.FileBean
import com.picfix.tools.bean.Price
import com.picfix.tools.bean.Resource
import com.picfix.tools.bean.SimplePrice
import com.picfix.tools.callback.DialogCallback
import com.picfix.tools.callback.PayCallback
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.ImageManager
import com.picfix.tools.controller.LogReportManager
import com.picfix.tools.controller.PayManager
import com.picfix.tools.controller.WxManager
import com.picfix.tools.http.loader.OrderDetailLoader
import com.picfix.tools.http.response.ResponseTransformer
import com.picfix.tools.http.schedulers.SchedulerProvider
import com.picfix.tools.utils.AppUtil
import com.picfix.tools.utils.JLog
import com.picfix.tools.utils.RomUtil
import com.picfix.tools.utils.ToastUtil
import com.picfix.tools.view.base.BaseActivity
import com.picfix.tools.view.views.AutoTextView
import com.picfix.tools.view.views.PaySuccessDialog
import com.picfix.tools.view.views.QuitDialog
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.heart_small.view.*
import kotlinx.android.synthetic.main.item_voice.view.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.*

class HandFixPayActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var pay: Button
    private lateinit var userAgreement: AppCompatCheckBox
    private lateinit var wechatPay: AppCompatCheckBox
    private lateinit var aliPay: AppCompatCheckBox
    private lateinit var titleName: TextView
    private lateinit var wxPayLayout: LinearLayout
    private lateinit var discount: TextView

    private lateinit var discountWx: TextView
    private lateinit var menuBox: RecyclerView
    private lateinit var priceRecyclerView: RecyclerView

    private var mAdapter: DataAdapter<SimplePrice>? = null
    private var mList = arrayListOf<SimplePrice>()

    private var serviceId: Int = 0
    private var currentServiceId = 0

    private var lastClickTime: Long = 0L

    private var mPrice = 0f
    private var currentPosition = 0

    private lateinit var counter: TextView
    private lateinit var counterTimer: CountDownTimer
    private lateinit var timer: CountDownTimer
    private lateinit var customerAgreement: TextView
    private lateinit var notice: AutoTextView

    private var remindTime = 15 * 60 * 1000L
    private var kv: MMKV? = MMKV.defaultMMKV()
    private var orderSn = ""
    private var startPay = false

    override fun setLayout(): Int {
        return R.layout.a_hand_fix_pay
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initView() {
        back = findViewById(R.id.iv_back)
        pay = findViewById(R.id.do_pay)
        wechatPay = findViewById(R.id.do_wechat_pay)
        aliPay = findViewById(R.id.do_alipay_pay)
        titleName = findViewById(R.id.pay_content)

        counter = findViewById(R.id.counter)
        notice = findViewById(R.id.tv_notice)
        customerAgreement = findViewById(R.id.customer_agreement)
        userAgreement = findViewById(R.id.user_agreement)
        discount = findViewById(R.id.discount)
        discountWx = findViewById(R.id.discount_wx)
        menuBox = findViewById(R.id.menu_box)
        priceRecyclerView = findViewById(R.id.ll)

        wxPayLayout = findViewById(R.id.layout_wx_pay)

        back.setOnClickListener { onBackPressed() }
        pay.setOnClickListener { checkPay(this) }
        customerAgreement.setOnClickListener { toAgreementPage() }


        //选择微信支付
        wechatPay.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                aliPay.isChecked = false
            }
        }

        //选择支付宝支付
        aliPay.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                wechatPay.isChecked = false
            }
        }

        initNotice()
        initCounter()
    }

    override fun onResume() {
        super.onResume()
        if (startPay) {
            checkPayResult()
        }
    }

    override fun initData() {
        loadMenuBox()
        loadPriceBox()
        getServicePrice()
    }

    private fun initNotice() {
        timer = object : CountDownTimer(4000 * 1000L, 4000) {
            override fun onFinish() {

            }

            override fun onTick(millisUntilFinished: Long) {
                val str = WxManager.getInstance(this@HandFixPayActivity).getRecoveryUser()
                notice.setText(str, Color.GRAY)
            }
        }

        timer.start()
    }

    private fun initCounter() {
        val result = kv?.decodeLong("hand_fix_pay_counter")
        remindTime = if (result == 0L) 15 * 60 * 1000L else result!!

        counterTimer = object : CountDownTimer(remindTime, 100 / 6L) {
            override fun onFinish() {
                val text = AppUtil.timeStamp2Date("0", "mm:ss:SS")
                counter.text = text
                kv?.encode("hand_fix_pay_counter", 15 * 60 * 1000L)
            }

            override fun onTick(millisUntilFinished: Long) {
                val text = AppUtil.timeStamp2Date(millisUntilFinished.toString(), "mm:ss:SS")
                counter.text = text
                remindTime = millisUntilFinished
            }
        }
    }


    private fun loadMenuBox() {
        val list = arrayListOf<Resource>()

        list.add(Resource("3", R.drawable.iv_hand_06, "专业团队"))
        list.add(Resource("2", R.drawable.iv_hand_05, "支持退款"))
        list.add(Resource("2", R.drawable.iv_hand_10, "效率保障"))
        list.add(Resource("2", R.drawable.iv_hand_07, "质量保证"))
        list.add(Resource("2", R.drawable.iv_hand_08, "专人服务"))
        list.add(Resource("5", R.drawable.iv_hand_09, "隐私保护"))

        val adapter = DataAdapter.Builder<Resource>()
            .setData(list)
            .setLayoutId(R.layout.heart_small)
            .addBindView { itemView, itemData ->
                Glide.with(this).load(itemData.icon).into(itemView.iv_icon)
                itemView.tv_name.text = itemData.name
            }
            .create()

        menuBox.layoutManager = GridLayoutManager(this, 3)
        menuBox.adapter = adapter
        adapter.notifyItemRangeInserted(0, list.size)
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun loadPriceBox() {
        val width = AppUtil.getScreenWidth(this)
        mAdapter = DataAdapter.Builder<SimplePrice>()
            .setData(mList)
            .setLayoutId(R.layout.item_voice)
            .addBindView { itemView, itemData, position ->

                val layoutParams = itemView.layoutParams
                layoutParams.width = width / 4
                itemView.layoutParams = layoutParams

                if (position == 0) {
                    mPrice = itemData.server_price.toFloat()
                    itemView.hot.visibility = View.VISIBLE
                } else {
                    itemView.hot.visibility = View.GONE
                }

                if (position == currentPosition) {
                    itemView.title.setTextColor(ResourcesCompat.getColor(resources, R.color.color_content, null))
                    itemView.price.setTextColor(ResourcesCompat.getColor(resources, R.color.color_red_price, null))
                    itemView.money.setImageResource(R.drawable.rmb_red)
                    itemView.setBackgroundResource(R.drawable.background_gradient_stroke)
                } else {
                    itemView.title.setTextColor(ResourcesCompat.getColor(resources, R.color.color_dark_grey, null))
                    itemView.price.setTextColor(ResourcesCompat.getColor(resources, R.color.color_orange_price, null))
                    itemView.money.setImageResource(R.drawable.rmb_orange)
                    itemView.setBackgroundResource(R.drawable.pay_background_nomal)
                }

                itemView.title.text = itemData.server_name
                itemView.price.text = String.format("%.1f", itemData.server_price.toFloat())

                itemView.setOnClickListener {
                    mPrice = itemData.server_price.toFloat()
                    currentServiceId = itemData.server_id
                    currentPosition = position
                    mAdapter!!.notifyDataSetChanged()
                }
            }
            .create()

        priceRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        priceRecyclerView.adapter = mAdapter
    }


    private fun toAgreementPage() {
        val intent = Intent()
        intent.setClass(this, AgreementActivity::class.java)
        startActivity(intent)
    }

    private fun getServicePrice() {

        PayManager.getInstance().getServiceList(this) {
            val packDetails = arrayListOf<Price>()
            for (child in it) {
                if (child.server_code == Constant.PHOTO_HAND_FIX) {
                    packDetails.add(child)
                }
            }

            if (packDetails.isEmpty()) {
                ToastUtil.showShort(this, "已付费")
                finish()
                return@getServiceList
            }

            if (packDetails.isNotEmpty()) {
                //多项套餐或者补价套餐
                for (child in packDetails) {
                    if (child.server_code == Constant.PHOTO_HAND_FIX) {
                        if (currentServiceId == 0) {
                            currentServiceId = child.id
                        }
                        mList.add(SimplePrice(child.id, child.server_name, child.sale_price))
                    }
                }

                //刷新价格
                changeDescription()
            }
        }
    }


    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun changeDescription() {
        pay.visibility = View.VISIBLE
        counterTimer.start()
        mAdapter!!.notifyDataSetChanged()
    }

    private fun checkPay(c: Activity) {
        if (!userAgreement.isChecked) {
            ToastUtil.show(this, "请阅读并勾选《会员须知》")
            return
        }

        if (!wechatPay.isChecked && !aliPay.isChecked) {
            ToastUtil.show(this, "请选择付款方式")
            return
        }

        if (lastClickTime == 0L) {
            lastClickTime = System.currentTimeMillis()
        } else if (System.currentTimeMillis() - lastClickTime < 2 * 1000) {
            ToastUtil.showShort(c, "请不要频繁发起支付")
            return
        }

        lastClickTime = System.currentTimeMillis()

        if (wechatPay.isChecked) {
            startPay = true
            doPay(c, 2)
        } else {
            startPay = false
            doPay(c, 1)
        }
    }

    /**
     *  index = 0快速支付 1支付宝支付 2微信支付
     */
    private fun doPay(c: Activity, index: Int) {
        when (index) {
            0 -> PayManager.getInstance().doFastPay(c, currentServiceId, object : PayCallback {
                override fun success() {
                }

                override fun progress(orderId: String) {
                    orderSn = orderId
                }

                override fun failed(msg: String) {
                    launch(Dispatchers.Main) {
                        ToastUtil.showShort(c, msg)
                    }
                }
            })

            1 -> PayManager.getInstance().doAtfPay(c, currentServiceId, object : PayCallback {
                override fun success() {
                    launch(Dispatchers.Main) {

                        //返回支付结果
                        ToastUtil.showShort(c, "支付成功")

                        if (AppUtil.getChannelId() == Constant.CHANNEL_OPPO) {
                            launch(Dispatchers.IO) {
                                ImageManager.reportToOPPO(this@HandFixPayActivity, System.currentTimeMillis(), 7, (mPrice * 100).toInt())
                            }
                        }

                        LogReportManager.logReport("修复支付页", "成功付费${mPrice}", LogReportManager.LogType.ORDER)

                        openPaySuccessDialog()
                    }
                }

                override fun progress(orderId: String) {
                    orderSn = orderId
                }

                override fun failed(msg: String) {
                    launch(Dispatchers.Main) {
                        ToastUtil.showShort(c, msg)
                    }
                }
            })

            2 -> PayManager.getInstance().doWechatPay(c, currentServiceId, object : PayCallback {
                override fun success() {
                }

                override fun progress(orderId: String) {
                    JLog.i("orderId = $orderId")
                    orderSn = orderId
                }

                override fun failed(msg: String) {
                }
            })
        }

    }

    private fun checkPayResult() {
        JLog.i("orderSn = $orderSn")
        if (orderSn == "") return
        launch(Dispatchers.IO) {
            OrderDetailLoader.getOrderStatus(orderSn)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    JLog.i("order_sn = ${it.order_sn}")
                    if (it.order_sn != orderSn) {
                        return@subscribe
                    }

                    when (it.status) {
                        "1" -> {

                            openPaySuccessDialog()

                            //返回支付结果
                            ToastUtil.showShort(this@HandFixPayActivity, "支付成功")

                            if (AppUtil.getChannelId() == Constant.CHANNEL_OPPO) {
                                launch(Dispatchers.IO) {
                                    ImageManager.reportToOPPO(this@HandFixPayActivity, System.currentTimeMillis(), 7, (mPrice * 100).toInt())
                                }
                            }

                            LogReportManager.logReport("修复支付页", "成功付费${mPrice}", LogReportManager.LogType.ORDER)
                        }

                        else -> {
                            ToastUtil.show(this@HandFixPayActivity, "未支付")
                        }
                    }

                }, {
                    ToastUtil.show(this@HandFixPayActivity, "查询支付结果失败")
                })
        }

    }

    private fun toPaySuccessPage() {
        val intent = Intent(this, PaySuccessActivity::class.java)
        intent.putExtra("serviceId", serviceId)
        startActivity(intent)
        finish()
    }

    private fun openPaySuccessDialog() {
        PaySuccessDialog(this@HandFixPayActivity, object : DialogCallback {
            override fun onSuccess(file: FileBean) {
                setResult(0x100)
                finish()
            }

            override fun onCancel() {
            }
        }).show()
    }


    override fun onBackPressed() {
        QuitDialog(this, getString(R.string.quite_title), object : DialogCallback {
            override fun onSuccess(file: FileBean) {
                finish()
            }

            override fun onCancel() {
            }
        }).show()
    }

    override fun onDestroy() {
        super.onDestroy()

        timer.cancel()
        counterTimer.cancel()

        if (kv != null && remindTime != 0L) {
            kv?.encode("hand_fix_pay_counter", remindTime)
        }
    }


}