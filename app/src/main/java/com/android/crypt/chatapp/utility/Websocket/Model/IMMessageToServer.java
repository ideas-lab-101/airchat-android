package com.android.crypt.chatapp.utility.Websocket.Model;

/**
 * Created by White on 2019/3/20.
 *
 * 所有发送给websocket，需要存入数据库的值首字母都大写（配合golang和mongodb机制）
 *
 *
 */

public class IMMessageToServer {
    private String JsonBody;

    public IMMessageToServer(String JsonBody){
        this.JsonBody = JsonBody;
    }

    public String getJsonBody(){
        return this.JsonBody;
    }
}
