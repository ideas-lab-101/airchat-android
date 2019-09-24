package com.android.crypt.chatapp.utility.Common;

/**
 * Created by White on 2019/9/19.
 */

public class ClickUtils {
    // 两次点击按钮之间的点击间隔不能少于1000毫秒
    private static final int MIN_CLICK_DELAY_TIME = 700;
    private static long lastClickTime = 0;

    public static boolean isFastClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            flag = true;
            lastClickTime = curClickTime;
        }

        return flag;
    }
}
