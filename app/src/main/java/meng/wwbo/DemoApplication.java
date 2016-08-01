package meng.wwbo;

import android.app.Application;
import android.content.Context;

import meng.customerservice.CustomerServiceManager;

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
        // TODO(mwang): 16/7/31 判断在主进程,防止初始化两次
        CustomerServiceManager.getInstance().init(this, true);
    }

}
