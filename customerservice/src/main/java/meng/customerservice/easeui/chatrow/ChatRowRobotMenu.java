package meng.customerservice.easeui.chatrow;

import android.content.Context;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.exceptions.HyphenateException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import meng.customerservice.ChatActivity;
import meng.customerservice.R;
import meng.customerservice.easeui.EaseConstant;
import meng.customerservice.easeui.utils.EaseCommonUtils;
import meng.customerservice.easeui.utils.EaseSmileUtils;

public class ChatRowRobotMenu extends EaseChatRow {

    TextView tvTitle;
    LinearLayout tvList;
    private TextView contentView;

    public ChatRowRobotMenu(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflateView() {
        if (EaseCommonUtils.isRobotMenuMessage(message)) {
            inflater.inflate(
                    message.direct() == EMMessage.Direct.RECEIVE ? R.layout.em_row_received_menu
                            : R.layout.ease_row_sent_message,
                    this);
        }
    }

    @Override
    protected void onFindViewById() {
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvList = (LinearLayout) findViewById(R.id.ll_layout);
        contentView = (TextView) findViewById(R.id.tv_chatcontent);
    }

    @Override
    protected void onUpdateView() {
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onSetUpView() {
        if (message.direct() == EMMessage.Direct.RECEIVE) {
            try {
                JSONObject jsonObj = message
                        .getJSONObjectAttribute(EaseConstant.MESSAGE_ATTR_MSGTYPE);
                if (jsonObj.has("choice")) {
                    JSONObject jsonChoice = jsonObj.getJSONObject("choice");
                    if (jsonChoice.has("title")) {
                        String title = jsonChoice.getString("title");
                        tvTitle.setText(title);
                    }
                    if (jsonChoice.has("items")) {
                        setRobotMenuListMessageLayout(tvList, jsonChoice.getJSONArray("items"));
                    } else if (jsonChoice.has("list")) {
                        setRobotMenuMessagesLayout(tvList, jsonChoice.getJSONArray("list"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
            Spannable span = EaseSmileUtils.getSmiledText(context, txtBody.getMessage());
            // 设置内容
            contentView.setText(span, BufferType.SPANNABLE);
            handleTextMessage();
        }

    }

    protected void handleTextMessage() {
        if (message.direct() == EMMessage.Direct.SEND) {
            setMessageSendCallback();
            switch (message.status()) {
                case CREATE:
                    progressBar.setVisibility(View.VISIBLE);
                    statusView.setVisibility(View.GONE);
                    break;
                case SUCCESS: // 发送成功
                    progressBar.setVisibility(View.GONE);
                    statusView.setVisibility(View.GONE);
                    break;
                case FAIL: // 发送失败
                    progressBar.setVisibility(View.GONE);
                    statusView.setVisibility(View.VISIBLE);
                    break;
                case INPROGRESS: // 发送中
                    progressBar.setVisibility(View.VISIBLE);
                    statusView.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        } else {
            if (!message.isAcked() && message.getChatType() == EMMessage.ChatType.Chat) {
                try {
                    EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(),
                            message.getMsgId());
                    message.setAcked(true);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onBubbleClick() {}

    private boolean isTransferKefuChoice(String choice) {
        return choice != null && choice.equals(context.getString(R.string.transfer_to_kefu));
    }

    private void setRobotMenuMessagesLayout(LinearLayout parentView, JSONArray jsonArr) {
        try {
            parentView.removeAllViews();
            for (int i = 0; i < jsonArr.length(); i++) {
                addChoiceItem(parentView, jsonArr.getString(i), null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setRobotMenuListMessageLayout(LinearLayout parentView, JSONArray jsonArr) {
        try {
            parentView.removeAllViews();
            for (int i = 0; i < jsonArr.length(); i++) {
                JSONObject itemJson = jsonArr.getJSONObject(i);
                addChoiceItem(parentView, itemJson.getString("name"), itemJson.getString("id"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addChoiceItem(LinearLayout parentView, final String itemStr, final String itemId) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final View itemView = inflater.inflate(isTransferKefuChoice(itemStr)
                ? R.layout.transfer_kefu_item : R.layout.robot_choice_item, parentView,
                false);
        ((TextView) itemView.findViewById(R.id.desc)).setText(itemStr);
        itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ChatActivity) context).sendRobotMessage(itemStr, itemId);
            }
        });
        parentView.addView(itemView);
    }
}
