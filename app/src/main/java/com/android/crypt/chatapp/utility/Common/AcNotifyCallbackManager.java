package com.android.crypt.chatapp.utility.Common;

/**
 * Created by White on 2019/7/9.
 */

public class AcNotifyCallbackManager {
    private ACNotifyMessage fregment_contact_callback;

    private static AcNotifyCallbackManager mInstance;
    public static AcNotifyCallbackManager getInstance() {
        if (mInstance == null) {
            synchronized (RunningData.class) {
                if (mInstance == null) {
                    mInstance = new AcNotifyCallbackManager();
                }
            }
        }
        return mInstance;
    }

    public AcNotifyCallbackManager setContactCallback(ACNotifyMessage callback){
        this.fregment_contact_callback = callback;
        return mInstance;
    }


    public void contact_callback_fresh(){
        if (this.fregment_contact_callback != null){
            this.fregment_contact_callback.freshFriendList();
        }
    }

    public void contact_callback_apply(){
        if (this.fregment_contact_callback != null){
            this.fregment_contact_callback.newFriendApply();
        }
    }


}
