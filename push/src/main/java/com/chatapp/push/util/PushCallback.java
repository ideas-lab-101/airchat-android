package com.chatapp.push.util;

/**
 * Created by White on 2019/7/8.
 */


public interface PushCallback {
    void comeSomePush(int count);
    void pushIsOpened();
    void loggerString(String value);
}
