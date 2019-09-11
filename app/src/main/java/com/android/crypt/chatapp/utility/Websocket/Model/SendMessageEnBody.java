package com.android.crypt.chatapp.utility.Websocket.Model;

import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.Crypt.CryTool;
import com.google.gson.Gson;

import java.io.Serializable;

/**
 * Created by White on 2019/3/20.
 *
 * 加密后的消息体，传输给服务器
 *
 * 所有发送给websocket，需要存入数据库的值首字母都大写（配合golang和mongodb机制）
 *
 */

public class SendMessageEnBody implements Cloneable, Serializable {

     /*
    *
    * 开头大写的 服务器会处理
    *
    * */

    //这6个值与 SendMessageBody 对应的值相等，可以直接用SendMessageBody 进行赋值
    private String MessageCreator;   //消息创建者
    private String MessageReceiver;  //消息接受者
    private String MessageIdClient;  //消息的本地id
    private String MessageSendTime;  //消息发送时间，先取客户端时间，发送成功后服务器会返回服务器时间，替换为服务器时间
    private  boolean MessageTag;          //用于判断消息是我的还是别人的。自己发传0， 服务器来的消息都是 1
    private  boolean IsSendSuccess;       //消息发送成功，用于渲染UI

    ////////
    private String Body_en;          //消息加密体  就是 SendMessageBody 序列化加密字符串
    private String Key;              //消息的加密key，经过public_key加密

    /*
    *
    * 开头小写的 用作本地处理
    *
    *
    * */

    //是否发送失败，iOS端当重发次数 > 5 时，认为发送失败
    //0 没有错误，1发送错误
    public  boolean has_send_error;
    //重发次数，用于重发池
    public  int resend_count;


    //***最简便的初始化方法
    public SendMessageEnBody(SendMessageBody body, String pub_key){
        this.MessageCreator  = body.getMessageCreator();
        this.MessageReceiver  = body.getMessageReceiver();
        this.MessageIdClient  = body.getMessageIdClient();
        this.MessageSendTime  = body.getMessageSendTime();
        this.MessageTag      = body.getMessageTag();
        this.IsSendSuccess   = body.getIsSendSuccess();

        this.has_send_error = false;
        this.resend_count = 0;

        //加密方法
        //1.  获取加密aes密钥
        String myImKey = RunningData.getInstance().getMyIMMessageAESKey();

        //2.  初始化工具类
        CryTool tool = new CryTool();
        Gson gson = new Gson();
        String originalString = gson.toJson(body);

        //3. myImKey使用aes加密消息体originalString
        String im_Body_en = tool.aesEnWith(originalString, myImKey);
        //4. pub_key使用rsa加密上面的密钥myImKey
        String im_key = tool.rsaEncrypt(myImKey, pub_key);

        //5. 传递密文和加密后的密钥
        this.Body_en  = im_Body_en;
        this.Key  = im_key;

    }



    public SendMessageEnBody(String MessageCreator,
                             String MessageReceiver,
                             String MessageIdClient,
                             String MessageSendTime,
                             String Body_en,
                             String Key,
                             boolean MessageTag,
                             boolean IsSendSuccess){

        this.MessageCreator  = MessageCreator;
        this.MessageReceiver  = MessageReceiver;
        this.MessageIdClient  = MessageIdClient;
        this.MessageSendTime  = MessageSendTime;
        this.Body_en  = Body_en;
        this.Key  = Key;
        this.MessageTag  = MessageTag;
        this.IsSendSuccess  = IsSendSuccess;
        this.has_send_error = false;
        this.resend_count = 0;
    }


    //消息 创建者
    public String getMessageCreator(){
        return this.MessageCreator;
    }

    public void setMessageCreator(String MessageCreator){
        this.MessageCreator  = MessageCreator;
    }

    //消息 接受者
    public String getMessageReceiver(){
        return this.MessageReceiver;
    }

    public void setMessageReceiver(String MessageReceiver){
        this.MessageReceiver  = MessageReceiver;
    }

    //消息 id
    public String getMessageIdClient(){
        return this.MessageIdClient;
    }

    public void setMessageIdClient(String MessageIdClient){
        this.MessageIdClient  = MessageIdClient;
    }

    //消息 time
    public String getMessageSendTime(){
        return this.MessageSendTime;
    }

    public void setMessageSendTime(String MessageSendTime){
        this.MessageSendTime  = MessageSendTime;
    }

    //消息 加密体
    public String getBody_en(){
        return this.Body_en;
    }

    public void setBody_en(String Body_en){
        this.Body_en  = Body_en;
    }

    //消息 加密体key
    public String getKey(){
        return this.Key;
    }

    public void setKey(String Key){
        this.Key  = Key;
    }

    //消息 MessageTag
    public boolean getMessageTag(){
        return this.MessageTag;
    }

    public void setMessageTag(boolean MessageTag){
        this.MessageTag  = MessageTag;
    }

    //消息 发送成功？
    public boolean getIsSendSuccess(){
        return this.IsSendSuccess;
    }

    public void setIsSendSuccess(boolean IsSendSuccess){
        this.IsSendSuccess  = IsSendSuccess;
    }

    //消息 发送失败
    public boolean get_has_send_error(){
        return this.has_send_error;
    }

    public void set_has_send_error(boolean has_send_error){
        this.has_send_error  = has_send_error;
    }

    //消息 重发次数
    public int get_resend_count(){
        return this.resend_count;
    }

    public void set_resend_count(int resend_count){
        this.resend_count  = resend_count;
    }


    @Override
    public Object clone() {
        try{
            return  super.clone();
        }catch(CloneNotSupportedException e){
            return null;
        }
    }



}
