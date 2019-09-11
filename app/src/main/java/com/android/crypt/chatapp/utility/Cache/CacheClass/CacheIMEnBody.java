package com.android.crypt.chatapp.utility.Cache.CacheClass;

import com.android.crypt.chatapp.utility.Websocket.Model.SendMessageEnBody;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by White on 2019/3/24.
 *
 * 私信的 body_en 持久化
 *
 */

@Entity
public class CacheIMEnBody {
    @Id(autoincrement = true)
    private Long id;

    //这6个值与 SendMessageEnBody 对应的值相等，可以直接用 SendMessageEnBody 进行赋值
    private String MessageCreator;   //消息创建者  存在本地数据库是，均是对方
    private String MessageReceiver;  //消息接受者  存在本地数据库是，均是我
    private String MessageIdClient;  //消息的本地id
    private String MessageSendTime;  //消息发送时间，先取客户端时间，发送成功后服务器会返回服务器时间，替换为服务器时间
    private  boolean MessageTag;          //用于判断消息是我的还是别人的。自己发传0， 服务器来的消息都是 1
    private  boolean IsSendSuccess;       //消息发送成功，用于渲染UI
    private String Body_en;          //消息加密体  就是 SendMessageBody 序列化加密字符串
    private String Key;              //消息的加密key，经过public_key加密


    public  boolean has_send_error;
    //重发次数，用于重发池
    public  int resend_count;

    public String message_other_process_info; //其他的消息，比如语音转文字的结果


    @Generated(hash = 1912321916)
    public CacheIMEnBody(Long id, String MessageCreator, String MessageReceiver, String MessageIdClient,
            String MessageSendTime, boolean MessageTag, boolean IsSendSuccess, String Body_en, String Key,
            boolean has_send_error, int resend_count, String message_other_process_info) {
        this.id = id;
        this.MessageCreator = MessageCreator;
        this.MessageReceiver = MessageReceiver;
        this.MessageIdClient = MessageIdClient;
        this.MessageSendTime = MessageSendTime;
        this.MessageTag = MessageTag;
        this.IsSendSuccess = IsSendSuccess;
        this.Body_en = Body_en;
        this.Key = Key;
        this.has_send_error = has_send_error;
        this.resend_count = resend_count;
        this.message_other_process_info = message_other_process_info;
    }

    @Generated(hash = 2066526861)
    public CacheIMEnBody() {
    }

    public void SetCacheImBody(Long id, SendMessageEnBody body){
        this.id = id;
        this.MessageCreator = body.getMessageCreator();
        this.MessageReceiver = body.getMessageReceiver();
        this.MessageIdClient = body.getMessageIdClient();
        this.MessageSendTime = body.getMessageSendTime();
        this.MessageTag = body.getMessageTag();
        this.IsSendSuccess =body.getIsSendSuccess() ;
        this.Body_en = body.getBody_en();
        this.Key = body.getKey();
        this.has_send_error = body.get_has_send_error();
        this.resend_count = body.get_resend_count();
        this.message_other_process_info = "";
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessageCreator() {
        return this.MessageCreator;
    }

    public void setMessageCreator(String MessageCreator) {
        this.MessageCreator = MessageCreator;
    }

    public String getMessageReceiver() {
        return this.MessageReceiver;
    }

    public void setMessageReceiver(String MessageReceiver) {
        this.MessageReceiver = MessageReceiver;
    }

    public String getMessageIdClient() {
        return this.MessageIdClient;
    }

    public void setMessageIdClient(String MessageIdClient) {
        this.MessageIdClient = MessageIdClient;
    }

    public String getMessageSendTime() {
        return this.MessageSendTime;
    }

    public void setMessageSendTime(String MessageSendTime) {
        this.MessageSendTime = MessageSendTime;
    }

    public boolean getMessageTag() {
        return this.MessageTag;
    }

    public void setMessageTag(boolean MessageTag) {
        this.MessageTag = MessageTag;
    }

    public boolean getIsSendSuccess() {
        return this.IsSendSuccess;
    }

    public void setIsSendSuccess(boolean IsSendSuccess) {
        this.IsSendSuccess = IsSendSuccess;
    }

    public String getBody_en() {
        return this.Body_en;
    }

    public void setBody_en(String Body_en) {
        this.Body_en = Body_en;
    }

    public String getKey() {
        return this.Key;
    }

    public void setKey(String Key) {
        this.Key = Key;
    }

    public boolean getHas_send_error() {
        return this.has_send_error;
    }

    public void setHas_send_error(boolean has_send_error) {
        this.has_send_error = has_send_error;
    }

    public int getResend_count() {
        return this.resend_count;
    }

    public void setResend_count(int resend_count) {
        this.resend_count = resend_count;
    }

    public String getMessage_other_process_info() {
        return this.message_other_process_info;
    }

    public void setMessage_other_process_info(String message_other_process_info) {
        this.message_other_process_info = message_other_process_info;
    }


  

}
