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

    public abstract boolean isNeedDisplay();

    public abstract String getLogPath();

    public abstract String getQualifier();

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
