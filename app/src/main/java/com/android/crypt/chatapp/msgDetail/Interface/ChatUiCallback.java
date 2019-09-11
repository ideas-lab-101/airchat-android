package com.android.crypt.chatapp.msgDetail.Interface;

/**
 * Created by White on 2019/4/25.
 */

public interface ChatUiCallback {

    void showActionsheets();
    void sendDefaultEmoji(String imageString);
    void sendCollectEmji(String imageString);
    void addCollectEmji();
}
