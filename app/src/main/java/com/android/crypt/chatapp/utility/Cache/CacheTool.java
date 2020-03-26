package com.android.crypt.chatapp.utility.Cache;

import com.android.crypt.chatapp.ChatAppApplication;
import com.android.crypt.chatapp.utility.Cache.CacheClass.CacheIMEnBody;
import com.android.crypt.chatapp.utility.Cache.CacheClass.ObjectCacheBody;
import com.android.crypt.chatapp.utility.Cache.CacheClass.ObjectCacheType;
import com.android.crypt.chatapp.utility.Cache.CacheManager.ObjectCacheManager;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.Crypt.CryTool;
import com.android.crypt.chatapp.utility.Websocket.Model.SendMessageBody;
import com.android.crypt.chatapp.utility.Websocket.Model.SendMessageEnBody;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.utility.Cache.CacheManager.IMMsgDBManager;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by White on 2019/3/24.
 *
 * ComCache 和 IMCache 直接与数据库操作的
 * 这个类统一处理集中缓存
 *
 */

public class CacheTool {
    private static CacheTool mInstance;
    public static CacheTool getInstance() {
        if (mInstance == null) {
            synchronized (CacheTool.class) {
                if (mInstance == null) {
                    mInstance = new CacheTool();
                }
            }
        }
        return mInstance;
    }

    /**
     *
     * 工具方法
     *
     * **/
    private  String stringToAscii(String value) {
        StringBuffer sbu = new StringBuffer();
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if ((int)chars[i] >= 48 && (int)chars[i] <=57){
                sbu.append((int)chars[i] - 48);
            }else if((int)chars[i] >= 97 && (int)chars[i] <= 122){
                sbu.append(((int)chars[i] - 97) % 9);
            }else if((int)chars[i] >= 65 && (int)chars[i] <= 90){
                sbu.append(((int)chars[i] - 65) % 9);
            }else{
                sbu.append(0);
            }
        }
        return sbu.toString();
    }

    /**
     *
     * ComCache包里的方法
     *
     *
     * **/
    public void cacheObject(ObjectCacheType type, String value){
        // 缓存的key就是Id，
        // 用户相关的缓存id = account + type 的字串相加转 long
        // 用户不相关的缓存id = 2019 + type 的字串相加转 long

        String account = RunningData.getInstance().getCurrentAccount();
        //cur 开头的类型不需要加 account
        if (type == ObjectCacheType.cur_account || type == ObjectCacheType.cur_token || type == ObjectCacheType.cur_pwd){
            account = "2019";
        }
        cacheObjectWithKey(type, value, account);
    }

    public void cacheObjectWithKey(ObjectCacheType type, String value, String account){
        String resultNumber = stringToAscii(account + String.valueOf(type.ordinal()));
        try {
            Long idValue = Long.valueOf(resultNumber);
//            Logger.d("操作"+ type.toString() +" 存入** id = " + idValue);
            if (idValue != 0){
                ObjectCacheBody body = new ObjectCacheBody(idValue, value);
                ObjectCacheManager dbManager = ChatAppApplication.getInstances().getObjectCacheManager();
                dbManager.insertData(body);
            }
        }catch (Exception e){
            Logger.d("存入失败");
        }
    }


    public String getCacheObject(ObjectCacheType type){
        String account = "2019";
        //cur 开头的类型不需要加 account
        if (type != ObjectCacheType.cur_account && type != ObjectCacheType.cur_token && type != ObjectCacheType.cur_pwd){
            account = RunningData.getInstance().getCurrentAccount();
        }
        return getCacheObjectWithKey(type, account);
    }

    public String getCacheObjectWithKey(ObjectCacheType type, String account){
        String resultNumber = stringToAscii(account + String.valueOf(type.ordinal()));
        try {
            Long idValue = Long.valueOf(resultNumber);
//            Logger.d("操作"+ type.toString() +" 取出** id = " + idValue);
            ObjectCacheManager dbManager = ChatAppApplication.getInstances().getObjectCacheManager();
            ObjectCacheBody data = dbManager.queryData(idValue);

            if (data != null) {
                String result = data.getValue();
                if (result == null){
                    return "";
                }else{
                    return result;
                }
            }else{
                return "";
            }
        }catch (Exception e){
            return "";
        }
    }


    public void saveAppKeys(String account, String pri_key_en){
        cacheObjectWithKey(ObjectCacheType.pri_key, pri_key_en, account);
    }

    public String getPri_key(String cacheAccount){
        return getCacheObjectWithKey(ObjectCacheType.pri_key, cacheAccount);
    }

    public void clearPri_key(String cacheAccount){
        cacheObject(ObjectCacheType.pri_key, "");
    }

