package meng.customerservice;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMOptions;

import java.util.Map;

/**
 * Created by meng on 16/8/1.
 */
public class CustomerServiceManager {
    private static final String TAG = CustomerServiceManager.class.getSimpleName();
    private Context appContext;
    private Handler mainHandler;
    private static CustomerServiceManager instance = new CustomerServiceManager();

    public static CustomerServiceManager getInstance() {
        return instance;
    }

    private CustomerServiceManager() {}

    public void init(Context context, boolean debugMode) {
        EMClient.getInstance().init(context, getDefaultChatOptions());
        setDebugMode(debugMode);
        this.appContext = context;
        mainHandler = new Handler(Looper.getMainLooper());
        if (isLoggedIn()) {
            EMClient.getInstance().chatManager().loadAllConversations();
        }
        // TODO(mwang): 16/8/1 设置客服昵称头像,全局监听等
    }

    protected EMOptions getDefaultChatOptions() {
        EMOptions options = new EMOptions();
        options.setAcceptInvitationAlways(false);
        options.setRequireAck(false); // 已读回执
        options.setRequireDeliveryAck(false);
        return options;
    }

    public void setDebugMode(boolean isDebug) {
        EMClient.getInstance().setDebugMode(isDebug);
    }

    public EMConversation getConversation() {
        Map<String, EMConversation> conversations = EMClient.getInstance().chatManager()
                .getAllConversations();
        if (conversations == null || conversations.size() < 1) {
            return null;
        } else {
            return conversations.values().iterator().next();
        }
    }

    public boolean isLoggedIn() {
        return EMClient.getInstance().isLoggedInBefore();
    }

    public void login(String uname, String upwd, final LoginListener loginListener) {
        // TODO(mwang): 16/8/1 login
        EMClient.getInstance().login(uname, upwd, new EMCallBack() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "login onSuccess!");
                EMClient.getInstance().chatManager().loadAllConversations();
                if (loginListener != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            loginListener.onSuccess();
                        }
                    });
                }
            }

            @Override
            public void onProgress(int progress, String status) {}

            @Override
            public void onError(final int code, final String message) {
                Log.d(TAG, "login onError! errorMsg = " + message);
                if (loginListener != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            loginListener.onFail(message);
                        }
                    });
                }
            }
        });
    }

    public void logout() {
        EMClient.getInstance().logout(true/* 解绑GCM或者小米推送的token */);
    }

    public interface LoginListener {
        void onSuccess();

        void onFail(String errorMsg);
    }
}
