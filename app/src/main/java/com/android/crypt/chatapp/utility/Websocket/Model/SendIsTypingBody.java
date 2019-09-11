package com.android.crypt.chatapp.utility.Websocket.Model;

/**
 * Created by White on 2019/3/22.
 */

public class SendIsTypingBody {
    private String WhoIsTyping;
    private String TypingMsgReceiver;

    public SendIsTypingBody(String WhoIsTyping, String TypingMsgReceiver){
        this.WhoIsTyping = WhoIsTyping;
        this.TypingMsgReceiver = TypingMsgReceiver;
    }

}