//    public String getPub_key(String cacheAccount){
//        ComCacheDBManager dbManager = ChatAppApplication.getInstances().getComCacheManager();
//        CacheCommonData result =  dbManager.queryPrir_Key(cacheAccount, "pub_key");
//        if (result == null){
//            return null;
//        }else{
//            return result.getCacheData();
//        }
//    }



    /**
     *
     * IMCache包里的方法
     *
     *
     * **/
    public void insertAImMsg(SendMessageEnBody body){
        //调整缓存结构
        if(body.IsGroupMessage){ // 群聊
            if(body.getMessageTag() == true){ // 我收到的消息
                String groupId =  body.getMessageReceiver();
                String receive = RunningData.getInstance().getCurrentAccount();
                body.setMessageCreator(groupId);
                body.setMessageReceiver(receive);

            }
        }
        CacheIMEnBody cacheBody = new CacheIMEnBody();
        cacheBody.SetCacheImBody(null, body);
        IMMsgDBManager dbManager = ChatAppApplication.getInstances().getIMCacheManager();
        dbManager.insertData(cacheBody);
    }


    public ArrayList<SendMessageBody> queryAll(String creator, String receiver, int curPage, boolean isGroupMessage){
        IMMsgDBManager dbManager = ChatAppApplication.getInstances().getIMCacheManager();
        List<CacheIMEnBody> result =  dbManager.queryOneFriendMsg(creator, receiver,curPage);

        ArrayList<SendMessageBody> resultList = new ArrayList<>();
        if (result != null) {
            Gson gson = new Gson();
            for (int i = 0; i < result.size(); i++){
                CacheIMEnBody imbody = result.get(i);
                if (existAlready(imbody.getMessageIdClient(), resultList) == true){
                    continue;
                }

                SendMessageBody sendBody = freshGroupMessage(imbody.getBody_en(), imbody.getKey(), imbody.getMessageTag(), isGroupMessage, imbody.getMessageTag(), imbody.getIsSendSuccess(), imbody.getHas_send_error());
                if(sendBody != null){
                    sendBody.message_other_process_info = imbody.getMessage_other_process_info();
                    resultList.add(sendBody);
                }
//                else{
//                    sendBody = new SendMessageBody();
//                    sendBody.setMessageCreator(imbody.getMessageCreator());
//                    sendBody.setMessageReceiver(imbody.getMessageReceiver());
//                    sendBody.setMessageSendTime(imbody.getMessageSendTime());
//                    sendBody.setMessageIdClient(imbody.getMessageIdClient());
//                    sendBody.setMsgType(1);
//                    sendBody.setContent("解密错误，具体查看小秘密");
//                    sendBody.setIsSendSuccess(false);
//                    sendBody.setMessageTag(imbody.getMessageTag());
//                    sendBody.set_has_send_error(false);
//                }
//                resultList.add(sendBody);
//
//                CryTool tool  = new CryTool();
//                String bodyString = "";
//                if (imbody.getMessageTag() == false){ //自己的消息
//                    String key = RunningData.getInstance().getMyIMMessageAESKey();
//                    bodyString = tool.aesDeWith(body_en, key);
//                }else{
//
//                    String account = RunningData.getInstance().getCurrentAccount();
//                    String pri_key = RunningData.getInstance().getMyRSAPriKeyWith(account);
//                    String key = tool.rsaDecrypt(key_en, pri_key);
//                    bodyString = tool.aesDeWith(body_en, key);
//                }
//
//                if (!bodyString.equals("")){
//                    try{
//                        SendMessageBody sendBody =  gson.fromJson(bodyString, SendMessageBody.class);
//                        if (sendBody != null){
//                            sendBody.set_has_send_error(imbody.getHas_send_error());
//                            sendBody.setIsSendSuccess(imbody.getIsSendSuccess());
//                            sendBody.setMessageSendTime(imbody.getMessageSendTime());
//                            sendBody.setMessageTag(imbody.getMessageTag());
//                            sendBody.message_other_process_info = imbody.getMessage_other_process_info();
//
//                            resultList.add(sendBody);
//                            continue;
//                        }
//                    }catch (Exception e){
//                        Logger.d("bodyString 转换错误 = " + bodyString + " " + e);
//                    }
//                    continue;
//                }
//                SendMessageBody sendBody = new SendMessageBody();
//                sendBody.setMessageCreator(imbody.getMessageCreator());
//                sendBody.setMessageReceiver(imbody.getMessageReceiver());
//                sendBody.setMessageSendTime(imbody.getMessageSendTime());
//                sendBody.setMessageIdClient(imbody.getMessageIdClient());
//                sendBody.setMsgType(1);
//                sendBody.setContent("解密错误，具体查看帮助");
//                sendBody.setIsSendSuccess(false);
//                sendBody.setMessageTag(imbody.getMessageTag());
//                sendBody.set_has_send_error(false);
//                resultList.add(sendBody);
            }
        }

        return resultList;
    }

    public SendMessageBody freshGroupMessage(String body_en, String key_en, boolean mssageTag, boolean isGroupMessage, boolean messageTag, boolean sendSuccess, boolean haserror){
        CryTool tool = new CryTool();
        if(isGroupMessage == true){
            String bodyString = tool.aesDeWith(body_en, key_en);
            if (!bodyString.equals("")){
                Gson gson = new Gson();
                SendMessageBody sendBody =  gson.fromJson(bodyString, SendMessageBody.class);
                if (sendBody != null){
                    //sendBody.setIsSendSuccess(true);
                    // sendBody.set_has_send_error(false);
                    sendBody.set_has_send_error(haserror);
                    sendBody.setIsSendSuccess(sendSuccess);
                    sendBody.setMessageTag(messageTag);
                    return sendBody;
                }
            }
        }else{
            String bodyString = "";
            if (mssageTag == false){ //自己的消息
                String key = RunningData.getInstance().getMyIMMessageAESKey();
                bodyString = tool.aesDeWith(body_en, key);
            }else{
                String account = RunningData.getInstance().getCurrentAccount();
                String pri_key = RunningData.getInstance().getMyRSAPriKeyWith(account);
                String key = tool.rsaDecrypt(key_en, pri_key);
                bodyString = tool.aesDeWith(body_en, key);
            }
            if (!bodyString.equals("")) {
                Gson gson = new Gson();
                SendMessageBody sendBody = gson.fromJson(bodyString, SendMessageBody.class);
                if (sendBody != null) {
                    sendBody.set_has_send_error(haserror);
                    sendBody.setIsSendSuccess(sendSuccess);
                    sendBody.setMessageTag(mssageTag);
                    return sendBody;
                }
            }
        }

        return null;
    }



    //*** 去重
    private boolean existAlready(String bodyMsgId, ArrayList<SendMessageBody> mListContact) {
        boolean exist = false;
        if (mListContact.size()> 0) {
            for (int i = 0; i < mListContact.size(); i++) {
                SendMessageBody bodyInnder = mListContact.get(i);
                if (bodyInnder != null && bodyInnder.getMessageIdClient() != null) {
                    if (bodyInnder.getMessageIdClient().equals(bodyMsgId)) {
                        exist = true;
                        break;
                    }
                }
            }
        }
        return exist;
    }

    public void sendSuccess(String creator, String msgId, String time) {
        IMMsgDBManager dbManager = ChatAppApplication.getInstances().getIMCacheManager();
        dbManager.msgSendSuccess(creator, msgId, time);
    }

    public void uploadTranslate(String creator, String msgId, String resultValue) {

        IMMsgDBManager dbManager = ChatAppApplication.getInstances().getIMCacheManager();
        dbManager.uploadTranslateMethod(creator, msgId, resultValue);
    }

    public void sendFailed(String creator, String msgId, String time) {
        IMMsgDBManager dbManager = ChatAppApplication.getInstances().getIMCacheManager();
        dbManager.msgSendFailed(creator, msgId, time);
    }


    public void deleteFriendMsg(String creator){
        IMMsgDBManager dbManager = ChatAppApplication.getInstances().getIMCacheManager();
        dbManager.deleteAllData(creator);
    }

    public void deleteSecretMsg(String creator, ArrayList <String> msgId){
        IMMsgDBManager dbManager = ChatAppApplication.getInstances().getIMCacheManager();
        dbManager.deleteSecretData(creator, msgId);
    }

    public void deleteOneSpeMsg(String creator, String messageId){
        IMMsgDBManager dbManager = ChatAppApplication.getInstances().getIMCacheManager();
        dbManager.deleteSpecData(creator, messageId);
    }

}
