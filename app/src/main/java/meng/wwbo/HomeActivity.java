package meng.wwbo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;

import meng.customerservice.ChatActivity;
import meng.customerservice.CustomerServiceManager;
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
            gotoConversation();
        } else if (v.getId() == R.id.conversation_container) {
            gotoConversation();
        }
    }

    public void loginHuanxinServer() {
        CustomerServiceManager.getInstance().login("dluffy", "222222",
                new CustomerServiceManager.LoginListener() {
                    @Override
                    public void onSuccess() {
                        initConversationViews();
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
        initConversationViews();
    }

    private void initConversationViews() {
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

    private void gotoConversation() {
        startActivity(new Intent(HomeActivity.this, ChatActivity.class));
    }
}
