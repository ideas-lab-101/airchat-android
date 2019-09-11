package com.android.crypt.chatapp.utility.Websocket.Model;

/**
 * Created by White on 2019/3/22.
 *
 * 私信消息
 *
 * 通知服务器当前消息已经被客户端成功接收，此时不再推送这个私信
 *
 */

public class SendToConfirmIMMsgIsGet {
    private String MessageReceiver;
    private String MessageIdClient;

    public SendToConfirmIMMsgIsGet(String MessageReceiver, String MessageIdClient){
        this.MessageReceiver = MessageReceiver;
        this.MessageIdClient = MessageIdClient;
    }
}
