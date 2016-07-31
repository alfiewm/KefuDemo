package meng.wwbo;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

public class ChatActivity extends ActionBarActivity {

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
}
