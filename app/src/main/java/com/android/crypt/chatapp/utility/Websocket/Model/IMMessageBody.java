package com.android.crypt.chatapp.utility.Websocket.Model;

/**
 * Created by White on 2019/3/20.
 *
 * 封装 鉴权对象 发送给服务器
 *
 * 所有发送给websocket，需要存入数据库的值首字母都大写（配合golang和mongodb机制）
 *
 */

public class IMMessageBody {
    private int  ver;
    private int  op;
    private int  seq;
    private Object body;

    public IMMessageBody(Object body, int op){
        this.ver = 1;
        this.op = op;
        this.seq = 1;
        this.body = body;
    }

    public int getOp(){
        return this.op;
    }
    public Object getBody(){
        return this.body;
    }

}
