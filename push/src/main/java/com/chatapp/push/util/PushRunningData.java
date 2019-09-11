package com.chatapp.push.util;

import com.chatapp.push.handle.impl.BaseHandleListener;

/**
 * Created by White on 2019/7/4.
 */

public class PushRunningData {
    private PushCallback callback;
    private boolean isForeground = true;
    private String pushAlias = "chat_app_test_value";

    private static PushRunningData mInstance;
    public static PushRunningData getInstance() {
        if (mInstance == null) {
            synchronized (PushRunningData.class) {
                if (mInstance == null) {
                    mInstance = new PushRunningData();
                }
            }
        }
        return mInstance;
    }

    public PushRunningData setCallbacks(PushCallback cb){
        this.callback = cb;
        return mInstance;
    }


    //***判断app是否在前台
    public void setIsForeground(boolean foreground){
        this.isForeground = foreground;
    }

    public boolean getIsForeground(){
        return this.isForeground;
    }

    //***推送的别名，用户id的哈希值来算
    public void setAlias(String alias){
        this.pushAlias = alias;
    }

    public String getAlias(){
        return pushAlias;
    }


    public enum CallBacKType {
        comeSomePush,
        pushIsOpened,
        loggerString
    }

    public void callBackOpened(CallBacKType type){
        callBack(type, 0 , "");
    }

    public void callBackComeMsg(int value){
        callBack(CallBacKType.comeSomePush, value , "");
    }

    public void callBackString(CallBacKType type, String value){
        callBack(type, 0 , value);
    }

    public void callBack(CallBacKType type, int intValue,String stringvalue){
        if (callback != null){
            switch (type){
                case comeSomePush:
                    callback.comeSomePush(intValue);
                    break;
                case pushIsOpened:
                    callback.pushIsOpened();
                    break;
                case loggerString:
                    callback.loggerString(stringvalue);
                    break;
            }
        }
    }


}
