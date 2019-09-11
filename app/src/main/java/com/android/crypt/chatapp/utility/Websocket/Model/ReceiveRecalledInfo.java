package com.android.crypt.chatapp.utility.Websocket.Model;

/**
 * Created by White on 2019/3/22.
 * x
 * x消息的撤回，确定撤回了那条消息
 *
 */

public class ReceiveRecalledInfo {
    private String MessageCreator;
    private String MessageIdClient;

    private int OP;
    private int OnceMsg;

    public ReceiveRecalledInfo(String MessageCreator,
                               String MessageIdClient){
        this.OP = 18;
        this.OnceMsg = 1;
        this.MessageCreator = MessageCreator;
        this.MessageIdClient = MessageIdClient;
    }

    public String getMessageIdClient(){
        return this.MessageIdClient;
    }

    public String getMessageCreator(){
        return this.MessageCreator;
    }



}
