package com.android.crypt.chatapp.utility.Websocket;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;


import com.android.crypt.chatapp.contact.cn.CNPinyin;
import com.android.crypt.chatapp.msgList.model.MessageListModel;
import com.android.crypt.chatapp.utility.Cache.CacheTool;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.Crypt.CryTool;
import com.android.crypt.chatapp.utility.Crypt.EncryptUtil;
import com.android.crypt.chatapp.utility.Websocket.Link.NetStatusReceiver;
import com.android.crypt.chatapp.utility.Websocket.Link.WsManager;
import com.android.crypt.chatapp.utility.Websocket.Model.MessageSendState;
import com.android.crypt.chatapp.utility.Websocket.Model.ReceiveRecalledInfo;
import com.android.crypt.chatapp.utility.Websocket.Model.SendIsTypingBody;
import com.android.crypt.chatapp.utility.Websocket.Model.SendMessageEnBody;
import com.android.crypt.chatapp.utility.Websocket.Model.SendRecalledMsgBody;
import com.android.crypt.chatapp.utility.Websocket.Model.SendToConfirmIMMsgIsGet;
import com.android.crypt.chatapp.utility.Websocket.Model.SendToConfirmSpecialMsgIsGet;
import com.android.crypt.chatapp.utility.Websocket.Model.SendToResetPushNumber;
import com.android.crypt.chatapp.utility.Websocket.Model.ReceiveSpecialInfo;
import com.android.crypt.chatapp.utility.Websocket.Model.ReceiverIsTypingBody;
import com.android.crypt.chatapp.utility.Websocket.Model.SendToConfirmRecalledMsgIsGet;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocketException;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.contact.Model.ContactModel;
import com.android.crypt.chatapp.utility.Websocket.Link.WebsocketCallbacks;
import com.android.crypt.chatapp.utility.Websocket.Link.WsStatus;
import com.android.crypt.chatapp.utility.Websocket.Model.IMMessageBody;
import com.android.crypt.chatapp.utility.Websocket.Model.IMMessageToServer;
import com.android.crypt.chatapp.utility.Websocket.Model.SendAuthBody;
import com.android.crypt.chatapp.utility.Websocket.Model.SendHeartBeatBody;
import com.android.crypt.chatapp.utility.Websocket.Model.SendMessageBody;
import com.android.crypt.chatapp.utility.Websocket.Model.SendToGetOffLineMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class WsChatService extends Service implements WebsocketCallbacks {
    private WsManager wsManager = null;
    private MessageCallbacks mesListCallbacks;
    private MessageCallbacks mesDetailCallbacks;
    private Timer heartBeatTimer, heartBeatTimeOutCheckTimer, offLineTimer, resendPoolTimer;
    private int dyOfflineMsgGapTimer = 3000;
    private final WsChatServiceBinder wsChatServiceBinder = new WsChatServiceBinder();
    private ArrayList<SendMessageEnBody> resendPool;  //消息缓冲池
    private NetStatusReceiver netBroadcastReceiver = null;

    public class WsChatServiceBinder extends Binder {
        public WsChatService getService() {
            return WsChatService.this;
        }
    }

    public WsChatService() {

    }

    /**
     * message list     消息列表一个回掉
     * message detail   消息详情页面
     **/
    public void setMessageListCallBacks(MessageCallbacks callbacks) {
//        Logger.d("mesListCallbacks  赋值");
        this.mesListCallbacks = callbacks;
    }

    public void setMessageDetailCallBacks(MessageCallbacks callbacks) {
//        Logger.d("mesDetailCallbacks  赋值");
        this.mesDetailCallbacks = callbacks;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return wsChatServiceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    /***
     *   websocket 连接 与  断开
     *   onCreate()开始连接
     *   onDestroy()断开链接
     *   绑定回掉函数
     * ***/

    @Override
    public void onCreate() {
        super.onCreate();
        registerBroadcastReceiver();
        if (wsManager == null) {
            Logger.d("连接websocket");
            wsManager = WsManager.getInstance();
            wsManager.init(this);
            callbackChatDisconnected(); //连接断开回掉
        } else {
            wsManager.reconnect();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_REDELIVER_INTENT;
    }

    /**
     *
     * 网络状态广播
     *
     * ***/

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wsManager == null) {
            Logger.d("断开websocket");
            wsManager.disconnect();
            callbackChatDisconnected(); //连接断开回掉
        }

        if (netBroadcastReceiver != null) {
            //注销广播
            unregisterReceiver(netBroadcastReceiver);
        }
    }
    private void registerBroadcastReceiver() {
        //注册广播
        if (netBroadcastReceiver == null) {
            netBroadcastReceiver = new NetStatusReceiver();

            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(netBroadcastReceiver, filter);
        }
    }


    /**
     * 实现接口 websocket 的回掉
     * 根据连接状态调整UI
     **/

    public WsStatus getWsState(){
        return wsManager.getStatus();
    }

    @Override
    public void websocketConnected() {
        Logger.d("websocket 连接成功, 开始鉴权");
        authWithServer();
    }

    @Override
    public void websocketConnectError(WebSocketException exception) {
        Logger.d("连接错误" + exception.toString());
        callbackChatDisconnected(); //连接断开回掉
    }

    @Override
    public void websocketDisconnected() {
        Logger.d("断开连接");
        callbackChatDisconnected(); //连接断开回掉
    }

    @Override
    public void netError() {
        Logger.d("网络错误");
        callbackChatDisconnected(); //连接断开回掉
    }

    @Override
    public void websocketStartReconect() {
        callbackChatDisconnected(); //连接断开回掉
    }

    //     根据服务器返回字段走业务逻辑
    @Override
    public void websocketTextMessage(String string) {
        String key = RunningData.getInstance().getInnerAESKey();

        Gson gson = new Gson();
        List<IMMessageToServer> result = gson.fromJson(string, new TypeToken<List<IMMessageToServer>>() {
        }.getType());
        String en_content = result.get(0).getJsonBody();

        try {
            byte[] decryptedData = EncryptUtil.decryptBase64EncodeData(en_content.getBytes(), key.getBytes(), key.getBytes(), "AES/CBC/PKCS7Padding");
            String reveiveString = new String(decryptedData);
            IMMessageBody reveiver = gson.fromJson(reveiveString, IMMessageBody.class);

            int op = reveiver.getOp();
            if (op == 8) { //鉴权成功
                startHeartBeat();  //开启心跳
                callbackChatConnected();//连接成功回掉
            } else if (op == 3) { //心跳返回, 停止重连的计时器
                sendHeartBeatGetReceive();
                callbackConnectSucTips();
            } else if (op == 15) { //消息发送成功
                Object body = reveiver.getBody();  //这里包含了具体哪个消息发送成功
                Map<String, Object> mapState = objectToMap(body);
                String MessageIdClient = String.valueOf(mapState.get("MessageIdClient"));
                String MessageSendTime = String.valueOf(mapState.get("MessageSendTime"));
                MessageSendState msgState = new MessageSendState(MessageIdClient, MessageSendTime);

                callbackMessageSendSuccess(msgState);
                callbackConnectSucTips();
            } else if (op == 5) { //收到服务器消息
                Object body = reveiver.getBody();
                dealWithReceiveMessage(body);
                callbackConnectSucTips();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void dealWithReceiveMessage(Object body) {
        try {
            //开始处理服务器传过来的消息
            Map<String, Object> map = objectToMap(body);
//            Logger.d("map = " + map);
            int OnceMsg = getIntWithString(String.valueOf(map.get("OnceMsg")));
            if (OnceMsg == 1) {
                int OP = getIntWithString(String.valueOf(map.get("OP")));
                if (OP == 21) {
                    //没有离线消息
                    callbackNoOfflineMessage();
                } else if (OP == 20) {
                    //正在输入
                    String WhoIsTyping = String.valueOf(map.get("WhoIsTyping"));
                    ReceiverIsTypingBody typingBody = new ReceiverIsTypingBody(WhoIsTyping);
                    callbackUserIsTyping(typingBody);
                } else if (OP == 23) {
                    int MessageKind = getIntWithString(String.valueOf(map.get("MessageKind")));

                    String Content = String.valueOf(map.get("Content"));
                    String MessageCreator = String.valueOf(map.get("MessageCreator"));
                    String MessageIdClient = String.valueOf(map.get("MessageIdClient"));
                    String MessageReceive = String.valueOf(map.get("MessageReceive"));
                    ReceiveSpecialInfo speInfo = new ReceiveSpecialInfo(Content, MessageCreator, MessageReceive, MessageIdClient, MessageKind);
                    if (MessageKind == 2) {
                        //有好友申请
                        callbackApplyForFriend(speInfo);
                    } else if (MessageKind == 3) {
                        //好友统一加我
                        callbackAllowMeASFriend(speInfo);
                    } else if (MessageKind == 4) {
                        //好友删除我
                        callbackSomeOneDeleteMe(speInfo);
                    }
                } else if (OP == 18) {
                    String MessageCreator = String.valueOf(map.get("MessageCreator"));
                    String MessageIdClient = String.valueOf(map.get("MessageIdClient"));
                    //消息撤回
                    ReceiveRecalledInfo recalledInfo = new ReceiveRecalledInfo(MessageCreator, MessageIdClient);
                    callbackUserRecalledAMessage(recalledInfo);
                }
            } else { //这里是私信消息
                String MessageCreator = String.valueOf(map.get("MessageCreator"));
                String MessageReceiver = String.valueOf(map.get("MessageReceiver"));
                String MessageIdClient = String.valueOf(map.get("MessageIdClient"));
                String MessageSendTime = String.valueOf(map.get("MessageSendTime"));
                String Body_en = String.valueOf(map.get("Body_en"));
                String Key = String.valueOf(map.get("Key"));
                boolean MessageTag = (getIntWithString(String.valueOf(map.get("MessageTag"))) == 0)?false:true;
                boolean IsSendSuccess = (getIntWithString(String.valueOf(map.get("IsSendSuccess"))) == 0)?false:true;

                SendMessageEnBody enBody = new SendMessageEnBody(MessageCreator, MessageReceiver, MessageIdClient, MessageSendTime, Body_en, Key, MessageTag, IsSendSuccess);
                callbackComeANewIMMessage(enBody);

            }

        } catch (Exception e) {
            e.printStackTrace();
            Logger.d("dealWithReceiveMessage 转换出错" + e.toString());
        }
    }


    /**
     * 鉴权
     * String UserName
     * String Password
     **/

    private void authWithServer() {
        /**
         * 未完成，需要取出当前 用户名密码
         * **/

        String key = RunningData.getInstance().getInnerAESKey();
        String username = RunningData.getInstance().getCurrentAccount();
        String password = RunningData.getInstance().getCurrentPwd();

        SendAuthBody body = new SendAuthBody(username, password);
        IMMessageBody userInfo = new IMMessageBody(body, 7);
        Gson gson = new Gson();
        String originalString = gson.toJson(userInfo);

        try {
            byte[] encryptedData = EncryptUtil.encryptAndBase64Encode(originalString.getBytes(), key.getBytes(), key.getBytes(), "AES/CBC/PKCS5Padding");
            String en_content = new String(encryptedData); //加密后的字典字符串
            sendImMessage(en_content);

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    /***
     *
     * 心跳函数
     *
     * ****/
    private void startHeartBeat() {
        if (heartBeatTimer == null) {
            Logger.d("开启心跳, 30s 后进行第一次心跳连接，之后每90s重连依次");

            heartBeatTimer = new Timer();
            heartBeatTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sendHeartBeat();
                }
            }, RunningData.getInstance().heartBeatDelay(), RunningData.getInstance().heartBeatGap());
        }
    }

    private void sendHeartBeat() {
//        Logger.d("心跳函数！");
        //发送心跳数据，等待回应

        String key = RunningData.getInstance().getInnerAESKey();

        SendHeartBeatBody body = new SendHeartBeatBody();
        IMMessageBody userInfo = new IMMessageBody(body, 2);
        Gson gson = new Gson();
        String originalString = gson.toJson(userInfo);

        try {
            byte[] encryptedData = EncryptUtil.encryptAndBase64Encode(originalString.getBytes(), key.getBytes(), key.getBytes(), "AES/CBC/PKCS5Padding");
            String en_content = new String(encryptedData); //加密后的字典字符串
            sendImMessage(en_content);

            //检测心跳是否收到了回应，超时就重连 超过10s
            if (heartBeatTimeOutCheckTimer == null) {
                heartBeatTimeOutCheckTimer = new Timer();
                heartBeatTimeOutCheckTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Logger.d("心跳超时重连");
                        wsManager.reconnect();
                    }
                }, RunningData.getInstance().heartBeatTimeOut());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendHeartBeatGetReceive() {
//        Logger.d("心跳函数接收");
        if (heartBeatTimeOutCheckTimer != null) {
            heartBeatTimeOutCheckTimer.cancel();
            heartBeatTimeOutCheckTimer = null;
        }
    }


    /***
     *
     * 消息的发送方法
     *
     * ****/

    //【【【 ---- 发送私信消息
    //服务器是保存IM消息推送状态的，必须告诉服务器消息已被接收，否则会不停给客户端推送
    public void sendImMessageBody(SendMessageEnBody body){
        unifiedSendMethod(body, 4);

        //1.0  加入缓冲池
        addMsgToResendPool(body);
//        Logger.d("改变前body creator = " + body.getMessageCreator() + " receiver = " + body.getMessageReceiver());

        //2.0  加入数据库, 这里receiver 和 creator 更换
        SendMessageEnBody enBody = (SendMessageEnBody)body.clone();
//        Logger.d("改变前creator = " + body.getMessageCreator() + " receiver = " + body.getMessageReceiver());
        String msgReceiver = enBody.getMessageReceiver();
        enBody.setMessageReceiver(enBody.getMessageCreator());
        enBody.setMessageCreator(msgReceiver);
        CacheTool.getInstance().insertAImMsg(enBody);

//        Logger.d("改变后enBody creator = " + enBody.getMessageCreator() + " receiver = " + enBody.getMessageReceiver());
//        Logger.d("改变后body creator = " + body.getMessageCreator() + " receiver = " + body.getMessageReceiver());

        //3.0 刷新msglist表
        freshMsgList(enBody, false);
    }

    //告诉服务器IM消息已经接收成功
    public void imMessageIsSuccessReceived(SendToConfirmIMMsgIsGet body){
        unifiedSendMethod(body, 17);
    }
    // ---- 发送私信消息 】】】


    //【【【【 ------ 我撤回了一条消息
    //服务器是保存recalled消息推送状态的，必须告诉服务器消息已被接收，否则会不停给客户端推送
    public void sendRecalledMessage(SendRecalledMsgBody body){
        unifiedSendMethod(body, 18);
    }


    //告诉服务器recalled消息已经接收成功
    public void recalledMsgIsSuccessReceived(SendToConfirmRecalledMsgIsGet body){
        unifiedSendMethod(body, 22);
    }
    // ------ 我撤回了一条消息】】】】】


    //【【【  -------- 告诉服务器special消息已经接收成功
    //服务器是保存special消息推送状态的，必须告诉服务器消息已被接收，否则会不停给客户端推送
    public void specialMsgIsSuccessReceived(SendToConfirmSpecialMsgIsGet body){
        unifiedSendMethod(body, 23);
    }


    //重置推送数量为0，app外面那个小红点
    public void resetPushNumber(SendToResetPushNumber body){
        unifiedSendMethod(body, 21);
    }


    //告诉对方正在输入
    public void sendUserIsTyping(SendIsTypingBody body){
        unifiedSendMethod(body, 20);
    }




    //拉取离线消息(这里有个特殊设计，保障流畅性)
    /***
     *
     * 注  实际测试拉取离线消息的时候，个别时候会出现返回很慢的情况，原因不明。
     * 所以在iOS端做法是：
     * 1。第一次拉取离线消息
     * 2。间隔 3s 后再次执行拉取离线消息
     * 3。间隔 3 * 2 秒后 次执行拉取离线消息
     * 4。间隔 3 * 2 * 2 秒后 次执行拉取离线消息
     * 5。 .....
     * 直到间隔达到最大值，每隔96秒再执行一次
     *
     * ***/
    public void getOfflineMsg(final SendToGetOffLineMessage body){
        //1。0 发送请求离线消息
        unifiedSendMethod(body, 16);

        //2.0 初始化定时器
        this.dyOfflineMsgGapTimer = 3000;
        if (this.offLineTimer == null) {
            this.offLineTimer = new Timer();
        }
        //3。0 开始不定时执行
        getOfflineMsgInner(body);
    }

    private void  getOfflineMsgInner(final SendToGetOffLineMessage body){
        if (this.offLineTimer == null) {
            this.offLineTimer = new Timer();
        }

        this.offLineTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                offLineMsgMonitor(body);
            }
        },this.dyOfflineMsgGapTimer);

    }

    private void offLineMsgMonitor(final SendToGetOffLineMessage body){
        unifiedSendMethod(body, 16);
        this.dyOfflineMsgGapTimer = this.dyOfflineMsgGapTimer * 2;
        if (this.dyOfflineMsgGapTimer >= 96000){
            this.dyOfflineMsgGapTimer = 96000;
        }
        getOfflineMsgInner(body);
    }

    /****
     *
     * 私信消息自动重发
     *
     * ***/
    private void addMsgToResendPool(SendMessageEnBody body){
        if (this.resendPool == null){
            this.resendPool = new ArrayList<SendMessageEnBody>();
        }
        this.resendPool.add(body);
        startRendPool();
    }

    private void delMsgToResendPool(MessageSendState msgState){
        if (this.resendPool != null){

            for (int i = 0; i < this.resendPool.size(); i++){
                SendMessageEnBody bodyTemp = this.resendPool.get(i);
                if (bodyTemp.getMessageIdClient().equals(msgState.getMessageIdClientt())){
                    this.resendPool.remove(i);
                    break;
                }
            }
        }

    }

    private void startRendPool(){
        if (this.resendPoolTimer == null) {
            this.resendPoolTimer = new Timer();
            this.resendPoolTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    resendMethod();
                }
            }, 3000, 3000);
        }
    }

    private void resendMethod(){
        if (this.resendPool.size() <= 0){
            this.resendPoolTimer.cancel();
            this.resendPoolTimer = null;
        }else{
            //取出
            SendMessageEnBody bodyTemp = this.resendPool.get(0);
            int re_send_num = bodyTemp.get_resend_count();
            if (re_send_num >= 5) { //最多重发5次

                //1.0 删除重发池并回掉发送失败
                this.resendPool.remove(bodyTemp);

                //2.0 回掉发送失败
                MessageSendState msgState = new MessageSendState(bodyTemp.getMessageIdClient(),
                        bodyTemp.getMessageSendTime());
                callbackMessageSendFailed(msgState);
            }else{
                bodyTemp.set_resend_count(re_send_num + 1);
                this.resendPool.set(0, bodyTemp);
                unifiedSendMethod(bodyTemp, 4);
            }
        }

    }

    /**
     *
     * 一些工具方法
     * 内部使用
     *
     * private
     *
     */

    private Map<String,Object> objectToMap(Object obj) throws Exception {
        Gson gsonObjToMap = new Gson();
        String jsonString = gsonObjToMap.toJson(obj);

        Map<String,Object> map = new HashMap<String, Object>();
        map = gsonObjToMap.fromJson(jsonString, map.getClass());

        return map;
    }


    private int getIntWithString(String value){
        int result = 0;
//        Logger.d("getIntWithString = " + value);
        if (value == null || value.equals("null") ||  value.equals("false") || value.equals("")){
            return result;
        }
        if (value.equals("true")){
            return 1;
        }

        try{
            result = Float.valueOf(value).intValue();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 发送消息 给服务器
     *
     * 私有发送方法
     *
     **/

    private void unifiedSendMethod(Object body, int op){
        String key = RunningData.getInstance().getInnerAESKey();
        IMMessageBody userInfo = new IMMessageBody(body, op);
        Gson gson = new Gson();
        String originalString = gson.toJson(userInfo);
        try {
            byte[] encryptedData = EncryptUtil.encryptAndBase64Encode(originalString.getBytes(), key.getBytes(), key.getBytes(), "AES/CBC/PKCS5Padding");
            String en_content = new String(encryptedData); //加密后的字典字符串
            sendImMessage(en_content);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendImMessage(String en_content) {
        Gson gson = new Gson();
        IMMessageToServer jsonSend = new IMMessageToServer(en_content);
        String jsonSendString = gson.toJson(jsonSend);
        transMessageToServer(jsonSendString);
    }


    private void transMessageToServer(String text) {
        if (wsManager != null) {
            if (text != null && text != "") {
                wsManager.sendData(text);
            }
        }
    }



    /***
     *
     *     //回掉方法
     *
     *
     * ****/
    private void callbackChatDisconnected() {
        //默认是连接是断开的
        if (this.mesListCallbacks != null) {
            this.mesListCallbacks.chatDisconnected(); //连接断开回掉
        }
        if (this.mesDetailCallbacks != null) {
            this.mesDetailCallbacks.chatDisconnected(); //连接断开回掉
        }

        if (getWsState() == WsStatus.CONNECT_FAIL){
            wsManager.reconnect();
        }
    }

    private void callbackChatConnected() {
        if (this.mesListCallbacks != null) {
            this.mesListCallbacks.chatConnected(); //连接成功回掉
        }
        if (this.mesDetailCallbacks != null) {
            this.mesDetailCallbacks.chatConnected(); //连接成功回掉
        }
    }

    private void callbackMessageSendSuccess(MessageSendState msgState) {
        //删除重发池
        delMsgToResendPool(msgState);
        if (this.mesListCallbacks != null) {
            this.mesListCallbacks.messageSendSuccess(msgState);
        }
        if (this.mesDetailCallbacks != null) {
            this.mesDetailCallbacks.messageSendSuccess(msgState);
        }

        //1.0  修改数据库中字段
        CacheTool.getInstance().sendSuccess(RunningData.getInstance().getCurrentAccount(), msgState.getMessageIdClientt(), msgState.getMessageSendTime());

    }

    private void callbackMessageSendFailed(MessageSendState msgState) {
        //删除重发池
        delMsgToResendPool(msgState);
        if (this.mesListCallbacks != null) {
            this.mesListCallbacks.messageSendFailed(msgState);
        }
        if (this.mesDetailCallbacks != null) {
            this.mesDetailCallbacks.messageSendFailed(msgState);
        }

        //1.0  修改数据库中字段
        CacheTool.getInstance().sendFailed(RunningData.getInstance().getCurrentAccount(), msgState.getMessageIdClientt(), msgState.getMessageSendTime());

    }



    private void callbackNoOfflineMessage() {
        if (this.mesListCallbacks != null) {
            this.mesListCallbacks.noOfflineMessage();
        }
    }


    private void callbackUserIsTyping(ReceiverIsTypingBody typingBody){
        if (this.mesListCallbacks != null) {
            this.mesListCallbacks.userIsTyping(typingBody);
        }
        if (this.mesDetailCallbacks != null) {
            this.mesDetailCallbacks.userIsTyping(typingBody);
        }
    }

    private void callbackConnectSucTips(){
        if (this.mesListCallbacks != null) {
            this.mesListCallbacks.connectSucTips();
        }
        if (this.mesDetailCallbacks != null) {
            this.mesDetailCallbacks.connectSucTips();
        }
    }

    /////////////////////// 处理的分界线

    private void callbackUserRecalledAMessage(ReceiveRecalledInfo recalledInfo){
        if (this.mesListCallbacks != null) {
            this.mesListCallbacks.userRecalledAMessage(recalledInfo);
        }
        if (this.mesDetailCallbacks != null) {
            this.mesDetailCallbacks.userRecalledAMessage(recalledInfo);
        }

        //1。0 通知消息已经接收
        SendToConfirmRecalledMsgIsGet body = new SendToConfirmRecalledMsgIsGet(RunningData.getInstance().getCurrentAccount(), recalledInfo.getMessageIdClient());
        recalledMsgIsSuccessReceived(body);
    }

    private void callbackComeANewIMMessage(SendMessageEnBody enBody){
//        if (this.mesListCallbacks != null) {
//            this.mesListCallbacks.comeANewIMMessage(enBody);
//        }
        if (this.mesDetailCallbacks != null) {
            this.mesDetailCallbacks.comeANewIMMessage(enBody);
        }

        //1。0  通知消息已经接收
        SendToConfirmIMMsgIsGet body = new SendToConfirmIMMsgIsGet(RunningData.getInstance().getCurrentAccount(), enBody.getMessageIdClient());
        imMessageIsSuccessReceived(body);

        //2.0  在service里cache到来的数据
        CacheTool.getInstance().insertAImMsg(enBody);

        //3.0 刷新msglist表
        freshMsgList(enBody, true);
    }


    private void callbackApplyForFriend(ReceiveSpecialInfo speInfo) {
        if (this.mesListCallbacks != null) {
            this.mesListCallbacks.applyForFriend(speInfo);
        }
        //1。0 通知消息已经接收
        SendToConfirmSpecialMsgIsGet body = new SendToConfirmSpecialMsgIsGet(RunningData.getInstance().getCurrentAccount(), speInfo.getMessageIdClient());
        specialMsgIsSuccessReceived(body);
    }

    private void callbackAllowMeASFriend(ReceiveSpecialInfo speInfo) {
        if (this.mesListCallbacks != null) {
            this.mesListCallbacks.allowMeASFriend(speInfo);
        }
        //1。0 通知消息已经接收
        SendToConfirmSpecialMsgIsGet body = new SendToConfirmSpecialMsgIsGet(RunningData.getInstance().getCurrentAccount(), speInfo.getMessageIdClient());
        specialMsgIsSuccessReceived(body);
    }

    private void callbackSomeOneDeleteMe(ReceiveSpecialInfo speInfo) {
        if (this.mesListCallbacks != null) {
            this.mesListCallbacks.someOneDeleteMe(speInfo);
        }

        if (this.mesDetailCallbacks != null) {
            this.mesDetailCallbacks.someOneDeleteMe(speInfo);
        }

        //1。0 通知消息已经接收
        SendToConfirmSpecialMsgIsGet body = new SendToConfirmSpecialMsgIsGet(RunningData.getInstance().getCurrentAccount(), speInfo.getMessageIdClient());
        specialMsgIsSuccessReceived(body);
    }

    private CryTool tool = new CryTool();
    public void freshMsgList(SendMessageEnBody imbody, boolean showReadedTips){
        String body_en = imbody.getBody_en();
        String bodyString = "";
        if (imbody.getMessageTag() == false){ //自己的消息
            String key = RunningData.getInstance().getMyIMMessageAESKey();
            bodyString = tool.aesDeWith(body_en, key);
        }else{
            String key_en = imbody.getKey();
            String account = RunningData.getInstance().getCurrentAccount();
            String pri_key = RunningData.getInstance().getMyRSAPriKeyWith(account);
            String key = tool.rsaDecrypt(key_en, pri_key);
            bodyString = tool.aesDeWith(body_en, key);
        }
        if (!bodyString.equals("")){
            Gson gson = new Gson();
            SendMessageBody sendBody =  gson.fromJson(bodyString, SendMessageBody.class);
            if (sendBody != null){
                searchAndfresh(imbody.getMessageCreator(),
                        sendBody.getContent(),
                        sendBody.getMessageSendTime(),
                        showReadedTips);
                return;
            }
        }

        searchAndfresh(imbody.getMessageCreator(),
                "解密错误，具体查看帮助",
                imbody.getMessageSendTime(),
                showReadedTips);

    }

    private void searchAndfresh(String msgC, String content, String msgT, boolean showReadedTips){
        boolean isExist = false;
        int resultIndex = 0;
        ArrayList<MessageListModel> mListContact = RunningData.getInstance().getMsgList();
        for (int i = 0; i < mListContact.size(); i++){
            MessageListModel modelInner = mListContact.get(i);
            if (modelInner.account.equals(msgC)){  //当前列表有
                resultIndex = i;
                isExist = true;
                break;
            }
        }

        if (isExist == true) {
            MessageListModel modelInner = mListContact.get(resultIndex);
            String timeOlder = modelInner.MessageSendTime;
            if (getSendTime(msgT) >= getSendTime(timeOlder)){
                modelInner.Content = content;
                modelInner.MessageSendTime = msgT;
                modelInner.isreaded = showReadedTips;
                mListContact.remove(resultIndex);
                mListContact.add(0, modelInner);
            }
        }else{
            MessageListModel modelInner = new MessageListModel();
            modelInner.Content = content;
            modelInner.MessageSendTime = msgT;
            modelInner.account = msgC;

            boolean isContact = false;
            int contactIndex = 0;
            ArrayList<CNPinyin<ContactModel>> innerContact = RunningData.getInstance().getContactList();

            for (int j = 0; j < innerContact.size(); j++){
                ContactModel model = innerContact.get(j).data;
//                Logger.d("联系人 = " + model.account);
                if (model.account.equals(msgC)){
                    isContact = true;
                    contactIndex = j;
                    break;
                }
            }
            if (isContact == true){
                ContactModel model = innerContact.get(contactIndex).data;
                modelInner.public_key = model.public_key;
                modelInner.introduction = model.introduction;
                modelInner.label = model.label;
                modelInner.friend_id = model.friend_id;
                modelInner.avatar_url = model.avatar_url;
                modelInner.stranger = isContact;
            }else{
                modelInner.label = "新朋友";
                modelInner.stranger = isContact;
            }
            modelInner.isreaded = showReadedTips;
            mListContact.add(0, modelInner);
        }

        if (this.mesListCallbacks != null) {
            this.mesListCallbacks.freshMsgListView();
        }
    }

    private Long getSendTime(String time){
        Long idValue = Long.valueOf(0);
        try{
            idValue = Long.valueOf(time);
            return idValue;
        }catch (Exception e){
        }
        return idValue;
    }


}
