package meng.wwbo;

import android.app.Application;
import android.content.Context;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;

/**
 * Created by meng on 16/7/30.
 */
public class DemoApplication extends Application {

    public static Context applicationContext;
    private static DemoApplication instance;

    public static DemoApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
        instance = this;
        EMClient.getInstance().init(this, getDefaultChatOptions());
        // TODO(mwang): 16/7/30 set debug false in release mode 
        EMClient.getInstance().setDebugMode(true);
    }

    protected EMOptions getDefaultChatOptions() {
        EMOptions options = new EMOptions();
        // change to need confirm contact invitation
        options.setAcceptInvitationAlways(false);
        // set if need read ack
        options.setRequireAck(true);
        // set if need delivery ack
        options.setRequireDeliveryAck(false);
        return options;
    }
}
