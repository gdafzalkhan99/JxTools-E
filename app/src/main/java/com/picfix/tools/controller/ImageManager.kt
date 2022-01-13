package com.picfix.tools.controller

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.ArrayMap
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.EnvironmentCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.picfix.tools.bean.*
import com.picfix.tools.callback.FileCallback
import com.picfix.tools.callback.HttpCallback
import com.picfix.tools.callback.UploadCallback
import com.picfix.tools.config.Constant
import com.picfix.tools.http.loader.*
import com.picfix.tools.http.request.AuthService
import com.picfix.tools.http.response.ResponseTransformer
import com.picfix.tools.http.schedulers.SchedulerProvider
import com.picfix.tools.utils.*
import com.tencent.mmkv.MMKV
import com.tencent.smtt.utils.Md5Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.thread


/**
 * @author Herr_Z
 * @description:
 * @date : 2021/6/24 14:59
 */
object ImageManager : CoroutineScope by MainScope() {
    private const val APP_ID = "24596362"
    private const val APP_KEY = "ox0Uz65dzs60GHqIloRYcxyL"
    private const val SECRET_KEY = "5OUUiU62kaR6jujt1d5me9kVTV7DuC9v"

    //    private const val APP_ID = "24433333"
//    private const val APP_KEY = "BZznWwsm4PxCOoGY5V8SDuUa"
//    private const val SECRET_KEY = "mpnDji1vfDqsj3ffDGp5jnNXZhQ3rETe"

    // 用于保存图片的文件路径，Android 10以下使用图片路径访问图片
    private var mCameraImagePath: String? = null


    fun checkPermission(activity: Activity, result: (Boolean) -> Unit) {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        )

