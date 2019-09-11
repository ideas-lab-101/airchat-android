package com.chatapp.push.target.xiaomi;

import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.chatapp.push.R;
import com.chatapp.push.handle.PushReceiverHandleManager;
import com.chatapp.push.model.PushTargetEnum;
import com.chatapp.push.model.ReceiverInfo;
import com.chatapp.push.util.PushRunningData;
import com.google.gson.Gson;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * 自定义的小米推送接收
 * Created by luoming on 2018/5/28.
 */

public class XiaomiBroadcastReceiver extends PushMessageReceiver {
    @Override
    public void onReceivePassThroughMessage(Context var1, MiPushMessage var2) {
        PushRunningData.getInstance().callBackString(PushRunningData.CallBacKType.loggerString, "onReceivePassThroughMessage");
        PushReceiverHandleManager.getInstance().onMessageReceived(var1, convert2ReceiverInfo(var2));
    }

    @Override
    public void onNotificationMessageClicked(Context var1, MiPushMessage var2) {
        PushRunningData.getInstance().callBackString(PushRunningData.CallBacKType.loggerString, "onNotificationMessageClicked");
        PushReceiverHandleManager.getInstance().onNotificationOpened(var1, convert2ReceiverInfo(var2));
    }

    @Override
    public void onNotificationMessageArrived(Context var1, MiPushMessage var2) {
        //***使用穿透消息
//        PushRunningData.getInstance().callBackString(PushRunningData.CallBacKType.loggerString, "onNotificationMessageArrived");
//        PushReceiverHandleManager.getInstance().onNotificationReceived(var1, convert2ReceiverInfo(var2));
//        playMusic(var1);
    }

    @Override
    public void onReceiveRegisterResult(Context var1, MiPushCommandMessage var2) {
        PushRunningData.getInstance().callBackString(PushRunningData.CallBacKType.loggerString, "小米推送注册成功");

        ReceiverInfo info = convert2ReceiverInfo(var2);
        info.setTitle("小米推送注册成功");
        info.setContent(var2.getCommand());
        PushReceiverHandleManager.getInstance().onRegistration(var1, info);
    }

    /**
     * 将intent的数据转化为ReceiverInfo用于处理
     *
     * @param miPushMessage
     * @return
     */
    private ReceiverInfo convert2ReceiverInfo(MiPushMessage miPushMessage) {
        ReceiverInfo info = new ReceiverInfo();
        info.setContent(miPushMessage.getContent());
        info.setPushTarget(PushTargetEnum.XIAOMI);
        info.setTitle(miPushMessage.getTitle());
        info.setRawData(miPushMessage);
        if (miPushMessage.getExtra() != null) {
            info.setExtra(new Gson().toJson(miPushMessage.getExtra()));
        }
        return info;
    }

    /**
     * 将intent的数据转化为ReceiverInfo用于处理
     *
     * @param miPushCommandMessage
     * @return
     */
    private ReceiverInfo convert2ReceiverInfo(MiPushCommandMessage miPushCommandMessage) {
        ReceiverInfo info = new ReceiverInfo();
        info.setContent(miPushCommandMessage.getCommand());
        info.setRawData(miPushCommandMessage);
        info.setPushTarget(PushTargetEnum.XIAOMI);
        return info;
    }

    private void playMusic(Context context) {
//        NotificationCompat.Builder notification = new NotificationCompat.Builder(context);
//        //这一步必须要有而且setSmallIcon也必须要，没有就会设置自定义声音不成功
//        notification.setAutoCancel(true).setSmallIcon(R.mipmap.ic_icon);
//        int type = R.raw.cat;
//        String voice = PushRunningData.getInstance().getCurPushVoice();
//        if (voice.equals("dog.mp3")){
//            type = R.raw.dog;
//        }else if(voice.equals("dev.mp3")){
//            type = R.raw.dev;
//        }else if(voice.equals("blank.mp3")){
//            type = R.raw.blank;
//        }
//        notification.setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + type));
//        //最后刷新notification是必须的
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
//        notificationManager.notify(1, notification.build());
    }
}
