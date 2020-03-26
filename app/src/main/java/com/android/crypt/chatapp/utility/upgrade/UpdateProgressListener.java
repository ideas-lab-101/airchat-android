package com.android.crypt.chatapp.utility.upgrade;

/**
 * Created by White on 2020/3/25.
 */

public interface UpdateProgressListener {
    void start();
    void update(int progress);
    void success();
    void error();
}
