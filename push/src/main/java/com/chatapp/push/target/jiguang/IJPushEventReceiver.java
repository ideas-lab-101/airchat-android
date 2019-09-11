package com.chatapp.push.target.jiguang;

import android.content.Context;

import com.chatapp.push.handle.PushReceiverHandleManager;
import com.chatapp.push.model.PushTargetEnum;
import com.chatapp.push.model.ReceiverInfo;

import cn.jpush.android.api.JPushMessage;
import cn.jpush.android.service.JPushMessageReceiver;

/**
 * 自定义的alin/tag设置接口回调
 * Created by luoming on 2018/5/28.
 */

public class IJPushEventReceiver extends JPushMessageReceiver {
    private static final String TAG = "IJPushEventReceiver";

    @Override
    public void onTagOperatorResult(Context var1, JPushMessage var2) {
    }

    @Override
    public void onCheckTagOperatorResult(Context var1, JPushMessage var2) {

    }

    @Override
    public void onAliasOperatorResult(Context var1, JPushMessage var2) {
        ReceiverInfo aliasInfo = new ReceiverInfo();
        aliasInfo.setContent(var2.getAlias());
        aliasInfo.setPushTarget(PushTargetEnum.JPUSH);
        aliasInfo.setRawData(var2);
        PushReceiverHandleManager.getInstance().onAliasSet(var1, aliasInfo);
    }

    @Override
    public void onMobileNumberOperatorResult(Context var1, JPushMessage var2) {

    }
}
