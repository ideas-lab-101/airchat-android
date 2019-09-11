package com.chatapp.push.target.jiguang;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.chatapp.push.R;
import com.chatapp.push.handle.PushReceiverHandleManager;
import com.chatapp.push.model.PushTargetEnum;
import com.chatapp.push.model.ReceiverInfo;
import com.chatapp.push.util.PushRunningData;

import org.json.JSONObject;

import cn.jpush.android.api.JPushInterface;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * 自定义的极光推送接收
 * Created by luoming on 2018/5/28.
 */

public class JPushBroadcastReceiver extends BroadcastReceiver{



    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            //用户注册SDK的intent
            PushRunningData.getInstance().callBackString(PushRunningData.CallBacKType.loggerString, "JPush 用户注册成功");
            ReceiverInfo info = new ReceiverInfo();
            info.setPushTarget(PushTargetEnum.JPUSH);
            info.setRawData(intent);
            info.setTitle("极光推送注册成功");
            PushReceiverHandleManager.getInstance().onRegistration(context, info);
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            PushRunningData.getInstance().callBackString(PushRunningData.CallBacKType.loggerString, "接受到推送下来的自定义消息");
            PushReceiverHandleManager.getInstance().onMessageReceived(context, convert2MessageReceiverInfo(intent));
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            //**通知不处理，无法控制是否显示
//            PushRunningData.getInstance().callBackString(PushRunningData.CallBacKType.loggerString, "接受到推送下来的通知");
//            processCustomMessage(context, intent);
//            PushReceiverHandleManager.getInstance().onNotificationReceived(context, convert2NotificationReceiverInfo(intent));
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            PushRunningData.getInstance().callBackString(PushRunningData.CallBacKType.loggerString, "用户点击打开了通知");
            PushReceiverHandleManager.getInstance().onNotificationOpened(context, convert2NotificationReceiverInfo(intent));
        } else {
            PushRunningData.getInstance().callBackString(PushRunningData.CallBacKType.loggerString, "其他 " + intent.getAction());
        }
    }

    /**
     * 将intent的数据转化为ReceiverInfo用于处理
     *
     * @param intent
     * @return
     */
    private ReceiverInfo convert2NotificationReceiverInfo(Intent intent) {
        ReceiverInfo info = new ReceiverInfo();
        Bundle bundle = intent.getExtras();
        String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
        String message = bundle.getString(JPushInterface.EXTRA_ALERT);
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);

        info.setTitle(title);
        info.setContent(message);
        info.setExtra(extras);
        info.setPushTarget(PushTargetEnum.JPUSH);
        info.setRawData(intent);
        return info;
    }

    /**
     * 将intent的数据转化为ReceiverInfo用于处理
     *
     * @param intent
     * @return
     */
    private ReceiverInfo convert2MessageReceiverInfo(Intent intent) {
        ReceiverInfo info = new ReceiverInfo();
        Bundle bundle = intent.getExtras();
        String title = bundle.getString(JPushInterface.EXTRA_TITLE);
        String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);

        info.setTitle(title);
        info.setContent(message);
        info.setExtra(extras);
        info.setPushTarget(PushTargetEnum.JPUSH);
        info.setRawData(intent);
        return info;
    }


}
