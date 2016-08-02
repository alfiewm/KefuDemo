package meng.customerservice.easeui.chatrow;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.exceptions.HyphenateException;

import org.json.JSONException;
import org.json.JSONObject;

import meng.customerservice.R;
import meng.customerservice.easeui.EaseConstant;
import meng.customerservice.easeui.utils.EaseCommonUtils;

/**
 * Created by meng on 16/8/1.
 */
public class ChatRowTransferToKefu extends EaseChatRow {

    TextView btnTransfer;
    TextView tvTitle;
    String uuid = null;
    String serviceSessionId = null;

    public ChatRowTransferToKefu(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflateView() {
        if (EaseCommonUtils.isTransferToKefuMsg(message)) {
            inflater.inflate(message.direct() == EMMessage.Direct.RECEIVE ? R.layout.em_row_received_transfertokefu
                    : R.layout.em_row_sent_transfertokefu, this);
        }
    }

    @Override
    protected void onFindViewById() {
        btnTransfer = (TextView) findViewById(R.id.desc);
        tvTitle = (TextView) findViewById(R.id.tv_chatcontent);
    }

    @Override
    protected void onUpdateView() {
    }

    @Override
    protected void onSetUpView() {
        if (message.getType() == EMMessage.Type.TXT) {
            EMTextMessageBody msgBody = (EMTextMessageBody) message.getBody();
            tvTitle.setText(msgBody.getMessage());
        }
        if (btnTransfer == null) {
            return;
        }
        try {
            JSONObject jsonWeiChat = message.getJSONObjectAttribute(EaseConstant.WEICHAT_MSG);
            if (jsonWeiChat.has("ctrlArgs")) {
                JSONObject jsonCtrlArgs = jsonWeiChat.getJSONObject("ctrlArgs");
                uuid = jsonCtrlArgs.getString("id");
                serviceSessionId = jsonCtrlArgs.getString("serviceSessionId");
                String btnLabel = jsonCtrlArgs.getString("label");
                if (!TextUtils.isEmpty(btnLabel)) {
                    btnTransfer.setText(btnLabel);
                }
            }
        } catch (HyphenateException | JSONException e) {
            e.printStackTrace();
        }

        btnTransfer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendTransferToKefuMessage();
            }
        });
    }

    private void sendTransferToKefuMessage() {
        if (TextUtils.isEmpty(uuid) || TextUtils.isEmpty(serviceSessionId)) {
            return;
        }
        EMMessage cmdMessage = EMMessage.createSendMessage(EMMessage.Type.CMD);
        cmdMessage.setReceipt(message.getFrom());
        EMCmdMessageBody cmdMsgBody = new EMCmdMessageBody("TransferToKf");
        cmdMessage.addBody(cmdMsgBody);
        JSONObject weichatJson = new JSONObject();
        JSONObject ctrlArgsJson = new JSONObject();
        try {
            ctrlArgsJson.put("id", uuid);
            ctrlArgsJson.put("serviceSessionId", serviceSessionId);
            weichatJson.put("ctrlArgs", ctrlArgsJson);
            cmdMessage.setAttribute(EaseConstant.WEICHAT_MSG, weichatJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        EMClient.getInstance().chatManager().sendMessage(cmdMessage);
    }


    @Override
    protected void onBubbleClick() {
    }
}
