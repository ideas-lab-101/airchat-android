package com.android.crypt.chatapp.utility.Common;

import org.json.JSONArray;

/**
 * Created by White on 2019/7/9.
 */

public interface ACNotifyMessage {
    void freshFriendList();
    void newFriendApply();
    void freshFriendListWithData(JSONArray mList);
}
