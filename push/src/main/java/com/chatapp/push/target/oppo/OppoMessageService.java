package com.chatapp.push.target.oppo;

import android.content.Context;
import android.util.Log;

import com.chatapp.push.handle.PushReceiverHandleManager;
import com.chatapp.push.model.PushTargetEnum;
import com.chatapp.push.model.ReceiverInfo;
import com.coloros.mcssdk.PushService;
import com.coloros.mcssdk.mode.AppMessage;
import com.coloros.mcssdk.mode.CommandMessage;
import com.coloros.mcssdk.mode.SptDataMessage;

/**
 * OPPO消息接收处理器
 * 作者：luoming on 2018/9/30.
 * 邮箱：luomingbear@163.com
 * 版本：v1.0
 */
public class OppoMessageService extends PushService {
    private static final String TAG = "OppoMessageService";

    /**
     * 普通应用消息
     *
     * @param context
     * @param appMessage
     */
    @Override
    public void processMessage(Context context, AppMessage appMessage) {
        super.processMessage(context, appMessage);
        Log.d(TAG, "app processMessage: handle:" + appMessage);
        ReceiverInfo info = new ReceiverInfo();
        info.setTitle(appMessage.getTitle());
        info.setContent(appMessage.getContent());
        info.setPushTarget(PushTargetEnum.OPPO);
        info.setRawData(appMessage);
        PushReceiverHandleManager.getInstance().onNotificationReceived(context, info);
    }

    /**
     * 透传消息处理，应用可以打开页面或者执行命令,如果应用不需要处理透传消息，则不需要重写此方法
     *
     * @param context
     * @param sptDataMessage
     */
    @Override
    public void processMessage(Context context, SptDataMessage sptDataMessage) {
        super.processMessage(context, sptDataMessage);
        Log.d(TAG, "spt processMessage: handle:" + sptDataMessage);
        ReceiverInfo info = new ReceiverInfo();
        info.setContent(sptDataMessage.getContent());
        info.setExtra(sptDataMessage.getDescription());
        info.setPushTarget(PushTargetEnum.OPPO);
        info.setRawData(sptDataMessage);
        PushReceiverHandleManager.getInstance().onMessageReceived(context, info);
    }

    /**
     * 命令消息，主要是服务端对客户端调用的反馈，一般应用不需要重写此方法
     *
     * @param context
     * @param commandMessage
     */
    @Override
    public void processMessage(Context context, CommandMessage commandMessage) {
        super.processMessage(context, commandMessage);
        Log.d(TAG, "command processMessage: " + commandMessage);
    }
}
