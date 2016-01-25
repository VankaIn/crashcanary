package com.example.crashcanary.demo;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.github.vankain.crashcanary_library.CrashCanaryContext;

/**
 * Created by Administrator on 2016/1/22.
 */
public class AppCrashCanaryContext extends CrashCanaryContext {
    private static final String TAG = "AppCrashCanaryContext";

    @Override
    public String getUid() {
        return null;
    }

    @Override
    public boolean isNeedDisplay() {
        return true;
    }

    @Override
    public String getLogPath() {
        return "/crashcanary/performance";

    }

    @Override
    public String getQualifier() {
        String qualifier = "";
        try {
            PackageInfo info = BaseApplication.getAppContext().getPackageManager()
                    .getPackageInfo(BaseApplication.getAppContext().getPackageName(), 0);
            qualifier += info.versionCode + "_" + info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getQualifier exception", e);
        }
        return qualifier;
    }
}
