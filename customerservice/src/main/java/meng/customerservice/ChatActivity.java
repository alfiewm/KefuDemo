package meng.customerservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;

import meng.customerservice.easeui.EaseConstant;

public class ChatActivity extends FragmentActivity {

    private ChatFragment chatFragment;
    private String peerUserId = "alfred";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!EMClient.getInstance().isLoggedInBefore()) {
            Toast.makeText(this, "请登录先!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_chat);
        String contextMsg = "";
        if (getIntent().getExtras() != null) {
            contextMsg = getIntent().getExtras().getString(EaseConstant.EXTRA_CONTEXT_TEXT_MESSAGE,
                    "");
            peerUserId = getIntent().getExtras().getString(EaseConstant.EXTRA_USER_ID, peerUserId);
        }
        Bundle bundle = new Bundle();
        bundle.putString(EaseConstant.EXTRA_USER_ID, peerUserId);
        bundle.putBoolean(EaseConstant.EXTRA_SHOW_USERNICK, true);
        bundle.putString(EaseConstant.EXTRA_CONTEXT_TEXT_MESSAGE, contextMsg);
        chatFragment = new ChatFragment();
        chatFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.container, chatFragment).commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // 点击notification bar进入聊天页面，保证只有一个聊天页面
        String username = intent.getStringExtra("userId");
        if (peerUserId.equals(username))
            super.onNewIntent(intent);
        else {
            finish();
            startActivity(intent);
        }
    }

    public void sendRobotMessage(String txtContent, String menuId) {
        chatFragment.sendRobotMessage(txtContent, menuId);
    }
}
