package com.example.crashcanary.demo;

import android.app.Application;
import android.content.Context;

import com.github.vankain.crashcanary_library.CrashCanary;


/**
 * Created by Administrator on 2016/1/22.
 */
public class BaseApplication extends Application {

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        CrashCanary.install(this, new AppCrashCanaryContext());

    }

    public static Context getAppContext() {
        return sContext;
    }


}
