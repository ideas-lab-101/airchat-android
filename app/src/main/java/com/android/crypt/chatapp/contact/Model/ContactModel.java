package com.android.crypt.chatapp.contact.Model;
import com.android.crypt.chatapp.contact.cn.CN;

import java.io.Serializable;

/**
 * Created by you on 2017/9/11.
 */

public class ContactModel implements CN, Serializable {

    public String avatar_url;
    public String username;
    public String label;
    public String account;
    public String introduction;
    public String public_key;
    public String friend_id;


    public ContactModel(String avatar_url,
                        String username,
                        String label,
                        String account,
                        String introduction,
                        String public_key,
                        String friend_id){
        this.avatar_url = avatar_url;
        this.username = username;
        this.label = label;
        this.account = account;
        this.introduction = introduction;
        this.public_key = public_key;
        this.friend_id = friend_id;
    }


    @Override
    public String chinese() {
        return label;
    }



}
