package com.android.crypt.chatapp.utility.Websocket.Model;

/**
 * Created by White on 2019/3/22.
 *
 * 消息发送状态，某个消息是否成功
 *
 *
 */

public class MessageSendState {

    private String MessageIdClient;
    private String MessageSendTime;

    public MessageSendState(String MessageIdClient, String MessageSendTime){
        this.MessageIdClient = MessageIdClient;
        this.MessageSendTime = MessageSendTime;
    }

    public String getMessageIdClientt(){
        return this.MessageIdClient;
    }

    public String getMessageSendTime(){
        return this.MessageSendTime;
    }
}
