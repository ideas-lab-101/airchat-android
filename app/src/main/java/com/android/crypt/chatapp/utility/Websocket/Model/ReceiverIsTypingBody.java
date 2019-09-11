package com.android.crypt.chatapp.utility.Websocket.Model;

/**
 * Created by White on 2019/3/22.
 *
 *  xxxx正在输入
 */

public class ReceiverIsTypingBody {
    private int OP;
    private int OneMsg;
    private String WhoIsTyping;

    public ReceiverIsTypingBody(String WhoIsTyping){
        this.OP = 20;
        this.OneMsg = 1;
        this.WhoIsTyping = WhoIsTyping;
    }

    public String getWhoIsTyping(){
        return this.WhoIsTyping;
    }
}
