package com.chatapp.push.target.xiaomi;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.chatapp.push.handle.PushReceiverHandleManager;
import com.chatapp.push.model.PushTargetEnum;
import com.chatapp.push.model.ReceiverInfo;
import com.chatapp.push.target.BasePushTargetInit;
import com.chatapp.push.util.ApplicationUtil;
import com.chatapp.push.util.PushRunningData;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;

/**
 * 小米推送的初始化
 * Created by luoming on 2018/5/28.
 */

public class XiaomiInit extends BasePushTargetInit {
    private static final String TAG = "XiaomiInit";

    public XiaomiInit(Application context) {
        super(context);
        //注册SDK
        String appId = ApplicationUtil.getMetaData(context, "XMPUSH_APPID");
        String appKey = ApplicationUtil.getMetaData(context, "XMPUSH_APPKEY");
        MiPushClient.registerPush(context, appId.replaceAll(" ", ""), appKey.replaceAll(" ", ""));
    }

    @Override
    public void setAlias(Context context, String alias, ReceiverInfo registerInfo) {
        PushRunningData.getInstance().callBackString(PushRunningData.CallBacKType.loggerString, "设置小米推送备注：" + alias);
        MiPushClient.setAlias(context, alias, null);
        ReceiverInfo aliasInfo = new ReceiverInfo();
        aliasInfo.setContent(alias);
        aliasInfo.setPushTarget(PushTargetEnum.XIAOMI);
        PushReceiverHandleManager.getInstance().onAliasSet(context, aliasInfo);

    }
}
