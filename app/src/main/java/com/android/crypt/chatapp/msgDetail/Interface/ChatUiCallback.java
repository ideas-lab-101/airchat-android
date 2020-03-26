package com.android.crypt.chatapp.msgDetail.Interface;

import com.android.crypt.chatapp.msgDetail.model.CollectEmoji;

/**
 * Created by White on 2019/4/25.
 */

public interface ChatUiCallback {

    void showActionsheets();
    void sendDefaultEmoji(CollectEmoji imageString);
    void sendCollectEmji(CollectEmoji imageString);
    void addCollectEmji();
}
