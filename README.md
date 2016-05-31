# CrashCanary
It's a library that can save your application crash exception. App only needs one-line-code to setup and provide application context.

这个库可以帮你保存你 app 奔溃的日志。

# Why need it
when we give our application to the tester, and they found the app crash later, but we no log or we can't get the divice and link our computer
to print log, but use the library, we can get the log easy.

为什么我们需要这个库？当有我们把 app 提交测试人员的时候，测试人员发现了 bug，很多时候交给了开发人员的时候就没法重现。这样对 bug 很难修复，所以加入这个库就可以把我们的 奔溃信息保存起来。

# Getting started

```gradle
dependencies {
        compile 'com.github.vankain:crashcanary-library:1.0.0'
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
