package com.picfix.tools.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.picfix.tools.R
import com.picfix.tools.adapter.DataAdapter
import com.picfix.tools.bean.Resource
import com.picfix.tools.bean.UserInfo
import com.picfix.tools.callback.Callback
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.DBManager
import com.picfix.tools.controller.PayManager
import com.picfix.tools.http.loader.AccountLoader
import com.picfix.tools.http.response.ResponseTransformer
import com.picfix.tools.http.schedulers.SchedulerProvider
import com.picfix.tools.utils.AppUtil
import com.picfix.tools.utils.Dict
import com.picfix.tools.utils.FileUtil
import com.picfix.tools.utils.ToastUtil
import com.picfix.tools.view.activity.AgreementActivity
import com.picfix.tools.view.activity.CustomerServiceActivity
import com.picfix.tools.view.activity.LoginActivity
import com.picfix.tools.view.base.BaseFragment
import com.picfix.tools.view.views.AccountDeleteDialog
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.item_function.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class FMine : BaseFragment() {
    private lateinit var title: TextView
    private lateinit var level: TextView
    private lateinit var common: TextView
    private lateinit var phone: TextView
    private lateinit var back: ImageView
    private lateinit var logout: Button
    private lateinit var customer: RecyclerView
    private var mmkv = MMKV.defaultMMKV()

    override fun initView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.a_mine, container, false)
        back = rootView.findViewById(R.id.iv_back)
        title = rootView.findViewById(R.id.tv_mine_nick)
        level = rootView.findViewById(R.id.tv_mine_vip)
        common = rootView.findViewById(R.id.tv_mine_common)
        phone = rootView.findViewById(R.id.tv_mine_phone)
        customer = rootView.findViewById(R.id.function)
        logout = rootView.findViewById(R.id.logout)

        title.setOnClickListener { checkLogin() }
        logout.setOnClickListener { logOut() }
        return rootView
    }

    override fun initData() {
        loadFunction()
        loadDeviceInfo()
        initHandler()
        loadUserInfo()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity!!.window.statusBarColor = ContextCompat.getColor(activity!!, R.color.color_white)
        }
    }

    override fun click(v: View?) {
    }

    private fun initHandler() {
        Constant.mSecondHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    0x1000 -> {
                        loadUserInfo()
                    }
                }
            }
        }
    }

    private fun loadUserInfo() {
        if (Constant.USER_NAME != "") {
            title.text = Constant.USER_NAME
            logout.visibility = View.VISIBLE
            PayManager.getInstance().getPayList {
                if (it.isNotEmpty()) {
                    val builder = StringBuilder()
                    for (order in it) {
                        if (order.server_code == Constant.PHOTO_FIX_TIMES) {
                            if (order.times != null) {
                                val times = order.times
                                if (times!! < 20) {
                                    builder.append("可用${20 - order.times!!}次 ")
                                }
                            }
                        }

                        if (order.server_code == Constant.PHOTO_FIX) {
                            when (order.expire_type) {
                                "1" -> builder.append("季度会员 ")
                                "2" -> builder.append("超级会员 ")
                                "3" -> builder.append("月度会员 ")
                                "4" -> builder.append("年度会员 ")
                                "5" -> builder.append("次数会员 ")
                            }
                        }

                        if (order.server_code == Constant.COM || order.server_code == Constant.REC || order.server_code == Constant.REPL) {
                            builder.append("恢复会员 ")
                        }
                    }

                    val text = builder.toString()
                    if (text != "") {
                        level.text = text
                        level.visibility = View.VISIBLE
                        common.visibility = View.GONE
                    } else {
                        common.text = "普通用户"
                        level.visibility = View.GONE
                        common.visibility = View.VISIBLE
                    }
                } else {
                    common.text = "普通用户"
                    level.visibility = View.GONE
                    common.visibility = View.VISIBLE
                }
            }
        } else {
            val userInfo = MMKV.defaultMMKV()?.decodeParcelable("userInfo", UserInfo::class.java)
            if (userInfo != null) {
                Constant.USER_NAME = userInfo.nickname
                title.text = Constant.USER_NAME
            }
        }
    }

    private fun checkLogin() {
        if (Constant.USER_NAME == "") {
            val intent = Intent(activity!!, LoginActivity::class.java)
            startActivity(intent)
        } else {
            title.text = Constant.USER_NAME
        }
    }

    private fun loadFunction() {
        val list = arrayListOf<Resource>()
        list.add(Resource("website", R.drawable.mine_website, getString(R.string.mine_website)))
        list.add(Resource("service", R.drawable.mine_help, getString(R.string.mine_service)))
        list.add(Resource("privacy", R.drawable.mine_privacy, getString(R.string.mine_privacy)))
        list.add(Resource("feedback", R.drawable.mine_feedback, getString(R.string.mine_help)))
        list.add(Resource("clear", R.drawable.clear_cache, getString(R.string.setting_clear_cache)))
        list.add(Resource("about", R.drawable.about_us, getString(R.string.setting_about_us)))
        list.add(Resource("quit", R.drawable.about_us, getString(R.string.setting_clear_cache_des)))

        val mAdapter = DataAdapter.Builder<Resource>()
            .setData(list)
            .setLayoutId(R.layout.item_function)
            .addBindView { itemView, itemData, position ->
                Glide.with(activity!!).load(itemData.icon).into(itemView.function_icon)
                itemView.function_name.text = itemData.name

                itemView.setOnClickListener {
                    when (position) {
                        0 -> openWebsite()
                        1 -> openUserAgreement()
                        2 -> openPrivacyAgreement()
                        3 -> openFeedback()
                        4 -> clearCache()
                        5 -> aboutUs()
                        6 -> accountDelete()
                    }
                }
            }
            .create()

        customer.layoutManager = LinearLayoutManager(activity!!)
        customer.adapter = mAdapter
        mAdapter.notifyItemRangeChanged(0, list.size)
    }


    private fun loadDeviceInfo() {
        if (Build.BRAND == "HUAWEI" || Build.BRAND == "HONOR") {
            val name = Dict.getHUAWEIName(Build.MODEL)
            if (name.isNullOrEmpty()) {
                val b = "${Build.BRAND} ${Build.MODEL}"
                phone.text = b
            } else {
                phone.text = name
            }
        } else {
            val b = "${Build.BRAND} ${Build.MODEL}"
            phone.text = b
        }
    }

    private fun openWebsite() {
        if (Constant.WEBSITE == "") return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constant.WEBSITE))
        startActivity(intent)
    }


    private fun openFeedback() {
        val intent = Intent(activity!!, CustomerServiceActivity::class.java)
        startActivity(intent)
    }

    private fun openUserAgreement() {
        val intent = Intent(activity!!, AgreementActivity::class.java)
        intent.putExtra("index", 0)
        startActivity(intent)
    }

    private fun openPrivacyAgreement() {
        val intent = Intent(activity!!, AgreementActivity::class.java)
        intent.putExtra("index", 1)
        startActivity(intent)
    }


    private fun clearCache() {
        launch(Dispatchers.IO) {
            DBManager.deleteFiles(activity!!)
            FileUtil.clearAllCache(activity!!)
        }

        launch(Dispatchers.Main) {
            ToastUtil.showShort(activity!!, "清除成功")
        }

    }

    private fun aboutUs() {
        val packName = AppUtil.getPackageVersionName(activity!!, activity!!.packageName)
        val appName = getString(R.string.app_name)
        ToastUtil.show(activity!!, "$appName $packName")
    }

    private fun accountDelete() {
        if (Constant.USER_NAME != "") {
            AccountDeleteDialog(activity!!, object : Callback {
                override fun onSuccess() {
                    delete()
                }

                override fun onCancel() {
                }
            }).show()
        } else {
            checkLogin()
        }
    }

    private fun delete() {
        launch {
            AccountLoader.delete()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    logOut()
                }, {
                })
        }
    }

    private fun logOut() {
        Constant.USER_NAME = ""
        Constant.CLIENT_TOKEN = ""
        title.text = "立即登录"
        level.text = "登录享受更多特权"
        common.visibility = View.GONE
        logout.visibility = View.GONE

        val userInfo = mmkv?.decodeParcelable("userInfo", UserInfo::class.java)
        if (userInfo != null) {
            Constant.CLIENT_TOKEN = ""
            Constant.USER_NAME = ""
            Constant.USER_ID = ""

            mmkv?.remove("userInfo")
        }
    }


}