package com.picfix.tools.view.base

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.picfix.tools.callback.Callback
import com.picfix.tools.config.Constant
import com.picfix.tools.http.loader.TokenLoader
import com.picfix.tools.http.response.ResponseTransformer
import com.picfix.tools.http.schedulers.SchedulerProvider
import com.picfix.tools.utils.LivePermissions
import com.picfix.tools.utils.PermissionResult
import com.picfix.tools.utils.ToastUtil
import com.picfix.tools.view.views.AuthDialog
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.*

abstract class BaseFragment : Fragment(), CoroutineScope by MainScope(), View.OnClickListener {
    private var mContext: Context? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = initView(inflater, container, savedInstanceState)
        initData()
        return v
    }

    override fun onClick(v: View) {
        click(v)
    }

    fun onActivityResume() {}
    protected abstract fun initView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    protected abstract fun initData()
    protected abstract fun click(v: View?)


    protected fun checkPermissions(method: () -> Unit) {

        val mmkv = MMKV.defaultMMKV()
        val key = mmkv?.decodeLong("permission_deny")
        if (key != null && key != 0L) {
            if (System.currentTimeMillis() - key < 60 * 1000) {
                ToastUtil.showShort(activity, "请去系统设置开启应用读写权限")
                return
            }
        }

        val authShow = mmkv?.decodeBool("auth_dialog_show")
        if (authShow != null && !authShow) {
            AuthDialog(activity!!, object : Callback {
                override fun onSuccess() {
                    mmkv.encode("auth_dialog_show", true)
                    requestPermission(method)
                }

                override fun onCancel() {
                    mmkv.encode("auth_dialog_show", false)
                }
            }).show()
        } else {
            requestPermission(method)
        }

    }

    private fun requestPermission(method: () -> Unit) {
        val mmkv = MMKV.defaultMMKV()
        LivePermissions(this@BaseFragment).request(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
        ).observe(this@BaseFragment) {
            when (it) {
                is PermissionResult.Grant -> {
                    //权限允许
                    method()
                    mmkv?.encode("permission_deny", 0L)

                    TTAdSdk.getAdManager().requestPermissionIfNecessary(requireActivity())
                }

                is PermissionResult.Rationale -> {
                    //权限拒绝
                    ToastUtil.showShort(context, "请打开必要的权限申请保证功能的正常使用")
                    it.permissions.forEach { s ->
                        println("Rationale:${s}")//被拒绝的权限
                        mmkv?.encode("permission_deny", System.currentTimeMillis())
                    }
                }

                is PermissionResult.Deny -> {
                    ToastUtil.showShort(context, "请打开必要的权限申请保证功能的正常使用")
                    //权限拒绝，且勾选了不再询问
                    it.permissions.forEach { s ->
                        println("deny:${s}")//被拒绝的权限
                        mmkv?.encode("permission_deny", System.currentTimeMillis())
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}