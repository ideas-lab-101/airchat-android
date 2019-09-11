package com.android.crypt.chatapp.utility.Websocket.Link;

import com.neovisionaries.ws.client.WebSocketException;

/**
 * Created by White on 2019/3/19.
 *
 * 这个是websocket的回掉接口
 */

public interface WebsocketCallbacks {


    //**连接
    void websocketConnected();
    void websocketConnectError(WebSocketException exception);
    void websocketDisconnected();

    //** 接收到消息
    void websocketTextMessage(String string);

    //**网络不可用
    void netError();

    void websocketStartReconect();

}
