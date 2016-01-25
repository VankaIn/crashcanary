package com.github.vankain.crashcanary_library;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;

import com.github.vankain.crashcanary_library.log.Crash;
import com.github.vankain.crashcanary_library.log.LogWriter;
import com.github.vankain.crashcanary_library.ui.DisplayCrashActivity;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.HONEYCOMB;
import static android.os.Build.VERSION_CODES.JELLY_BEAN;

/**
 * Created by Administrator on 2016/1/22.
 */
public class CrashCanary {

    private static CrashCanary sInstance;

    private CrashCanary() {
        try {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ex.printStackTrace(pw);
                    String crashMessage = sw.toString();
                    String time = new SimpleDateFormat("yy-MM-dd HH:mm:ss", Locale.getDefault())
                            .format(new Date());
                    Crash crash = Crash.newInstance()
                            .setCause(ex.getCause().toString())
                            .setTime(time)
                            .setCrashMessage(crashMessage)
                            .flushString();
                    LogWriter.saveLooperLog(crash.toString());
                    if (CrashCanaryContext.get().isNeedDisplay()) {
                        Context context = CrashCanaryContext.get().getContext();
                        PendingIntent pendingIntent = DisplayCrashActivity.createPendingIntent(context, time);
                        String contentTitle = context.getString(R.string.crash_canary_class_has_crashed, time);
                        String contentText = context.getString(R.string.crash_canary_notification_message);
                        CrashCanary.this.notify(contentTitle, contentText, pendingIntent);
                    }

                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();//不处理
        }
    }

    public static CrashCanary install(Context context, CrashCanaryContext crashCanaryContext) {
        CrashCanaryContext.init(context, crashCanaryContext);
        return get();
    }

    public static CrashCanary get() {
        if (sInstance == null) {
            synchronized (CrashCanary.class) {
                if (sInstance == null) {
                    sInstance = new CrashCanary();
                }
            }
        }
        return sInstance;
    }

    @TargetApi(HONEYCOMB)
    private void notify(String contentTitle, String contentText, PendingIntent pendingIntent) {
        NotificationManager notificationManager = (NotificationManager)
                CrashCanaryContext.get().getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification;
        if (SDK_INT < HONEYCOMB) {
            notification = new Notification();
            notification.icon = R.drawable.crash_canary_notification;
            notification.when = System.currentTimeMillis();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            // TODO SUPPORT
            //notification.setLatestEventInfo(BlockCanaryContext.get().getContext(), contentTitle, contentText, pendingIntent);
        } else {
            Notification.Builder builder = new Notification.Builder(CrashCanaryContext.get().getContext())
                    .setSmallIcon(R.drawable.crash_canary_notification)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);
            if (SDK_INT < JELLY_BEAN) {
                notification = builder.getNotification();
            } else {
                notification = builder.build();
            }
        }
        notificationManager.notify(0xDEAFBEEF, notification);
    }
}
