package com.android.crypt.chatapp.utility.Websocket;

import com.android.crypt.chatapp.utility.Websocket.Model.MessageSendState;
import com.android.crypt.chatapp.utility.Websocket.Model.ReceiveRecalledInfo;
import com.android.crypt.chatapp.utility.Websocket.Model.SendMessageEnBody;
import com.android.crypt.chatapp.utility.Websocket.Model.ReceiveSpecialInfo;
import com.android.crypt.chatapp.utility.Websocket.Model.ReceiverIsTypingBody;

/**
 * Created by White on 2019/4/16.
 */

public interface MessageCallbacks {
    //
    void connectSucTips();

    //私信消息
    void comeANewIMMessage(SendMessageEnBody body);        //有一个新私信
    void noOfflineMessage();                               //没有离线消息
    void freshMsgListView();                               //刷新msglist列表

    //私信发送状态
    void messageSendSuccess(MessageSendState body);                  //消息发送成功
    void messageSendFailed(MessageSendState body);                   //消息发送失败

    //私信提示
    void userIsTyping(ReceiverIsTypingBody body);            //用户正在输入
    void userRecalledAMessage(ReceiveRecalledInfo body);     //用户撤回一个消息

    //其他消息
    void applyForFriend(ReceiveSpecialInfo body);           //用户给我发送好友申请
    void allowMeASFriend(ReceiveSpecialInfo body);          //用户同意加我好友
    void someOneDeleteMe(ReceiveSpecialInfo body);          //用户删除我

    //其他连接状态
    void chatConnected();
    void chatDisconnected();
}

