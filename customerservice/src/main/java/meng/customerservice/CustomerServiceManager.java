package meng.customerservice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.util.EMLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import meng.customerservice.easeui.EaseConstant;
import meng.customerservice.easeui.EaseNotifier;
import meng.customerservice.easeui.EmptyEMMessageListener;

/**
 * Created by meng on 16/8/1.
 */
public class CustomerServiceManager {
    private static final String TAG = CustomerServiceManager.class.getSimpleName();
    private static CustomerServiceManager instance = new CustomerServiceManager();
    private Context appContext;
    private Handler mainHandler;
    private EaseNotifier easeNotifier;
    private String userAvatarUrl;
    /**
     * 用来记录注册了eventListener的foreground Activity
     */
    private List<Activity> msgListenerForegroundActivities = new ArrayList<>();
    /**
     * 全局事件监听 因为可能会有UI页面先处理到这个消息，所以一般如果UI页面已经处理，这里就不需要再次处理
     * msgListenerForegroundActivities.size() <= 0
     * 意味着所有页面都已经在后台运行，或者已经离开Activity Stack
     */
    private EMMessageListener globalMessageListener = new EmptyEMMessageListener() {
        @Override
        public void onMessageReceived(List<EMMessage> list) {
            if (!hasForegroundActivities()) {
                for (EMMessage message : list) {
                    easeNotifier.onNewMsg(message);
                }
            }
        }

        @Override
        public void onCmdMessageReceived(List<EMMessage> list) {
            for (EMMessage message : list) {
                EMLog.d(TAG, "收到透传消息");
                EMCmdMessageBody cmdMsgBody = (EMCmdMessageBody) message.getBody();
                final String action = cmdMsgBody.action();// 获取自定义action
                EMLog.d(TAG,
                        String.format("透传消息：action:%s,message:%s", action, message.toString()));
                // TODO(mwang): 16/8/1 暂时不清楚有什么应用
            }
        }
    };
    private EMConnectionListener connectionListener = new EMConnectionListener() {
        @Override
        public void onDisconnected(int error) {
            if (error == EMError.USER_REMOVED) {
                showToast("环信帐号被移除!");
            } else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                showToast("环信帐号在别的设备登录");
            }
        }

        @Override
        public void onConnected() {}
    };

    private CustomerServiceManager() {}

    public static CustomerServiceManager getInstance() {
        return instance;
    }

    public EaseNotifier getNotifier() {
        return easeNotifier;
    }

    public void init(Context context, boolean debugMode) {
        EMClient.getInstance().init(context, getDefaultChatOptions());
        easeNotifier = new EaseNotifier();
        easeNotifier.init(context);
        setDebugMode(debugMode);
        this.appContext = context;
        mainHandler = new Handler(Looper.getMainLooper());
        if (isLoggedIn()) {
            EMClient.getInstance().chatManager().loadAllConversations();
        }
        EMClient.getInstance().addConnectionListener(connectionListener);
        EMClient.getInstance().chatManager().addMessageListener(globalMessageListener);
    }

    private EMOptions getDefaultChatOptions() {
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

    public void setUserAvatarUrl(String url) {
        userAvatarUrl = url;
    }

    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public boolean isLoggedIn() {
        return EMClient.getInstance().isLoggedInBefore();
    }

    public void login(String uname, String upwd, final LoginListener loginListener) {
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

    public static void launchConversationDetail(Context context, String contextMessage) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EaseConstant.EXTRA_CONTEXT_TEXT_MESSAGE, contextMessage);
        context.startActivity(intent);
    }

    public void addActivity(Activity activity) {
        if (!msgListenerForegroundActivities.contains(activity)) {
            msgListenerForegroundActivities.add(0, activity);
        }
    }

    public void removeActivity(Activity activity) {
        msgListenerForegroundActivities.remove(activity);
    }

    public boolean hasForegroundActivities() {
        return msgListenerForegroundActivities.size() != 0;
    }

    private void showToast(final String msg) {
        if (msgListenerForegroundActivities.size() < 0) {
            return;
        }
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(appContext, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public interface LoginListener {
        void onSuccess();

        void onFail(String errorMsg);
    }
}
