package com.android.crypt.chatapp.utility.Websocket.Model;

/**
 * Created by White on 2019/3/22.
 *
 * 通知服务器撤回消息已经被客户端成功接收，此时不再推送
 *
 */

public class SendToConfirmRecalledMsgIsGet {
    private String MessageCreator;
    private String MessageIdClient;

    public SendToConfirmRecalledMsgIsGet(String MessageCreator, String MessageIdClient){
        this.MessageCreator = MessageCreator;
        this.MessageIdClient = MessageIdClient;
    }
}
