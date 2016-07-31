package meng.customerservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class ChatActivity extends FragmentActivity {

    public static ChatActivity activityInstance;
    private ChatFragment chatFragment;
    String toChatUsername = "alfred";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        activityInstance = this;
        // 聊天人或群id
        // toChatUsername = HelpDeskPreferenceUtils.getInstance(this).getSettingCustomerAccount();
        // 可以直接new EaseChatFratFragment使用
        chatFragment = new ChatFragment();
        Intent intent = getIntent();
        intent.putExtra("userId", toChatUsername);
        intent.putExtra("showUserNick", true);
        // 传入参数
        chatFragment.setArguments(intent.getExtras());
        getSupportFragmentManager().beginTransaction().add(R.id.container, chatFragment).commit();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityInstance = null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // 点击notification bar进入聊天页面，保证只有一个聊天页面
        String username = intent.getStringExtra("userId");
        if (toChatUsername.equals(username))
            super.onNewIntent(intent);
        else {
            finish();
            startActivity(intent);
        }

    }
}
