package com.chatapp.push.handle.impl;

import android.content.Context;
import android.util.Log;

import com.chatapp.push.model.ReceiverInfo;
import com.chatapp.push.util.PushRunningData;

/**
 * 处理推送的消息，不会主动显示通知栏
 * Created by luoming on 2018/5/28.
 */

public class HandleReceiverMessage implements BaseHandleListener {

    @Override
    public void handle(Context context, ReceiverInfo info) {
        PushRunningData.getInstance().callBackComeMsg(1);
    }
}
