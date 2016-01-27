package com.github.vankain.crashcanary_library.log;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.github.vankain.crashcanary_library.CrashCanaryContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Created by Administrator on 2016/1/22.
 */
public class Crash {
    public static final String SEPARATOR = "\r\n";
    public static final String KV = " = ";
    public static final String KEY_QUA = "qualifier";
    public static final String KEY_MODEL = "model";
    public static final String KEY_API = "apilevel";
    public static final String KEY_IMEI = "imei";
    public static final String KEY_UID = "uid";
    public static final String KEY_VERSION_NAME = "versionName";
    public static final String KEY_VERSION_CODE = "versionCode";
    public static final String KEY_TIME = "time";
    public static final String KEY_CAUSE = "cause";
    public static final String KEY_CRASH_MESSAGE = "crashMessage";


    public String time;
    public String crashMessage;
    public String cause;
    public File logFile;
    public String versionName = "";
    public String versionCode;
    public String imei = "";
    public String qualifier;
    public String apiLevel = "";
    public String model;
    public String uid;

    private StringBuilder basicSb = new StringBuilder();
    private StringBuilder timeSb = new StringBuilder();
    private StringBuilder causeSb = new StringBuilder();
    private StringBuilder crashMessageSb = new StringBuilder();
    private StringBuilder statusSb = new StringBuilder();



    public static Crash newInstance() {
        Crash crash = new Crash();
        Context context = CrashCanaryContext.get().getContext();
        if (crash.versionName == null || crash.versionName.length() == 0) {
            try {
                PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                crash.versionCode = info.versionCode + "";
                crash.versionName = info.versionName + "";
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if (crash.imei == null || crash.imei.length() == 0) {
            TelephonyManager mTManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            crash.imei = mTManager.getDeviceId();
        }
        crash.qualifier = CrashCanaryContext.get().getQualifier();
        crash.apiLevel = Build.VERSION.SDK_INT + " " + Build.VERSION.RELEASE;
        crash.model = Build.MODEL;
        crash.uid = CrashCanaryContext.get().getUid();
        return crash;
    }

    /**
     * 从保存的log文件创建log对象
     *
     * @param file looper log file
     * @return LooperLog created from log file
     */
    public static Crash newInstance(File file) {
        Crash crash = new Crash();
        crash.logFile = file;

        BufferedReader reader = null;
        try {
            InputStreamReader in = new InputStreamReader(new FileInputStream(file), "UTF-8");

            reader = new BufferedReader(in);
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.startsWith(KEY_QUA)) {
                    crash.qualifier = line.split(KV)[1];
                } else if (line.startsWith(KEY_MODEL)) {
                    crash.model = line.split(KV)[1];
                } else if (line.startsWith(KEY_API)) {
                    crash.apiLevel = line.split(KV)[1];
                } else if (line.startsWith(KEY_IMEI)) {
                    crash.imei = line.split(KV)[1];
                } else if(line.startsWith(KEY_VERSION_NAME)){
                    crash.versionName = line.split(KV)[1];
                } else if(line.startsWith(KEY_VERSION_CODE)){
                    crash.versionCode = line.split(KV)[1];
                }else if (line.startsWith(KEY_UID)) {
                    crash.uid = line.split(KV)[1];
                } else if(line.startsWith(KEY_CAUSE)){
                    crash.cause = line.split(KV)[1];
                } else if(line.startsWith(KEY_TIME)){
                    crash.time = line.split(KV)[1];
                } else if(line.startsWith(KEY_CRASH_MESSAGE)){
                    crash.crashMessage = line.split(KV)[1];
                    String[] split = line.split(KV);
                    if (split.length > 1) {
                        StringBuilder crashMessageSb = new StringBuilder(split[1]);
                        crashMessageSb.append(line.split(KV)[1]).append(SEPARATOR);
                        line = reader.readLine();

                        // read until SEPARATOR appears
                        while (line != null) {
                            if (!line.equals("")) {
                                crashMessageSb.append(line).append(SEPARATOR);
                            } else {
                                break;
                            }
                            line = reader.readLine();
                        }
                        crash.crashMessage = crashMessageSb.toString();
                    }

                }
            }
            reader.close();
            reader = null;
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                    reader = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        crash.flushString();
        return crash;
    }

    public Crash flushString() {
        basicSb.delete(0, basicSb.length());
        timeSb.delete(0, timeSb.length());
        causeSb.delete(0, causeSb.length());
        crashMessageSb.delete(0, crashMessageSb.length());
        statusSb.delete(0, statusSb.length());

        String separator = SEPARATOR;
        basicSb.append(KEY_QUA).append(KV).append(qualifier).append(separator);
        basicSb.append(KEY_VERSION_NAME).append(KV).append(versionName).append(separator);
        basicSb.append(KEY_VERSION_CODE).append(KV).append(versionCode).append(separator);
        basicSb.append(KEY_IMEI).append(KV).append(imei).append(separator);
        basicSb.append(KEY_MODEL).append(KV).append(Build.MODEL).append(separator);
        basicSb.append(KEY_UID).append(KV).append(uid).append(separator);
        basicSb.append(KEY_API).append(KV).append(apiLevel).append(separator);

        timeSb.append(KEY_TIME).append(KV).append(time).append(separator);
        causeSb.append(KEY_CAUSE).append(KV).append(cause).append(separator);

        crashMessageSb.append(KEY_CRASH_MESSAGE).append(KV).append(crashMessage).append(separator);


        return this;
    }

    public String getBasicString() {
        return basicSb.toString();
    }

    public String getTimeString() {
        return timeSb.toString();
    }

    public String getCauseString() {
        return causeSb.toString();
    }

    public String getCrashMessageString(){
        return crashMessageSb.toString();
    }

    public Crash setTime(String time){
        this.time = time;
        return this;
    }

    public Crash setCause(String cause){
        this.cause = cause;
        return this;
    }

    public Crash setCrashMessage(String crashMessage){
        this.crashMessage = crashMessage;
        return this;
    }


    public String toString() {
        return String.valueOf(basicSb) + timeSb + causeSb + crashMessageSb + statusSb;
    }

}
