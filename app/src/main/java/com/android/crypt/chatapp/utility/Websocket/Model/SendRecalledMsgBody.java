package com.android.crypt.chatapp.utility.Websocket.Model;

/**
 * Created by White on 2019/3/22.
 */

public class SendRecalledMsgBody {
    private String MessageCreator;
    private String MessageReceiver;
    private String MessageIdClient;

    public SendRecalledMsgBody(String MessageCreator,
                               String MessageReceiver,
                               String MessageIdClient){
        this.MessageCreator = MessageCreator;
        this.MessageReceiver = MessageReceiver;
        this.MessageIdClient = MessageIdClient;
    }

}
