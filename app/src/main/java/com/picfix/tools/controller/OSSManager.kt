package com.picfix.tools.controller

import android.content.Context
import android.net.Uri
import com.alibaba.sdk.android.oss.ClientConfiguration
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import com.picfix.tools.bean.OssParam
import com.picfix.tools.callback.UploadCallback
import com.picfix.tools.config.Constant
import com.picfix.tools.utils.JLog

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/3/26 14:04
 */
class OSSManager private constructor() {

    companion object {

        @Volatile
        private var ossManager: OSSManager? = null

        fun get(): OSSManager {
            if (ossManager == null) {
                synchronized(OSSManager::class) {
                    ossManager = OSSManager()
                }
            }

            return ossManager!!
        }
    }


    private val clientConfiguration = config()

    private fun config(): ClientConfiguration {
        val conf = ClientConfiguration()
        conf.connectionTimeout = 15 * 1000 // 连接超时，默认15秒
        conf.socketTimeout = 15 * 1000 // socket超时，默认15秒
        conf.maxConcurrentRequest = 5 // 最大并发请求数，默认5个
        conf.maxErrorRetry = 2 // 失败后最大重试次数，默认2次
        return conf
    }


    /**
     * 上传单个文件到投诉专区
     */
    fun uploadFileToComplaint(context: Context, stsModel: OssParam, filePath: Uri, callback: UploadCallback) {
        val credentialProvider = OSSStsTokenCredentialProvider(
            stsModel.AccessKeyId,
            stsModel.AccessKeySecret,
            stsModel.SecurityToken
        )

        val objectKey = "complaint/${Constant.USER_ID}_" + System.currentTimeMillis().toString() + ".jpg"
        val ossClient = OSSClient(context.applicationContext, Constant.END_POINT, credentialProvider, clientConfiguration)
        val put = PutObjectRequest(Constant.BUCKET_NAME, objectKey, filePath)

        beginUpload(objectKey, put, ossClient, callback)
    }

    /**
     * 上传单个文件到修复专区
     */
    fun uploadFileToFix(context: Context, stsModel: OssParam, filePath: Uri, callback: UploadCallback) {
        val credentialProvider = OSSStsTokenCredentialProvider(
            stsModel.AccessKeyId,
            stsModel.AccessKeySecret,
            stsModel.SecurityToken
        )

        val objectKey = "photoFix/${Constant.USER_ID}_" + System.currentTimeMillis().toString() + ".jpg"
        val ossClient = OSSClient(context.applicationContext, Constant.END_POINT, credentialProvider, clientConfiguration)
        val put = PutObjectRequest(Constant.BUCKET_NAME, objectKey, filePath)

        beginUpload(objectKey, put, ossClient, callback)
    }

    private fun beginUpload(objectKey: String, put: PutObjectRequest, ossClient: OSSClient, callback: UploadCallback) {
        ossClient.asyncPutObject(put, object : OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
            override fun onSuccess(request: PutObjectRequest?, result: PutObjectResult?) {
                JLog.i("upload success")
                callback.onSuccess("http://${Constant.BUCKET_NAME}.${Constant.END_POINT_WITHOUT_HTTP}/$objectKey")
            }

            override fun onFailure(request: PutObjectRequest?, clientException: ClientException?, serviceException: ServiceException?) {
                JLog.i("upload failed")
                if (clientException != null) {
                    callback.onFailed("网络连接失败")
                }

                if (serviceException != null) {
                    callback.onFailed("上传图片失败")
                }
            }
        })
    }
}