package meng.wwbo;

import android.app.Application;
import android.content.Context;

import meng.wwbo.easeui.CustomerServiceHelper;

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
        CustomerServiceHelper.initCustomerService(this, true);
    }

}
