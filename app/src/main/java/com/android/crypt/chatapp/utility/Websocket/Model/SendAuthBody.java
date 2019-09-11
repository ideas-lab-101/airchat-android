package com.android.crypt.chatapp.utility.Websocket.Model;

/**
 * Created by White on 2019/3/20.
 * 鉴权对象
 *
 * 所有发送给websocket，需要存入数据库的值首字母都大写（配合golang和mongodb机制）
 */

public class SendAuthBody {
    private String UserName;
    private String Password;

    public SendAuthBody(String UserName, String Password){
        this.UserName = UserName;
        this.Password = Password;
    }

}
