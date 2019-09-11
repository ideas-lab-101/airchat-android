package com.android.crypt.chatapp.utility.Websocket.Model;

import java.io.Serializable;

/**
 * Created by White on 2019/3/20.
 *
 * 所有发送给websocket，需要存入数据库的值首字母都大写（配合golang和mongodb机制）
 *
 * ///////
 * body 的字段，整体加密的：
 *
 * Content         string  //@require 客户端必传
 * MessageCreator  string  //@require 客户端必传
 * MessageReceiver string  //@require 客户端必传
 * MessageIdClient string  //@require 客户端必传
 * MessageIdServer string  //@forbid 客户端不传
 * MsgType         int64   //@require 客户端必传
 * FileUrl         string  //@optional 客户端可传（如果有）
 * ExcessInfo    string     //@optional 客户端可传（如果有）  **** 有特定的格式
 * MessageSendTime string  //@require 客户端必传
 * IsSendSuccess   bool    //@ optional 客户端不传或传NO
 * MessageTag      bool    //@ optional 客户端不传或传NO
 * MessageSecretType int64  //0默认，1 阅后即焚
 *
 */

public class SendMessageBody implements Serializable {
    private String Content;
    private String MessageCreator;
    private String MessageReceiver;
    private String MessageIdClient;
    private String MessageIdServer;
    private int MsgType;
    private String FileUrl;
    private String ExcessInfo;
    private String MessageSendTime;
    private boolean IsSendSuccess;
    private boolean MessageTag;
    private int MessageSecretType;

    public  boolean has_send_error = false;
    public  String image_value = "";
    public String message_other_process_info = ""; //其他的消息，比如语音转文字的结果

    public void setContent(String Content){
        this.Content = Content;
    }
    public String getContent(){
        return this.Content;
    }
    public void setMessageCreator(String MessageCreator){
        this.MessageCreator = MessageCreator;
    }
    public String getMessageCreator(){
        return this.MessageCreator;
    }


    public void setMessageReceiver(String MessageReceiver){
        this.MessageReceiver = MessageReceiver;
    }

    public String getMessageReceiver(){
        return this.MessageReceiver;
    }

    public void setMessageIdClient(String MessageIdClient){
        this.MessageIdClient = MessageIdClient;
    }

    public String getMessageIdClient(){
        return this.MessageIdClient;
    }

    public void setMessageIdServer(String MessageIdServer){
        this.MessageIdServer = MessageIdServer;
    }

    public String getMessageIdServer(){
        return this.MessageIdServer;
    }

    public void setFileUrl(String FileUrl){
        this.FileUrl = FileUrl;
    }

    public String getFileUrl(){
        return this.FileUrl;
    }

    public void setExcessInfo(String ExcessInfo){
        this.ExcessInfo = ExcessInfo;
    }

    public String getExcessInfo(){
        return this.ExcessInfo;
    }

    public void setMessageSendTime(String MessageSendTime){
        this.MessageSendTime = MessageSendTime;
    }

    public String getMessageSendTime(){
        return this.MessageSendTime;
    }

    public void setMsgType(int MsgType){
        this.MsgType = MsgType;
    }

    public int getMsgType(){
        return this.MsgType;
    }

    public void setIsSendSuccess(boolean IsSendSuccess){
        this.IsSendSuccess = IsSendSuccess;
    }

    public boolean getIsSendSuccess(){
        return this.IsSendSuccess;
    }


    public void setMessageTag(boolean MessageTag){
        this.MessageTag = MessageTag;
    }

    public boolean getMessageTag(){
        return this.MessageTag;
    }

    public void setMessageSecretType(int MessageSecretType){
        this.MessageSecretType = MessageSecretType;
    }

    public int getMessageSecretType(){
        return this.MessageSecretType;
    }

    public boolean get_has_send_error(){
        return this.has_send_error;
    }

    public void set_has_send_error(boolean has_send_error){
        this.has_send_error  = has_send_error;
    }


}
