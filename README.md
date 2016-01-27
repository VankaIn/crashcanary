# CrashCanary
It's a library that can save your application crash exception. App only needs one-line-code to setup and provide application context.

# Why need it
when we give our application to the tester, and they found the app crash later, but we no log or we can't get the divice and link our computer
to print log, but use the library, we can get the log easy.

# Getting started

```gradle
dependencies {
        compile 'com.github.moduth:blockcanary-android:1.1.0'
}
```

# Usage

```java
public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        ...
        // Do it on main process
        CrashCanary.install(this, new AppCrashCanaryContext());
    }
}
```

Implement CrashCanaryContext context：
```java
public class AppCrashCanaryContext extends CrashCanaryContext {
    // override to provide context like app qualifier, uid, log save path..

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
```

# Screenshots
![image](/screenshots/demo.gif)  


# Thanks
参考以下开源库：<br />
https://github.com/moduth/blockcanary<br />
https://github.com/square/leakcanary<br />
