package com.github.vankain.crashcanary_library;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by Administrator on 2016/1/22.
 */
public abstract class CrashCanaryContext {
    private static Context sAppContext;
    private static CrashCanaryContext sInstance = null;

    public CrashCanaryContext() {
    }

    public static void init(Context context, CrashCanaryContext blockCanaryContext) {
        sAppContext = context;
        sInstance = blockCanaryContext;
    }

    public static CrashCanaryContext get(){
        if (sInstance == null) {
            throw new RuntimeException("CrashCanaryContext not init");
        } else {
            return sInstance;
        }
    }

    public Context getContext() {
        return sAppContext;
    }

    /**
     * 用户id，方便联系用户和后台查询定位
     *
     * @return 用户id
     */
    public abstract String getUid();

    /**
     * 是否需要展示崩溃界面，如仅在Debug包开启
     *
     * @return 是否需要展示崩溃界面
     */
    public abstract boolean isNeedDisplay();

    /**
     * Log文件保存的位置，如"/crashcanary/log"
     *
     * @return Log文件保存的位置
     */
    public abstract String getLogPath();

    /**
     * 标示符，可以唯一标示该安装版本号，如版本+渠道名+编译平台
     *
     * @return apk唯一标示符
     */
    public abstract String getQualifier();

    /**
     * 获得写log线程的handler
     *
     * @return 写log线程的handler
     */
    public Handler getWriteLogFileThreadHandler() {
        return sWriteLogThread.getHandler();
    }
    private static HandlerThreadWrapper sLoopThread = new HandlerThreadWrapper("loop");
    private static HandlerThreadWrapper sWriteLogThread = new HandlerThreadWrapper("writelog");

    private static class HandlerThreadWrapper {
        private Handler handler = null;

        public HandlerThreadWrapper(String name) {
            HandlerThread handlerThread = new HandlerThread("CrashCanaryThread_" + name);
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
        }

        public Handler getHandler() {
            return handler;
        }
    }

}
