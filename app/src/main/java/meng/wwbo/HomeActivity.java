package meng.wwbo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;

import java.util.List;

import meng.customerservice.CustomerServiceManager;
import meng.customerservice.easeui.EmptyEMMessageListener;
import meng.customerservice.easeui.utils.EaseCommonUtils;

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
        if (!CustomerServiceManager.getInstance().isLoggedIn()) {
            loginHuanxinServer();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.contact_kefu) {
            gotoConversationDetail("[咨询] 高三物理尖子班XXD");
        } else if (v.getId() == R.id.conversation_container) {
            gotoConversationDetail("");
        }
    }

    public void loginHuanxinServer() {
        CustomerServiceManager.getInstance().login("dluffy", "222222",
                new CustomerServiceManager.LoginListener() {
                    @Override
                    public void onSuccess() {
                        renderConversationViews();
                    }

                    @Override
                    public void onFail(String errorMsg) {
                        Toast.makeText(HomeActivity.this, "登陆环信服务器失败", Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderConversationViews();
        EMClient.getInstance().chatManager().addMessageListener(msgListener);
        CustomerServiceManager.getInstance().addActivity(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EMClient.getInstance().chatManager().removeMessageListener(msgListener);
        CustomerServiceManager.getInstance().removeActivity(this);
    }

    private EMMessageListener msgListener = new EmptyEMMessageListener() {
        @Override
        public void onMessageReceived(List<EMMessage> list) {
            super.onMessageReceived(list);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    renderConversationViews();
                }
            });
        }
    };

    private void renderConversationViews() {
        EMConversation conversation = CustomerServiceManager.getInstance().getConversation();
        if (conversation == null) {
            conversationContainer.setVisibility(View.GONE);
        } else {
            conversationContainer.setVisibility(View.VISIBLE);
            EMMessage lastMsg = conversation.getLastMessage();
            lastMsgView.setText(EaseCommonUtils.getMessageDigest(lastMsg));
            int unreadCount = conversation.getUnreadMsgCount();
            unreadCountView.setText(String.valueOf(conversation.getUnreadMsgCount()));
            unreadCountView.setVisibility(unreadCount > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void gotoConversationDetail(String contextMessage) {
        CustomerServiceManager.launchConversationDetail(this, contextMessage);
    }
}
