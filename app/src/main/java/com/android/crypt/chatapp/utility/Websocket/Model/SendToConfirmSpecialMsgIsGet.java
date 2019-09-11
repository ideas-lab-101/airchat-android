package com.android.crypt.chatapp.utility.Websocket.Model;

/**
 * Created by White on 2019/3/22.
 *
 * 通知服务器已经获得特殊消息通知
 *
 * 添加好友、好友加我、好友删除我等等
 *
 */

public class SendToConfirmSpecialMsgIsGet {
    private String MessageCreator;
    private String MessageIdClient;

    public SendToConfirmSpecialMsgIsGet(String MessageCreator, String MessageIdClient){
        this.MessageCreator = MessageCreator;
        this.MessageIdClient = MessageIdClient;
    }
}
