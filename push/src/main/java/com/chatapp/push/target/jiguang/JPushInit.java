package com.chatapp.push.target.jiguang;

import android.app.Application;
import android.content.Context;

import com.chatapp.push.model.ReceiverInfo;
import com.chatapp.push.target.BasePushTargetInit;
import com.chatapp.push.util.PushRunningData;

import cn.jpush.android.api.JPushInterface;

/**
 * 极光推送的初始化服务
 * Created by luoming on 2018/5/28.
 */

public class JPushInit extends BasePushTargetInit {
    private static final String TAG = "JPushInit";

    public JPushInit(Application application) {
        super(application);
        JPushInterface.init(application);
        PushRunningData.getInstance().callBackString(PushRunningData.CallBacKType.loggerString, "初始化极光推送");

    }

    @Override
    public void setAlias(Context context, String alias, ReceiverInfo registerInfo) {
        PushRunningData.getInstance().callBackString(PushRunningData.CallBacKType.loggerString, "极光设置别名：" + alias);

        JPushInterface.setAlias(context, 0, alias);
    }

}
