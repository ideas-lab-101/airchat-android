package com.chatapp.push.handle.impl;

import android.content.Context;
import android.util.Log;

import com.chatapp.push.model.ReceiverInfo;

/**
 * 处理用户注册SDK
 * 设置别名
 * Created by luoming on 2018/5/28.
 */

public class HandleReceiverRegistration implements BaseHandleListener {
    private static final String TAG = "HandleReceiverRegistrat";

    @Override
    public void handle(Context context, ReceiverInfo info) {
        Log.d(TAG, "handle: " + info.getTitle());
    }
}
