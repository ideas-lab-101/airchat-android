package com.chatapp.push.handle.impl;

import android.content.Context;
import android.util.Log;

import com.chatapp.push.model.ReceiverInfo;

/**
 * 当设置了别名之后回调，通常是向自己的服务器发送别名，用来标识用户
 * Created by luoming on 2018/5/29.
 */

public class HandleReceiverAlias implements BaseHandleListener {
    private static final String TAG = "PushHandleReceiverAlias";

    @Override
    public void handle(Context context, ReceiverInfo info) {
        Log.d(TAG, "handle: " + info.getContent());
    }
}
