package meng.wwbo.easeui;

import android.content.Context;
import android.widget.Toast;

import meng.customerservice.CustomerServiceManager;

/**
 * Created by meng on 16/8/3.
 */
public class CustomerServiceHelper {
    private static final String TAG = CustomerServiceHelper.class.getSimpleName();

    private CustomerServiceHelper() {}

    public static void initCustomerService(Context context, boolean debug) {
        CustomerServiceManager.getInstance().init(context, debug);
    }

    public static void ensuerCustomerServiceConnected(final Context context) {
        if (CustomerServiceManager.getInstance().isLoggedIn()) {
            return;
        }
        requestHuanxinAccount(new RequestHuanxinAccountCallback() {
            @Override
            public void onSuccess(String userName, String password) {
                CustomerServiceManager.getInstance().login(userName, password, null);
            }

            @Override
            public void onFail(String errorMessage) {
                Toast.makeText(context, "请求客服账号失败,请稍后重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void contactOnlineCustomerService(final Context context,
            final String contextMessage) {
        if (!CustomerServiceManager.getInstance().isLoggedIn()) {
            requestHuanxinAccount(new RequestHuanxinAccountCallback() {
                @Override
                public void onSuccess(String userName, String password) {
                    CustomerServiceManager.getInstance().login(userName, password,
                            new CustomerServiceManager.LoginListener() {
                                @Override
                                public void onSuccess() {
                                    CustomerServiceManager.launchConversationDetail(context,
                                            contextMessage);
                                    CustomerServiceManager.getInstance().setUserAvatarUrl(
                                            "http://thesource.com/wp-content/uploads/2015/02/Pablo_Picasso1.jpg");
                                }

                                @Override
                                public void onFail(String errorMsg) {
                                    Toast.makeText(context, "连接客服系统失败,请稍后重试",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }

                @Override
                public void onFail(String errorMessage) {
                    Toast.makeText(context, "请求客服账号失败,请稍后重试", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            CustomerServiceManager.launchConversationDetail(context, contextMessage);
        }
    }

    private static void requestHuanxinAccount(RequestHuanxinAccountCallback callback) {
        // TODO(mwang): 16/8/3 call server spi
        callback.onSuccess("dluffy", "222222");
    }

    private interface RequestHuanxinAccountCallback {
        void onSuccess(String userName, String password);

        void onFail(String errorMessage);
    }
}
