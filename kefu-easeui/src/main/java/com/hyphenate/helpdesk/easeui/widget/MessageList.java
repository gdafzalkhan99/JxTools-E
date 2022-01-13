package com.hyphenate.helpdesk.easeui.widget;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hyphenate.chat.ChatClient;
import com.hyphenate.chat.Conversation;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.Message;
import com.hyphenate.helpdesk.R;
import com.hyphenate.helpdesk.easeui.adapter.MessageAdapter;
import com.hyphenate.helpdesk.easeui.provider.CustomChatRowProvider;
import com.hyphenate.helpdesk.util.Log;

import java.util.List;

public class MessageList extends RelativeLayout {
    protected static final String TAG = MessageList.class.getSimpleName();
    protected ListView listView;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected Context context;
    protected Conversation conversation;
    protected String toChatUsername;
    protected MessageAdapter messageAdapter;
    protected boolean showUserNick;
    protected boolean showAvatar;
    protected Drawable myBubbleBg;
    protected Drawable otherBuddleBg;
    public static long defaultDelay = 200;
    public boolean robotShow = true;
    public boolean isPay = false;

    public MessageList(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public MessageList(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseStyle(context, attrs);
        init(context);
    }

    public MessageList(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.hd_chat_message_list, this);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.chat_swipe_layout);
        listView = (ListView) findViewById(R.id.list);
    }

    /**
     * init widget
     *
     * @param toChatUsername
     * @param customChatRowProvider
     */
    public void init(String toChatUsername, CustomChatRowProvider customChatRowProvider) {
        this.toChatUsername = toChatUsername;

        conversation = ChatClient.getInstance().chatManager().getConversation(toChatUsername);
        messageAdapter = new MessageAdapter(context, toChatUsername, listView);
        messageAdapter.setShowAvatar(showAvatar);
        messageAdapter.setShowUserNick(showUserNick);
        messageAdapter.setMyBubbleBg(myBubbleBg);
        messageAdapter.setOtherBuddleBg(otherBuddleBg);
        messageAdapter.setCustomChatRowProvider(customChatRowProvider);
        messageAdapter.robotShow = robotShow;


        // 设置adapter显示消息
        listView.setAdapter(messageAdapter);

        refreshSelectLast();
    }

    protected void parseStyle(Context context, AttributeSet attrs) {
        @SuppressLint("CustomViewStyleable") TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EaseChatMessageList);
        showAvatar = ta.getBoolean(R.styleable.EaseChatMessageList_msgListShowUserAvatar, true);
        myBubbleBg = ta.getDrawable(R.styleable.EaseChatMessageList_msgListMyBubbleBackground);
        otherBuddleBg = ta.getDrawable(R.styleable.EaseChatMessageList_msgListMyBubbleBackground);
        showUserNick = ta.getBoolean(R.styleable.EaseChatMessageList_msgListShowUserNick, false);
        ta.recycle();
    }


    /**
     * 刷新列表
     */
    public void refresh() {
        if (messageAdapter != null) {
            messageAdapter.refresh();
        }
    }

    public void clearMessages() {
        if (messageAdapter != null) {
            messageAdapter.isAdd = false;
            messageAdapter.isClear = true;
        }
    }

    public void addNewMessage(String content, Message message) {
        if (messageAdapter != null) {
            if (!messageAdapter.isNewSession) {
                ChatClient.getInstance().chatManager().sendMessage(message);
            } else {
                messageAdapter.addAndRefresh(message);
                String cont = getContent(content);
                if (cont != null) {
                    Message msg = Message.createReceiveMessage(Message.Type.TXT);
                    EMTextMessageBody body = new EMTextMessageBody(cont);
                    msg.setBody(body);
                    messageAdapter.addAndRefresh(msg);
                } else {
                    if (content.equals("9")) {
                        if (isPay) {
                            ChatClient.getInstance().chatManager().sendMessage(message);
                        } else {
                            Toast.makeText(this.getContext(), "仅对付费用户开放", Toast.LENGTH_SHORT).show();
                        }
                    }

                    if (!robotShow) {
                        ChatClient.getInstance().chatManager().sendMessage(message);
                    }
                }
            }
            messageAdapter.refreshSelectLast();
        }
    }

    private String getContent(String key) {
        ArrayMap<String, String> map = new ArrayMap<>();
        map.put("1", "AI修复照片是指使用人工智能技术来修复图片，使老旧图片焕发出来新的活力。比如，80年代照片很多都是黑白的，可以用AI技术去把老旧泛黄模糊的照片修复成彩色的数码照片，同时补充照片细节，增加清晰度");
        map.put("2", "AI会自动学习和记忆这些关键信息的颜色 ，比如树叶是绿色、人脸是肤色等。因此，它可以很快开始对黑白照片里的场景进行彩色化。AI通过算法和大量已有图片的训练完成的学习。先对照片进行图像分割，区分出标志性物体。 " +
                "比如树木、天空、人脸、服装……尽管很难做到完美无缺，但还原过后的老照片还是能给人很多惊喜。 PS：由于AI修复是算法学习的“经验”，推演出来的结果，因此和真实照片、真实景物会存在差异。");
        map.put("3", "黑白照片，相对模糊的照片，人脸照片，大头照，半身照等");
        map.put("4", "打开去遮挡，选择图片，然后用矩形框选中要去除的遮挡物，尽可能的保持矩形框和遮挡物大小相等");
        map.put("5", "照片清晰度恢复功能适用于那些相对模糊的照片，通过ai算法让字迹变得清晰，还原人脸的五官，色彩的锐利度等");
        map.put("6", "如果照片长时间的处理中，可能是图片在处理的过程中出了问题或者图片不符合处理的要求，请退出重试或者换一张照片，处理不成功不会计入有效使用次数。");
        map.put("7", "如果支付成功以后点击还需要支付，请到【投诉与退款】里面填写申请退款或者进入人工客服服务");
        map.put("8", "申请退款的处理时间为3-5个工作日，审核成功后退款会退出原支付账号，审核失败会有短信通知");
        return map.get(key);
    }

    /**
     * 刷新列表，并且跳至最后一个item
     */
    public void refreshSelectLast() {
        if (messageAdapter != null) {
            messageAdapter.refreshSelectLast();
        }
    }

    public void refreshSelectLastDelay(long delay) {
        new Handler().postDelayed(() -> {
            if (messageAdapter != null) {
                messageAdapter.refreshSelectLast();
            }
        }, delay);
    }

    /**
     * 刷新页面,并跳至给定position
     *
     * @param position
     */
    public void refreshSeekTo(int position) {
        if (messageAdapter != null) {
            messageAdapter.refreshSeekTo(position);
        }
    }

    /**
     * 获取listview
     *
     * @return
     */
    public ListView getListView() {
        return listView;
    }

    /**
     * 获取SwipeRefreshLayout
     *
     * @return
     */
    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    public Message getItem(int position) {
        return messageAdapter.getItem(position);
    }

    /**
     * 设置是否显示用户昵称
     *
     * @param showUserNick
     */
    public void setShowUserNick(boolean showUserNick) {
        this.showUserNick = showUserNick;
    }

    public boolean isShowUserNick() {
        return showUserNick;
    }

    public enum ItemAction {
        ITEM_TO_NOTE,//跳转到留言页面
        ITEM_RESOLVED, //问题已经解决
        ITEM_UNSOLVED //问题未解决
    }

    public interface MessageListItemClickListener {
        void onResendClick(Message message);

        /**
         * 控件有对气泡做点击事件默认实现，如果需要自己实现，return true。
         * 当然也可以在相应的chatrow的onBubbleClick()方法里实现点击事件
         *
         * @param message
         * @return
         */
        boolean onBubbleClick(Message message);

        void onBubbleLongClick(Message message);

        void onUserAvatarClick(String username);

        void onMessageItemClick(Message message, ItemAction action);
    }

    /**
     * 设置list item里控件的点击事件
     *
     * @param listener
     */
    public void setItemClickListener(MessageListItemClickListener listener) {
        if (messageAdapter != null) {
            messageAdapter.setItemClickListener(listener);
        }
    }

    /**
     * 设置自定义chatrow提供者
     *
     * @param rowProvider
     */
    public void setCustomChatRowProvider(CustomChatRowProvider rowProvider) {
        if (messageAdapter != null) {
            messageAdapter.setCustomChatRowProvider(rowProvider);
        }
    }
}