package com.picfix.tools.view.activity

import android.content.Intent
import android.widget.*
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.picfix.tools.R
import com.picfix.tools.bean.FileStatus
import com.picfix.tools.bean.FileWithType
import com.picfix.tools.callback.FileWithTypeCallback
import com.picfix.tools.controller.WxManager
import com.picfix.tools.utils.AppUtil
import com.picfix.tools.view.base.BaseActivity
import com.picfix.tools.view.views.ExportFileDialog
import kotlinx.coroutines.*


class VideoDetailActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var description: TextView
    private lateinit var video: PlayerView
    private lateinit var export: Button
    private lateinit var delete: Button
    private var file: FileWithType? = null
    private val mainList = arrayListOf<FileWithType>()
    private lateinit var videoFrameLayout: RelativeLayout
    private lateinit var player: SimpleExoPlayer


    override fun setLayout(): Int {
        return R.layout.video_detail
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        video = findViewById(R.id.vv_video)
        description = findViewById(R.id.video_description)
        export = findViewById(R.id.recovery)
        delete = findViewById(R.id.delete)
        videoFrameLayout = findViewById(R.id.fl_video)

        back.setOnClickListener { finish() }
        export.setOnClickListener { nextStep() }
        delete.setOnClickListener { deleteVideos() }

        initializePlayer()
    }

    override fun initData() {
        file = intent.getParcelableExtra("file")
        if (file != null) {
            mainList.add(file!!)

            val date = AppUtil.timeStamp2Date(file!!.date.toString(), null)
            val text = date + " / " + file!!.size / 1024 + "KB"
            description.text = text

            launch(Dispatchers.Default) {
                val mediaItem = MediaItem.fromUri(file!!.path)
                player.setMediaItem(mediaItem)
                player.prepare()
            }

        }
    }

    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(this).build()
        video.player = player
    }


    private fun nextStep() {
        if (mainList.isEmpty()) return
        //pay success , do something
        ExportFileDialog(this, mainList, "recovery_video").show()
    }

    private fun deleteVideos() {
        if (mainList.isEmpty()) return

        launch(Dispatchers.IO) {
            WxManager.getInstance(this@VideoDetailActivity).deleteFile(mainList, object : FileWithTypeCallback {
                override fun onSuccess(step: Enum<FileStatus>) {
                    launch(Dispatchers.Main) {
                    }
                }

                override fun onProgress(step: Enum<FileStatus>, file: FileWithType) {
                    launch(Dispatchers.Main) {
                        val intent = Intent()
                        intent.putExtra("file", file)
                        setResult(0x301, intent)
                        finish()
                    }
                }

                override fun onFailed(step: Enum<FileStatus>, message: String) {
                    launch(Dispatchers.Main) {
                    }
                }
            })
        }

    }



    override fun onDestroy() {
        player.pause()
        player.release()
        super.onDestroy()
    }
}