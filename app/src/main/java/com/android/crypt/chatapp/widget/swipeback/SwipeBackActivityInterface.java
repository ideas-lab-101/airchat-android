package com.android.crypt.chatapp.widget.swipeback;


import com.android.crypt.chatapp.widget.swipeback.lib.SwipeBackLayout;

/**
 * @author Yrom
 */
public interface SwipeBackActivityInterface {
    /**
     * @return the SwipeBackLayout associated with this activity.
     */
    public abstract SwipeBackLayout getSwipeBackLayout();

    public abstract void setSwipeBackEnable(boolean enable);

    /**
     * Scroll out contentView and finish the activity
     */
    public abstract void scrollToFinishActivity();

}
