package com.picfix.tools.view.activity

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.picfix.tools.R
import com.picfix.tools.adapter.MutableDataAdapter
import com.picfix.tools.bean.Resource
import com.picfix.tools.view.base.BaseActivity
import kotlinx.android.synthetic.main.item_question_content.view.*
import kotlinx.android.synthetic.main.item_question_title.view.question_title

class QuestionActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var recyclerView: RecyclerView
    private val mainList = arrayListOf<Resource>()
    private lateinit var adapter: MutableDataAdapter<Resource>

    override fun setLayout(): Int {
        return R.layout.a_question
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        recyclerView = findViewById(R.id.question_list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        back.setOnClickListener { finish() }
    }

    override fun initData() {
        mainList.add(Resource("", 0, "常见问题"))
        mainList.add(
            Resource(
                "照片恢复的服务流程是怎样的？", 1, "点击首页的【图片恢复】，软件会对文件夹开始扫描，在扫描的结果中能看到的图片都是可以恢复的。您可以勾选要恢复的图片，然后点击’恢复按钮‘，" +
                        "软件会将图片恢复到相册中。如果扫描的结果没有您想要的照片，则无法恢复。建议您先找到想要恢复的照片再开通会员恢复。"
            )
        )
        mainList.add(Resource("手机图片恢复到哪里去了？", 1, "为了便于查看和使用，恢复的图片会保存到相册里。"))
        mainList.add(Resource("图片恢复有哪些注意事项？", 1, "扫描显示的图片，肉眼所见的清晰度和恢复后的清晰度一样，请确认是否看清以及能否满足自身需求。"))
        mainList.add(Resource("微信语音怎么恢复？", 1, "点击首页的【语音恢复】，等待软件扫描微信数据碎片，待分析完成之后，便会跳转到恢复结果页面。然后可以看到恢复的语音文件。"))
        mainList.add(Resource("微信图片和视频怎么恢复不到微信里面？", 1, "本软件是对微信备份文件进行数据分析的，和微信本身也没有任何的交互。"))
        mainList.add(Resource("几个月前删除的图片、视频、语音或文档还能恢复吗？", 1, "本软件能恢复出来多少文件，和用户删除之后的具体操作有很大关系。新的数据会不断覆盖掉之前被删除的数据。时间越久或者数据写入频繁，恢复的几率越低。"))
        mainList.add(Resource("没有微信密码怎么找回聊天记录？", 1, "软件扫描数据，和是否登录微信账户或者是否打开微信没有关系。软件是通过扫描备份文件中的数据碎片来查找记录的。"))
        mainList.add(Resource("微信卸载有机会恢复什么？", 1, "微信卸载之后，文字记录恢复几率相对很低，有机会找到卸载之前的图片、语音、视频，文档等文件，具体可以体验首页各个功能。"))
        mainList.add(Resource("没有微信密码怎么找回聊天图片视频等信息吗？", 1, "软件扫描数据，和是否登录微信账户或者是否打开微信没有关系。软件是通过扫描备份文件中的数据碎片来查找记录的。"))
        mainList.add(Resource("手机丢失以后，能找到丢失手机上的图片视频等吗？", 1, "手机丢失之后，是无法找到丢失手机上的数据的。"))
        mainList.add(Resource("人工客服问题？", 1, "人工客服需要购买任一vip套餐后才能使用，我们的人工客服在线时间是10：00-22：00"))

        initRecyclerView()
    }

    private fun initRecyclerView() {
        adapter = MutableDataAdapter.Builder<Resource>()
            .setData(mainList)
            .setLayoutId(R.layout.item_question_title, R.layout.item_question_content)
            .setViewType { position -> mainList[position].icon }
            .addBindView { itemView, itemData ->
                when (itemData.icon) {
                    0 -> {
                        itemView.question_title.text = itemData.name
                    }

                    1 -> {
                        itemView.question_content_title.text = itemData.type
                        itemView.question_content.text = itemData.name
                        itemView.setOnClickListener {
                            when (itemView.question_content.visibility) {
                                View.VISIBLE -> itemView.question_content.visibility = View.GONE
                                View.GONE -> itemView.question_content.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }.create()

        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}