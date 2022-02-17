package com.picfix.tools.view.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.content.res.ResourcesCompat
import com.tencent.mmkv.MMKV
import com.picfix.tools.R
import com.picfix.tools.bean.FileBean
import com.picfix.tools.callback.DialogCallback
import com.picfix.tools.callback.PayCallback
import com.picfix.tools.config.Constant
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
import kotlinx.android.synthetic.main.heart_small.view.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.*


class RecoveryPayActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var pay: Button
    private lateinit var userAgreement: AppCompatCheckBox
    private lateinit var wechatPay: AppCompatCheckBox
    private lateinit var aliPay: AppCompatCheckBox
    private lateinit var titleName: TextView

    private lateinit var firstLayout: FrameLayout
    private lateinit var wxPayLayout: LinearLayout
    private lateinit var introduce: TextView

    private lateinit var firstPriceView: TextView
    private lateinit var firstOriginPriceView: TextView

    private lateinit var discount: TextView
    private lateinit var discountWx: TextView

    private var serviceId: Int = 0
    private var currentServiceId = 0
    private var firstServiceId = 0
    private var checkItem = 0

    private var lastClickTime: Long = 0L

    private var mPrice = 0f
    private var firstPrice = 0f
    private var firstOriginalPrice = 0f

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
        return R.layout.a_recovery_pay
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initView() {
        back = findViewById(R.id.iv_back)
        pay = findViewById(R.id.do_pay)
        wechatPay = findViewById(R.id.do_wechat_pay)
        aliPay = findViewById(R.id.do_alipay_pay)
        titleName = findViewById(R.id.pay_content)
        introduce = findViewById(R.id.introduce)

        firstPriceView = findViewById(R.id.price)
        firstOriginPriceView = findViewById(R.id.original_price)

        counter = findViewById(R.id.counter)
        notice = findViewById(R.id.tv_notice)
        customerAgreement = findViewById(R.id.customer_agreement)
        userAgreement = findViewById(R.id.user_agreement)
        discount = findViewById(R.id.discount)
        discountWx = findViewById(R.id.discount_wx)

        firstLayout = findViewById(R.id.ll_1)
        wxPayLayout = findViewById(R.id.layout_wx_pay)

        //原价删除线
        firstOriginPriceView.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG

        back.setOnClickListener { onBackPressed() }
        pay.setOnClickListener { checkPay(this) }
        firstLayout.setOnClickListener { chooseMenu(1) }
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


        kv = MMKV.defaultMMKV()

        initNotice()
        initCounter()
        chooseMenu(1)

    }

    override fun onResume() {
        super.onResume()
        if (startPay) {
            checkPayResult()
        }
    }

    override fun initData() {
        serviceId = intent.getIntExtra("serviceId", 0)
        getServicePrice()
    }

    private fun initNotice() {
        timer = object : CountDownTimer(4000 * 1000L, 4000) {
            override fun onFinish() {

            }

            override fun onTick(millisUntilFinished: Long) {
                val str = WxManager.getInstance(this@RecoveryPayActivity).getRecoveryUser()
                notice.setText(str, Color.GRAY)
            }
        }

        timer.start()
    }

    private fun initCounter() {
        val result = kv?.decodeLong("recovery_pay_counter")
        remindTime = if (result == 0L) 15 * 60 * 1000L else result!!

        counterTimer = object : CountDownTimer(remindTime, 100 / 6L) {
            override fun onFinish() {
                val text = AppUtil.timeStamp2Date("0", "mm:ss:SS")
                counter.text = text
                kv?.encode("recovery_pay_counter", 15 * 60 * 1000L)
            }

            override fun onTick(millisUntilFinished: Long) {
                val text = AppUtil.timeStamp2Date(millisUntilFinished.toString(), "mm:ss:SS")
                counter.text = text
                remindTime = millisUntilFinished
            }
        }
    }


    private fun chooseMenu(index: Int) {
        when (index) {
            1 -> {
                firstLayout.background = ResourcesCompat.getDrawable(resources, R.drawable.background_gradient_stroke, null)
                checkItem = 1
            }
        }

    }


    private fun toAgreementPage() {
        val intent = Intent()
        intent.setClass(this, AgreementActivity::class.java)
        startActivity(intent)
    }

    private fun getServicePrice() {

        PayManager.getInstance().getPayStatus(this, Constant.COM + Constant.EXPIRE_TYPE_FOREVER) {
            discount.text = "支付立减${it.discountFee}"
            discountWx.text = "支付立减${it.discountFee}"

            val packDetails = it.packDetail

            if (packDetails.isEmpty()) {
                ToastUtil.showShort(this, "已付费")
                finish()
                return@getPayStatus
            }

            //单项补价套餐
            if (packDetails.size == 1) {
                firstServiceId = packDetails[0]!!.id
                firstPrice = packDetails[0]!!.sale_price.toFloat()
                firstOriginalPrice = packDetails[0]!!.server_price.toFloat()
                currentServiceId = firstServiceId
                introduce.text = packDetails[0]!!.desc
                mPrice = firstPrice
            }

            //多项套餐或者补价套餐
            if (packDetails.size == 2) {
                for (child in packDetails) {
                    if (child!!.server_code == Constant.COM) {
                        firstServiceId = child.id
                        firstPrice = child.sale_price.toFloat()
                        firstOriginalPrice = child.server_price.toFloat()
                        currentServiceId = firstServiceId
                        introduce.text = child.desc
                        mPrice = firstPrice
                    }
                }
            }

            //刷新价格
            changeDescription()
        }
    }


    private fun changeDescription() {
        pay.visibility = View.VISIBLE

        firstPriceView.text = String.format("%.0f", firstPrice)
        firstOriginPriceView.text = String.format("%.0f", firstOriginalPrice)
        counterTimer.start()

        firstPriceView.text = String.format("%.0f", firstPrice)
        firstOriginPriceView.text = String.format("%.0f", firstOriginalPrice)
        counterTimer.start()
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

        when (checkItem) {
            1 -> currentServiceId = firstServiceId
            0 -> return
        }

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

            1 -> PayManager.getInstance().doAliPay(c, currentServiceId, object : PayCallback {
                override fun success() {
                    launch(Dispatchers.Main) {

                        //返回支付结果
                        ToastUtil.showShort(c, "支付成功")

                        LogReportManager.logReport("恢复支付页", "成功付费${mPrice}", LogReportManager.LogType.ORDER)

//                        if (currentServiceId == secondServiceId) {
//                            toPaySuccessPage()
//                        } else {
                        openPaySuccessDialog()
//                        }
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

//                            if (currentServiceId == secondServiceId) {
//                                toPaySuccessPage()
//                            } else {
                            openPaySuccessDialog()
//                            }

                            //返回支付结果
                            ToastUtil.showShort(this@RecoveryPayActivity, "支付成功")

                            LogReportManager.logReport("恢复支付页", "成功付费${mPrice}", LogReportManager.LogType.ORDER)
                        }

                        else -> {
                            ToastUtil.show(this@RecoveryPayActivity, "未支付")
                        }
                    }

                }, {
                    ToastUtil.show(this@RecoveryPayActivity, "查询支付结果失败")
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
        PaySuccessDialog(this@RecoveryPayActivity, object : DialogCallback {
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
            kv?.encode("recovery_pay_counter", remindTime)
        }
    }


}