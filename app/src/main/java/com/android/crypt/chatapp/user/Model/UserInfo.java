package com.android.crypt.chatapp.user.Model;

import java.io.Serializable;

/**
 * Created by White on 2019/4/17.
 */

public class UserInfo implements Serializable {

    public String avatar_url;
    public String sex;
    public String username;
    public String login_name;
    public String introduction;
    public int user_id;
    public int snnumber;

    public UserInfo(String avatar_url,
                    String sex,
                    String username,
                    String login_name,
                    String introduction,
                    int user_id,
                    int snnumber){

        this.avatar_url = avatar_url;
        this.sex = sex;
        this.username = username;
        this.login_name = login_name;
        this.introduction = introduction;
        this.user_id = user_id;
        this.snnumber = snnumber;
    }

}
