package meng.wwbo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

import java.util.Map;

public class HomeActivity extends Activity implements View.OnClickListener {
    private static final String TAG = HomeActivity.class.getSimpleName();

    private Button btnContactKefu;
    private TextView lastMsgView;
    private TextView unreadCountView;
    private View conversationContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        btnContactKefu = (Button) findViewById(R.id.contact_kefu);
        conversationContainer = findViewById(R.id.conversation_container);
        lastMsgView = (TextView) findViewById(R.id.last_msg);
        unreadCountView = (TextView) findViewById(R.id.unread_count);
        btnContactKefu.setOnClickListener(this);
        conversationContainer.setOnClickListener(this);
        if (!EMClient.getInstance().isLoggedInBefore()) {
            loginHuanxinServer("nRobin", "222222");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.contact_kefu) {
            gotoConversation();
        } else if (v.getId() == R.id.conversation_container) {
            gotoConversation();
        }
    }

    public void loginHuanxinServer(final String uname, final String upwd) {
        // login huanxin server
        EMClient.getInstance().login(uname, upwd, new EMCallBack() {
            @Override
            public void onSuccess() {
                // DemoHelper.getInstance().setCurrentUserName(uname);
                // DemoHelper.getInstance().setCurrentPassword(upwd);
                Log.d(TAG, "onSuccess: 登录环信成功!");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initConversationViews();
                    }
                });
            }

            @Override
            public void onProgress(int progress, String status) {}

            @Override
            public void onError(final int code, final String message) {
                Log.d(TAG, "onError: 登录环信失败!");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(HomeActivity.this, "登录环信服务器失败" + message, Toast.LENGTH_SHORT)
                                .show();
                        finish();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initConversationViews();
    }

    private void initConversationViews() {
        EMClient.getInstance().chatManager().loadAllConversations();
        Map<String, EMConversation> conversations = EMClient.getInstance().chatManager()
                .getAllConversations();
        if (conversations.size() < 1) {
            conversationContainer.setVisibility(View.GONE);
        } else {
            EMConversation kefuCon = conversations.values().iterator().next();
            EMMessage lastMsg = kefuCon.getLastMessage();
            if (lastMsg.getType() == EMMessage.Type.IMAGE) {
                lastMsgView.setText("[图片]");
            } else {
                EMTextMessageBody textBody = (EMTextMessageBody) lastMsg.getBody();
                lastMsgView.setText(textBody.getMessage());
            }
            if (kefuCon.getUnreadMsgCount() > 0) {
                unreadCountView.setVisibility(View.VISIBLE);
                unreadCountView.setText(kefuCon.getUnreadMsgCount());
            } else {
                unreadCountView.setVisibility(View.GONE);
            }
        }
    }

    private void gotoConversation() {
        startActivity(new Intent(HomeActivity.this, ChatActivity.class));
//        .putExtra( Constant.INTENT_CODE_IMG_SELECTED_KEY, selectedIndex).putExtra( Constant.MESSAGE_TO_INTENT_EXTRA, messageToIndex));
    }
}
