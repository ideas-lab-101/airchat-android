package com.chatapp.push.handle.impl;

import android.content.Context;

import com.chatapp.push.model.ReceiverInfo;

/**
 * 处理推送的消息
 * Created by luoming on 2018/5/28.
 */

public interface BaseHandleListener {
    void handle(Context context, ReceiverInfo info);
}
