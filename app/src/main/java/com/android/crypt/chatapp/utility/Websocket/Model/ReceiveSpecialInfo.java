package com.android.crypt.chatapp.utility.Websocket.Model;

/**
 * Created by White on 2019/3/22.
 *
 *  消息发送成功？or发送失败？
 */

public class ReceiveSpecialInfo {
    private String Content;
    private String MessageCreator;
    private String MessageIdClient;

    private int MessageKind;
    private int OP;
    private int OnceMsg;

    public ReceiveSpecialInfo(String Content,
                              String MessageCreator,
                              String MessageIdClient,
                              int MessageKind){
        this.Content = Content;
        this.MessageCreator = MessageCreator;
        this.MessageIdClient = MessageIdClient;
        this.MessageKind = MessageKind;
        this.OP = 23;
        this.OnceMsg = 1;
    }

    public String getMessageIdClient(){
        return this.MessageIdClient;
    }

    public String getMessageCreator(){
        return this.MessageCreator;
    }


}
