package com.android.crypt.chatapp.utility.Common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.android.crypt.chatapp.MainActivity;
import com.android.crypt.chatapp.R;
import com.chatapp.push.util.PushRunningData;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by White on 2019/7/11.
 */

public class NotifyUtils {
    public static String CALENDAR_ID = "chat_push";


    public static void showNotification(Context context, String title, String msg, Class<?> activity) {
        PendingIntent pendingIntent = null;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CALENDAR_ID, "123",
                    NotificationManager.IMPORTANCE_HIGH);
            // 设置渠道描述
            notificationChannel.setDescription("测试通知组");
            // 是否绕过请勿打扰模式
            notificationChannel.canBypassDnd();
            // 设置绕过请勿打扰模式
            notificationChannel.setBypassDnd(true);
            // 桌面Launcher的消息角标
            notificationChannel.canShowBadge();
            // 设置显示桌面Launcher的消息角标
            notificationChannel.setShowBadge(true);
            // 设置通知出现时声音，默认通知是有声音的
            notificationChannel.setSound(null, null);
            // 设置通知出现时的闪灯（如果 android 设备支持的话）
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.WHITE);
            // 设置通知出现时的震动（如果 android 设备支持的话）
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400,
                    300, 200, 400});
            manager.createNotificationChannel(notificationChannel);
        }



        //****自定义铃声
        int type = com.chatapp.push.R.raw.cat;
        String voice = RunningData.getInstance().getCurPushVoice();
        if (voice.equals("dog.mp3")){
            type = com.chatapp.push.R.raw.dog;
        }else if(voice.equals("dev.mp3")){
            type = com.chatapp.push.R.raw.dev;
        }else if(voice.equals("blank.mp3")){
            type = com.chatapp.push.R.raw.blank;
        }
        Notification notification;
        if (activity != null) {
            Intent intent = new Intent(context, activity);
            intent.putExtra("i", 1);
            pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                notification = new Notification.Builder(context)
                        .setContentIntent(pendingIntent)//跳转的activity
                        .setContentTitle(title)
                        .setAutoCancel(true)//标题和点击消失
                        .setContentText(msg)//文本
                        .setSmallIcon(R.mipmap.ic_icon)//图标
                        .setPriority(Notification.PRIORITY_MAX) //设置该通知优先级
                        .setFullScreenIntent(pendingIntent, true)
                        .setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + type))
                        .build();
            } else {
                notification = new Notification.Builder(context)
                        .setContentIntent(pendingIntent)
                        .setContentTitle(title).setAutoCancel(true)
                        .setContentText(msg)
                        .setSmallIcon(R.mipmap.ic_icon)
                        .setPriority(Notification.PRIORITY_MAX) //设置该通知优先级
                        .setFullScreenIntent(pendingIntent, true)
                        .setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + type))
                        .getNotification();
            }
            manager.notify(111, notification);
        }
    }
}