        if (hasCameraPermission == PackageManager.PERMISSION_GRANTED) {
            //有调起相机拍照。
            result(true)
        } else {
            result(false)

            //没有权限，申请权限。
            ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.CAMERA),
                Constant.PERMISSION_CAMERA_REQUEST_CODE
            )
        }
    }

    fun openCamera(activity: Activity, result: (Uri) -> Unit) {
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // 判断是否有相机
        if (captureIntent.resolveActivity(activity.packageManager) != null) {
            var photoFile: File? = null
            var photoUri: Uri? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // 适配android 10
                photoUri = createImageUri(activity)
            } else {
                try {
                    photoFile = createImageFile(activity)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                if (photoFile != null) {
                    mCameraImagePath = photoFile.absolutePath
                    photoUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        //适配Android 7.0文件权限，通过FileProvider创建一个content类型的Uri
                        FileProvider.getUriForFile(activity, activity.packageName.toString() + ".fileprovider", photoFile)
                    } else {
                        Uri.fromFile(photoFile)
                    }
                }
            }

            if (photoUri != null) {
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                activity.startActivityForResult(captureIntent, Constant.CAMERA_REQUEST_CODE)

                result(photoUri)
            }
        }
    }

    /**
     * 创建图片地址uri,用于保存拍照后的照片 Android 10以后使用这种方法
     */
    private fun createImageUri(activity: Activity): Uri? {
        val status: String = Environment.getExternalStorageState()
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        return if (status == Environment.MEDIA_MOUNTED) {
            activity.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
        } else {
            activity.contentResolver.insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, ContentValues())
        }
    }

    /**
     * 创建保存图片的文件
     */
    @Throws(IOException::class)
    private fun createImageFile(activity: Activity): File? {
        val imageName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdir()
        }

        val tempFile = File(storageDir, imageName)
        return if (Environment.MEDIA_MOUNTED != EnvironmentCompat.getStorageState(tempFile)) {
            null
        } else tempFile
    }

    /**
     * 输入一张图片，可以在尽量保持图像质量的条件下，将图像在长宽方向各放大两倍
     * 本地图片路径或者图片二进制数据，大小不超过4M，长宽乘积不超过1600p x 1600px
     */

    fun enlargeImage(activity: Activity, filePath: String, callback: HttpCallback): Bitmap? {
        // 请求url
        val url = "https://aip.baidubce.com/rest/2.0/image-process/v1/image_quality_enhance"
        try {
            val imgData = FileUtils.readFileByBytes(filePath)
            val imgStr = Base64Util.encode(imgData)
            val imgParam = URLEncoder.encode(imgStr, "UTF-8")

            val param = "image=$imgParam"

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            val accessToken = AuthService.getAuth()

            val result = HttpUtil.post(url, accessToken, param)

            if (result != null) {
                saveImage(activity, result, Bitmap.CompressFormat.JPEG, callback)
            } else {
                callback.onFailed("请求失败")
            }

            return null

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * 图像去雾
     */
    fun dehaze(activity: Activity, filePath: String, callback: HttpCallback): Bitmap? {
        // 请求url
        val url = "https://aip.baidubce.com/rest/2.0/image-process/v1/dehaze"
        try {
            val imgData = FileUtils.readFileByBytes(filePath)
            val imgStr = Base64Util.encode(imgData)
            val imgParam = URLEncoder.encode(imgStr, "UTF-8")

            val param = "image=$imgParam"

            val accessToken = AuthService.getAuth()

            val result = HttpUtil.post(url, accessToken, param)


            if (result != null) {
                saveImage(activity, result, Bitmap.CompressFormat.JPEG, callback)
            } else {
                callback.onFailed("请求失败")
            }

            return null

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }


    /**
     * 图片对比度增强
     */
    fun contrastEnhance(activity: Activity, filePath: String, callback: HttpCallback): Bitmap? {
        // 请求url
        val url = "https://aip.baidubce.com/rest/2.0/image-process/v1/contrast_enhance"
        try {
            val imgData = FileUtils.readFileByBytes(filePath)
            val imgStr = Base64Util.encode(imgData)
            val imgParam = URLEncoder.encode(imgStr, "UTF-8")

            val param = "image=$imgParam"

            val accessToken = AuthService.getAuth()

            val result = HttpUtil.post(url, accessToken, param)

            if (result != null) {
                saveImage(activity, result, Bitmap.CompressFormat.JPEG, callback)
            } else {
                callback.onFailed("请求失败")
            }

            return null

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }


    /**
     * 黑白图像上色
     */

    fun colourize(activity: Activity, filePath: String, callback: HttpCallback): Bitmap? {
        // 请求url
        val url = "https://aip.baidubce.com/rest/2.0/image-process/v1/colourize"
        try {
            val imgData = FileUtils.readFileByBytes(filePath)
            val imgStr = Base64Util.encode(imgData)
            val imgParam = URLEncoder.encode(imgStr, "UTF-8")

            val param = "image=$imgParam"

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            val accessToken = AuthService.getAuth()

            val result = HttpUtil.post(url, accessToken, param)

            if (result != null) {
                saveImage(activity, result, Bitmap.CompressFormat.JPEG, callback)
            } else {
                callback.onFailed("请求失败")
            }

            return null

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }


    /**
     * 人像动漫画
     */
    @SuppressLint("CheckResult")
    fun cartoon(activity: Activity, filePath: Uri, callback: HttpCallback) {
        OssLoader.getOssToken(Constant.CLIENT_TOKEN)
            .compose(ResponseTransformer.handleResult())
            .compose(SchedulerProvider.getInstance().applySchedulers())
            .subscribe({ ossParam ->
                OSSManager.get().uploadFileToFix(activity, ossParam, filePath, object : UploadCallback {
                    override fun onSuccess(path: String) {

                        CartoonLoader.cartoonTrans(path)
                            .compose(ResponseTransformer.handleResult())
                            .compose(SchedulerProvider.getInstance().applySchedulers())
                            .subscribe({
                                saveImage(activity, it, callback)
                            }, {})

                    }

                    override fun onFailed(msg: String) {
                    }
                })

            }, {
                ToastUtil.show(activity, "请求失败，请检查网络")
            })
    }

    /**
     * 图片风格转换
     */
    fun styleTrans(activity: Activity, filePath: String, type: String, callback: HttpCallback) {
        // 请求url
        val url = "https://aip.baidubce.com/rest/2.0/image-process/v1/style_trans"
        try {
            // 本地文件路径
            val imgData = FileUtils.readFileByBytes(filePath)
            val imgStr = Base64Util.encode(imgData)
            val imgParam = URLEncoder.encode(imgStr, "UTF-8")

            val param = "image=$imgParam&option=$type"

            val accessToken = AuthService.getAuth()

            val result = HttpUtil.post(url, accessToken, param)

            if (result != null) {
                saveImage(activity, result, Bitmap.CompressFormat.JPEG, callback)
            } else {
                callback.onFailed("请求失败")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 图片色彩增强
     */
    fun colorEnhance(activity: Activity, filePath: String, callback: HttpCallback) {
        // 请求url
        val url = "https://aip.baidubce.com/rest/2.0/image-process/v1/selfie_anime"
        try {
            // 本地文件路径
            val imgData = FileUtils.readFileByBytes(filePath)
            val imgStr = Base64Util.encode(imgData)
            val imgParam = URLEncoder.encode(imgStr, "UTF-8")

            val param = "image=$imgParam"

            val accessToken = AuthService.getAuth()

            val result = HttpUtil.post(url, accessToken, param)

            if (result != null) {
                saveImage(activity, result, Bitmap.CompressFormat.JPEG, callback)
            } else {
                callback.onFailed("请求失败")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 拉伸图像恢复
     */
    fun stretch(activity: Activity, filePath: String, callback: HttpCallback) {
        // 请求url
        val url = "https://aip.baidubce.com/rest/2.0/image-process/v1/stretch_restore"
        try {
            // 本地文件路径
            val imgData = FileUtils.readFileByBytes(filePath)
            val imgStr = Base64Util.encode(imgData)
            val imgParam = URLEncoder.encode(imgStr, "UTF-8")

            val param = "image=$imgParam"

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            val accessToken = AuthService.getAuth()

            val result = HttpUtil.post(url, accessToken, param)

            if (result != null) {
                saveImage(activity, result, Bitmap.CompressFormat.JPEG, callback)
            } else {
                callback.onFailed("请求失败")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 图片清晰度增强
     */
    fun definition(activity: Activity, filePath: String, callback: HttpCallback) {
        // 请求url
        val url = "https://aip.baidubce.com/rest/2.0/image-process/v1/image_definition_enhance"
        try {
            // 本地文件路径
            val imgData = FileUtils.readFileByBytes(filePath)
            val imgStr = Base64Util.encode(imgData)
            val imgParam = URLEncoder.encode(imgStr, "UTF-8")

            val param = "image=$imgParam"

            val accessToken = AuthService.getAuth()

            val result = HttpUtil.post(url, accessToken, param)

            if (result != null) {
//                blockPrint(result)
                saveImage(activity, result, Bitmap.CompressFormat.JPEG, callback)
            } else {
                callback.onFailed("请求失败")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 图片恢复
     */
    fun inPainting(activity: Activity, filePath: String, rectangleMap: HashMap<String, Any>, callback: HttpCallback) {
        // 请求url
        val url = "https://aip.baidubce.com/rest/2.0/image-process/v1/inpainting"
        try {
            // 本地文件路径
            val imgData = FileUtils.readFileByBytes(filePath)
            val imgStr = Base64Util.encode(imgData)

            val rectangle = arrayListOf<Any>()
            rectangle.add(rectangleMap)

            val map = HashMap<String, Any>()
            map["image"] = imgStr
            map["rectangle"] = rectangle

            val param = GsonUtils.toJson(map)

            JLog.i("path = $filePath")
            JLog.i("rectangle = ${map["rectangle"]}")

            //获取access_token
            val accessToken = AuthService.getAuth()

            val result = HttpUtil.post(url, accessToken, "application/json", param)

            if (result != null) {
//                blockPrint(result)
                saveImage(activity, result, Bitmap.CompressFormat.JPEG, callback)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 人脸重叠
     */
    fun faceMerge(activity: Activity, filePath1: String, filePath2: String, callback: HttpCallback) {
        // 请求url
        val url = "https://aip.baidubce.com/rest/2.0/face/v1/merge"
        try {
            val paramMap = HashMap<String, Any>()
            val templateMap = HashMap<String, Any>()
            templateMap["image"] = Base64Util.encode(FileUtils.readFileByBytes(filePath1))
            templateMap["image_type"] = "BASE64"
            templateMap["quality_control"] = "NONE"
            paramMap["image_template"] = templateMap

            val targetMap = HashMap<String, Any>()
            targetMap["image"] = Base64Util.encode(FileUtils.readFileByBytes(filePath2))
            targetMap["image_type"] = "BASE64"
            targetMap["quality_control"] = "NONE"
            paramMap["image_target"] = targetMap

            val param = GsonUtils.toJson(paramMap)

            //获取access_token
            val accessToken = AuthService.getAuth()

            val result = HttpUtil.post(url, accessToken, "application/json", param)

            if (result != null) {
                saveImageFromBaiduFace(activity, result, Bitmap.CompressFormat.JPEG, callback)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * 人像分割
     */
    fun bodySeg(activity: Activity, filePath: String, callback: HttpCallback) {
        // 请求url
        val url = "https://aip.baidubce.com/rest/2.0/image-classify/v1/body_seg"
        try {
            // 本地文件路径
            val imgData = FileUtils.readFileByBytes(filePath)
            val imgStr = Base64Util.encode(imgData)
            val imgParam = URLEncoder.encode(imgStr, "UTF-8")

            val param = "image=$imgParam&type=foreground"

            val accessToken = AuthService.getAuth()

            val result = HttpUtil.post(url, accessToken, param)

            if (result != null) {
                saveImage(activity, result, Bitmap.CompressFormat.PNG, callback)
            } else {
                callback.onFailed("请求失败")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("CheckResult")
    fun ageTrans(activity: Activity, filePath: Uri, age: Int, callback: HttpCallback) {
        OssLoader.getOssToken(Constant.CLIENT_TOKEN)
            .compose(ResponseTransformer.handleResult())
            .compose(SchedulerProvider.getInstance().applySchedulers())
            .subscribe({ ossParam ->
                OSSManager.get().uploadFileToFix(activity, ossParam, filePath, object : UploadCallback {
                    override fun onSuccess(path: String) {

                        AgeTransLoader.ageTrans(path, age)
                            .compose(ResponseTransformer.handleResult())
                            .compose(SchedulerProvider.getInstance().applySchedulers())
                            .subscribe({
                                saveImage(activity, it, callback)
                            }, {})

                    }

                    override fun onFailed(msg: String) {
                    }
                })

            }, {
                ToastUtil.show(activity, "请求失败，请检查网络")
            })
    }

    @SuppressLint("CheckResult")
    fun genderTrans(activity: Activity, filePath: Uri, gender: Int, callback: HttpCallback) {
        OssLoader.getOssToken(Constant.CLIENT_TOKEN)
            .compose(ResponseTransformer.handleResult())
            .compose(SchedulerProvider.getInstance().applySchedulers())
            .subscribe({ ossParam ->
                OSSManager.get().uploadFileToFix(activity, ossParam, filePath, object : UploadCallback {
                    override fun onSuccess(path: String) {

                        GenderTransLoader.genderTrans(path, gender)
                            .compose(ResponseTransformer.handleResult())
                            .compose(SchedulerProvider.getInstance().applySchedulers())
                            .subscribe({
                                saveImage(activity, it, callback)
                            }, {})

                    }

                    override fun onFailed(msg: String) {
                    }
                })

            }, {
                ToastUtil.show(activity, "请求失败，请检查网络")
            })
    }

    @SuppressLint("CheckResult")
    fun morph(activity: Activity, uploadList: ArrayList<Uri>, callback: HttpCallback) {
        OssLoader.getOssToken(Constant.CLIENT_TOKEN)
            .compose(ResponseTransformer.handleResult())
            .compose(SchedulerProvider.getInstance().applySchedulers())
            .subscribe({ ossParam ->
                val list = arrayListOf<String>()
                for ((position, item) in uploadList.withIndex()) {
                    OSSManager.get().uploadFileToFix(activity, ossParam, item, object : UploadCallback {
                        override fun onSuccess(path: String) {
                            list.add(path)
                            if (list.size > 0 && position == uploadList.size - 1) {
                                val gson = Gson()
                                val json = gson.toJson(list)
                                JLog.i("json = $json")
                                MorphLoader.morph(json)
                                    .compose(ResponseTransformer.handleResult())
                                    .compose(SchedulerProvider.getInstance().applySchedulers())
                                    .subscribe({
                                        if (!it.JobId.isNullOrEmpty()) {
                                            val time = System.currentTimeMillis()
                                            getMorphUrl(activity, it.JobId!!, time, callback)
                                        }
                                    }, {})
                            }
                        }

                        override fun onFailed(msg: String) {
                            callback.onFailed("图片上传失败")
                        }
                    })
                }
            }, {
                ToastUtil.show(activity, "请求失败，请检查网络")
            })
    }

    @SuppressLint("CheckResult")
    private fun getMorphUrl(context: Context, jobId: String, time: Long, callback: HttpCallback) {
        Thread.sleep(5000)
        GetMorphUrlLoader.getMorphUrl(jobId)
            .compose(ResponseTransformer.handleResult())
            .compose(SchedulerProvider.getInstance().applySchedulers())
            .subscribe({
                if (!it.Url.isNullOrEmpty()) {
                    JLog.i("url = ${it.Url}")
                    launch(Dispatchers.IO) {
                        FileUtil.downloadVideo(context, it.Url!!, callback)
                    }
                } else {
                    if (System.currentTimeMillis() - time < 3 * 60 * 1000L) {
                        getMorphUrl(context, jobId, time, callback)
                    } else {
                        callback.onFailed("处理图片超时")
                    }
                }
            }, {})
    }

    /**
     * 分块日志输出
     */
    private fun blockPrint(result: String) {
        val r = result.length / 4096
        if (r < 1) {
            JLog.i("result = $result")
        } else {
            for (index in 0..(r + 1)) {
                if (index < r) {
                    JLog.i("result $index = ${result.substring(index * 4096, (index + 1) * 4096)}")
                } else {
                    JLog.i("result $index = ${result.substring(index * 4096)}")
                }
            }
        }
    }

    private fun saveImage(activity: Activity, res: String, type: Bitmap.CompressFormat, callback: HttpCallback) {
        val gson = Gson()
        if (res.contains("error_msg")) {
            JLog.i("发生错误")
            val result = gson.fromJson(res, ImageErrorResult::class.java)

            when (result.error_code) {
                1 -> callback.onFailed("服务器内部错误，请重试")
                4 -> callback.onFailed("调用次数超限额")
                13 -> callback.onFailed("鉴权失效")
                17, 18, 19 -> callback.onFailed("请求超限额")
                100, 110, 111 -> callback.onFailed("无效的请求码")
                216100, 282004 -> callback.onFailed("非法参数")
                216101 -> callback.onFailed("缺少必要参数")
                216102 -> callback.onFailed("请求服务不支持")
                216103 -> callback.onFailed("请求参数过长")
                216200 -> callback.onFailed("图片为空")
                216201 -> callback.onFailed("图片格式不符合要求")
                216202 -> callback.onFailed("图片大小不符合要求")
                216203 -> callback.onFailed("图片大小与面部图片大小不一致")
                216204 -> callback.onFailed("图片未检测到人脸")
                216630 -> callback.onFailed("识别错误，请重试")
                else -> callback.onFailed(result.error_msg)
            }
            return
        }

        if (res.contains("foreground")) {
            val result = gson.fromJson(res, BodyResult::class.java)
            if (result != null) {

                val data = base64ToByteArray(result.foreground)
                val outPath = FileUtil.getSDPath(activity) + Constant.PICTURE_PATH + System.currentTimeMillis() + ".png"
                FileUtil.byteToFile(data, outPath)

                JLog.i("result logId = ${result.log_id}")
                JLog.i("outPath = $outPath")

                val file = File(outPath)
                if (file.exists()) {
                    if (type == Bitmap.CompressFormat.PNG) {
                        FileUtil.savePNGImage(activity, file)
                    } else {
                        FileUtil.saveImage(activity, file)
                    }
                    callback.onSuccess()
                    JLog.i("save successfully")
                }

            } else {
                callback.onFailed("未知错误")
            }
            return
        }


        if (res.contains("image")) {
            val result = gson.fromJson(res, ImageResult::class.java)
            if (result != null) {
                val data = base64ToByteArray(result.image)
                val outPath = FileUtil.getSDPath(activity) + Constant.PICTURE_PATH + System.currentTimeMillis() + ".png"
                FileUtil.byteToFile(data, outPath)

                JLog.i("result logId = ${result.log_id}")
                JLog.i("outPath = $outPath")

                val file = File(outPath)
                if (file.exists()) {
                    if (type == Bitmap.CompressFormat.PNG) {
                        FileUtil.savePNGImage(activity, file)
                    } else {
                        FileUtil.saveImage(activity, file)
                    }
                    report()
                    callback.onSuccess()
                    JLog.i("save successfully")
                }

            } else {
                callback.onFailed("未知错误")
            }
        }
    }

    private fun saveImageFromBaiduFace(activity: Activity, res: String, type: Bitmap.CompressFormat, callback: HttpCallback) {
        val gson = Gson()
        if (res.contains("error_msg")) {
            val result = gson.fromJson(res, BaiduFaceResult::class.java)
            when (result.error_code) {
                0 -> {
                    val dataBase64 = result.result.merge_image
                    if (dataBase64 != "") {
                        val data = base64ToByteArray(dataBase64)
                        val outPath = FileUtil.getSDPath(activity) + Constant.PICTURE_PATH + System.currentTimeMillis() + ".png"
                        FileUtil.byteToFile(data, outPath)

                        JLog.i("result logId = ${result.log_id}")
                        JLog.i("outPath = $outPath")

                        val file = File(outPath)
                        if (file.exists()) {
                            if (type == Bitmap.CompressFormat.PNG) {
                                FileUtil.savePNGImage(activity, file)
                            } else {
                                FileUtil.saveImage(activity, file)
                            }
                            report()
                            callback.onSuccess()
                            JLog.i("save successfully")
                        }
                    }
                }
                1 -> callback.onFailed("服务器内部错误，请重试")
                4 -> callback.onFailed("调用次数超限额")
                13 -> callback.onFailed("鉴权失效")
                17, 18, 19 -> callback.onFailed("请求超限额")
                100, 110, 111 -> callback.onFailed("无效的请求码")
                216100, 282004 -> callback.onFailed("非法参数")
                216101 -> callback.onFailed("缺少必要参数")
                216102 -> callback.onFailed("请求服务不支持")
                216103 -> callback.onFailed("请求参数过长")
                216200 -> callback.onFailed("图片为空")
                216201 -> callback.onFailed("图片格式不符合要求")
                216202 -> callback.onFailed("图片大小不符合要求")
                216203 -> callback.onFailed("图片大小与面部图片大小不一致")
                216204 -> callback.onFailed("图片未检测到人脸")
                216630 -> callback.onFailed("识别错误，请重试")
                else -> callback.onFailed(result.error_msg)
            }
        }
    }

    /**
     * 保存图片到相册
     */
    private fun saveImage(activity: Activity, result: TencentCloudResult, callback: HttpCallback) {
        if (!result.ResultUrl.isNullOrEmpty()) {
            //用Glide下载图片
            Glide.with(activity).asBitmap().load(result.ResultUrl).into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    //保存到相册
                    FileUtil.savePNGImage(activity, resource, callback)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
            return
        }

        if (!result.hint.isNullOrEmpty()) {
            callback.onFailed(result.hint!!)
            return
        }

        if (!result.msg.isNullOrEmpty()) {
            callback.onFailed(result.msg!!)
            return
        }

    }

    /**
     * 保存GIF图片到Picture下
     */
    private fun saveGifImage(activity: Activity, result: TencentCloudResult, callback: HttpCallback) {
        if (!result.ResultUrl.isNullOrEmpty()) {
            //用Glide下载图片
            Glide.with(activity).asFile().load(result.ResultUrl).into(object : CustomTarget<File>() {
                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                    //保存到Picture下
                    val target = FileUtil.getSDPath(activity) + Constant.PICTURE_PATH + System.currentTimeMillis() + ".gif"
                    FileUtil.copyFile(resource.absolutePath, target, object : FileCallback {
                        override fun onSuccess(step: Enum<FileStatus>) {
                        }

                        override fun onProgress(step: Enum<FileStatus>, index: Int) {
                        }

                        override fun onFailed(step: Enum<FileStatus>, message: String) {
                        }
                    })
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
            return
        }

        if (!result.hint.isNullOrEmpty()) {
            callback.onFailed(result.hint!!)
        }

    }

    private fun base64ToByteArray(imageBase64: String): ByteArray? {
        var bytes: ByteArray? = null
        try {

            bytes = if (imageBase64.indexOf("data:image/jpeg;base64,") != -1) {
                Base64Util.decode(imageBase64.replace("data:image/jpeg;base64,".toRegex(), ""))
            } else {
                Base64Util.decode(imageBase64.replace("data:image/jpg;base64,".toRegex(), ""))
            }

            for (i in bytes.indices) {
                if (bytes[i] < 0) { // 调整异常数据
                    (bytes[i].plus(256)).toByte()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bytes
    }

    @SuppressLint("CheckResult")
    fun report() {
        val mmkv = MMKV.defaultMMKV()
        when (AppUtil.getChannelId()) {
            Constant.CHANNEL_VIVO, Constant.CHANNEL_HUAWEI -> {
                val times = mmkv?.decodeString("activity_times")
                if (times == "true") {
                    return
                }
            }
        }

        thread {
            ReportLoader.report()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({

                }, {

                })
        }
    }

    /**
     * OPPO推广渠道上报
     */
    fun reportToOPPO(activity: Activity, timeStamp: Long, type: Int, amount: Int) {
        val url = "https://api.ads.heytapmobi.com/api/uploadActiveData"
        try {
            val imei = DeviceUtil.getDefaultIMEI(activity)
            val base64Key = "XGAXicVG5GMBsx5bueOe4w=="
            val enCodeImei = AES.encrypt(Base64Util.decode(base64Key), imei)
            var channel = 1
            val model = Build.MODEL
            if (model.lowercase().contains("oneplus")) {
                channel = 2
            } else {
                channel = 0
            }

            val paramMap = HashMap<String, Any>()
            paramMap["imei"] = enCodeImei
            paramMap["timestamp"] = timeStamp
            paramMap["pkg"] = activity.packageName
            paramMap["dataType"] = type
            paramMap["channel"] = channel
            paramMap["type"] = 1
            paramMap["payAmount"] = amount

            val postBody = Gson().toJson(paramMap)
            val salt = "e0u6fnlag06lc3pl"
            val signature = AppUtil.MD5Encode("$postBody$timeStamp$salt")

            val map = ArrayMap<String, String>()
            map["signature"] = signature
            map["timestamp"] = timeStamp.toString()

            JLog.i("body = $postBody")

            val result = HttpUtil.post(url, "application/json", postBody, map)

            if (result != null) {
                JLog.i("result  = $result")
            } else {
                JLog.i("result is null")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}