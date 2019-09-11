package com.android.crypt.chatapp.utility.Common;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import com.android.crypt.chatapp.msgList.model.MessageListModel;
import com.android.crypt.chatapp.utility.Cache.CacheClass.ObjectCacheType;
import com.android.crypt.chatapp.utility.Cache.CacheTool;
import com.android.crypt.chatapp.utility.Crypt.CryTool;
import com.android.crypt.chatapp.utility.okgo.model.CodeResponse;
import com.android.crypt.chatapp.utility.okgo.utils.Convert;
import com.chatapp.push.util.PushRunningData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.Response;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.ChatAppApplication;
import com.android.crypt.chatapp.InfoSetting.Config.ConfigDic;
import com.android.crypt.chatapp.contact.Model.ContactCacheModel;
import com.android.crypt.chatapp.contact.Model.ContactModel;
import com.android.crypt.chatapp.contact.cn.CNPinyin;
import com.android.crypt.chatapp.utility.okgo.callback.JsonCallback;

import org.json.JSONObject;
import java.util.ArrayList;


/**
 * Created by White on 2019/3/20.
 *
 * 存储一些app运行时数据，单例的类
 *
 */

public class RunningData {
    public int keyKind = 0;
    private String innerAESKey = null;
    private String iMMessageAESKey = null;
    private String myPrivateKey = null;

    private String server_url = null;
    private String echoMainPicUrl = null;
    private String echoIMPicUrl = null;
    private String echoEmojiPicUrl = null;
    private String cur_token = null;
    private String cur_account = null;
    private String cur_pwd = null;
    private String curPushVoice = null;

    private int curTextSize = 0;
    private boolean isDebug;

    private static RunningData mInstance;
    public static RunningData getInstance() {
        if (mInstance == null) {
            synchronized (RunningData.class) {
                if (mInstance == null) {
                    mInstance = new RunningData();
                }
            }
        }
        return mInstance;
    }

    /***
     *
     * App 一些配置
     *
     * ***/

    //最初的加密key
    // ws 测试url
    //118.24.158.102:8089
    public String wsTestUrl(){
        return "ws://118.24.158.102:8089/sub";
    }

    // ws 正式url
    public String wsReleaseUrl(){ return "ws://118.24.158.102:8089/sub"; }

    public int heartBeatDelay(){
        return 30000;
    }

    public int heartBeatGap(){
        return 90000;
    }

    public int heartBeatTimeOut(){
        return 10000;
    }


    public String server_url(){
        if (server_url == null || server_url.equals("")){
            server_url = PropertiesFactoryHelper.getInstance().getConfig("server.url");
        }
        return server_url;
    }

    public String echoMainPicUrl(){
        if (echoMainPicUrl == null || echoMainPicUrl.equals("")){
            echoMainPicUrl = PropertiesFactoryHelper.getInstance().getConfig("qiniu.echoMainPicUrl");
        }
        return echoMainPicUrl;
    }

    public String echoIMPicUrl(){
        if (echoIMPicUrl == null || echoIMPicUrl.equals("") ){
            echoIMPicUrl = PropertiesFactoryHelper.getInstance().getConfig("qiniu.echoIMPicUrl");
        }
        return echoIMPicUrl;
    }

    public String echoEmojiPicUrl(){
        if (echoEmojiPicUrl == null || echoEmojiPicUrl.equals("") ){
            echoEmojiPicUrl = PropertiesFactoryHelper.getInstance().getConfig("qiniu.echoEmojiPicUrl");
        }
        return echoEmojiPicUrl;
    }




    /***
     *
     * 各种密钥的读取
     *
     * ***/

    //这个是app固定密钥，在私钥计算之前，一些加密都用这个
    //用处：
    //1。在与ws服务器交互的时候，传输数据最外层加密用它。JosnBody = "XXXX"  这一段
    //2。iOS端用它加密保存了用户登录信息、
    //3。加密了用户私钥，实际用户看见的是加密后的二维码。
    //4。加密了个人名片二维码

    private String Boring(){
        CryTool tool = new CryTool();
        byte[] bb = {116, 104, 101, 97, 112, 112, 119, 105, 108, 108, 104, 111, 116, 49, 50, 51};
        String b = new String(bb);
        byte[] cb = {76, 55, 107, 49, 116, 75, 108, 76, 111, 78, 83, 43, 105, 106, 72, 51, 70, 99, 117, 83, 107, 111, 109, 87, 48, 86, 43, 66, 73, 48, 68, 65, 80, 111, 54, 67, 97, 83, 113, 113, 81, 43, 69, 61};
        String c = new String(cb);

//        Logger.d("aesDe 1");
        return tool.aesDeWith(c, b);
    }


    public String getInnerAESKey(){
        //值 = e1ngra2m#w8Ss2&3
        if (this.innerAESKey == null || this.innerAESKey.equals("")){
            this.innerAESKey = Boring();
        }
        return this.innerAESKey;
    }

    //与Ws服务器私信的的body，用这个key，进行AES加密
    //用户计算的私钥后经过sha512 计算截取而成
    //当保存私钥的时候，它自动计算并与私钥一起保存
    public String getMyIMMessageAESKey(){
        if (this.iMMessageAESKey == null || this.iMMessageAESKey.equals("")){
            String account = getCurrentAccount();
            String myPri_key = getMyRSAPriKeyWith(account);

            CryTool tool = new CryTool();
            this.iMMessageAESKey = tool.getMyIMMessageAESKey(myPri_key);
        }

        return this.iMMessageAESKey;
    }

    public void setMyIMMessageAESKey(String myPri_key){
        CryTool tool = new CryTool();
        this.iMMessageAESKey = tool.getMyIMMessageAESKey(myPri_key);

    }

    //根据账号获取用户计算的的私钥
    public String getMyRSAPriKeyWith(String account){
        if (this.myPrivateKey == null || this.myPrivateKey.equals("")){
            String key_en = CacheTool.getInstance().getPri_key(getCurrentAccount());
            if (!key_en.equals("")){
                CryTool tool = new CryTool();
                this.myPrivateKey = tool.aesDeWith(key_en, getInnerAESKey());
            }else{
                this.myPrivateKey = "";
            }
        }
        return this.myPrivateKey;
    }

    public String getMyPrikeyEnWith(){
        return CacheTool.getInstance().getPri_key(getCurrentAccount());
    }

    public String getCurrentAccount(){
        if (cur_account == null || cur_account.equals("")){
            cur_account = CacheTool.getInstance().getCacheObject(ObjectCacheType.cur_account);
        }
        return cur_account;
    }

    public String getCurrentPwd(){
        if (cur_pwd == null || cur_pwd.equals("") ){
            String result = CacheTool.getInstance().getCacheObject(ObjectCacheType.cur_pwd);
            CryTool tool = new CryTool();
            cur_pwd = tool.aesDeWith(result, getInnerAESKey());
        }
        return cur_pwd;
    }

    public String getToken(){
        if (cur_token == null || cur_token.equals("")){
            cur_token = CacheTool.getInstance().getCacheObject(ObjectCacheType.cur_token);
        }
        return cur_token;
    }

    //****好友列表
    private ArrayList<CNPinyin<ContactModel>> mListContact = null;
    public void initContactList(){
        if (mListContact == null){
            mListContact = new ArrayList<>();
            getCacheFriendList();
        }
    }
    public  ArrayList<CNPinyin<ContactModel>> getContactList(){
        if (mListContact == null){
            mListContact = new ArrayList<>();
            getCacheFriendList();
        }
        return mListContact;
    }

    private void getCacheFriendList() {
        try {
            String friendListString =  CacheTool.getInstance().getCacheObject(ObjectCacheType.friend_list);
            Logger.d("friendListString = " + friendListString);
            if (friendListString != null){
                Gson gson = new Gson();
                ArrayList<ContactCacheModel> cacheModelsList = gson.fromJson(friendListString, new TypeToken<ArrayList<ContactCacheModel>>() {
                }.getType());
                if (cacheModelsList != null && cacheModelsList.size() > 0){
                    for (int i = 0; i < cacheModelsList.size(); i++){
                        ContactCacheModel cacheModel = cacheModelsList.get(i);
                        ContactModel data = new ContactModel(cacheModel.avatar_url, cacheModel.username, cacheModel.label, cacheModel.account, cacheModel.introduction, cacheModel.public_key, cacheModel.friend_id);
                        CNPinyin<ContactModel> pinyin = new CNPinyin(data);

                        pinyin.firstChar = cacheModel.firstChar;
                        pinyin.firstChars = cacheModel.firstChars;
                        pinyin.pinyinsTotalLength = cacheModel.pinyinsTotalLength;
                        pinyin.pinyins = cacheModel.pinyins.split("_");
                        mListContact.add(pinyin);
                    }
                    Logger.d("mListContact = " + gson.toJson(mListContact));
                }
            }

        }catch (Exception e){
            Logger.d("friendListString 转换错误 = " + e);
            e.printStackTrace();
        }
    }

    public int getCurTextSize(){
        if (this.curTextSize < 15){
            this.curTextSize = 15;
            Gson gson = new Gson();
            String userInfoString = CacheTool.getInstance().getCacheObject(ObjectCacheType.config_dic);
            if (userInfoString != "") {
                try {
                    ConfigDic config = gson.fromJson(userInfoString, ConfigDic.class);
                    if (config != null){
                        curTextSize = config.msgTextSize;
                    }
                }catch (Exception e){
                    Logger.d("getCurTextSize 错误");
                }
            }
        }
        return this.curTextSize;
    }

    public void setCurTextSize(int size){
        this.curTextSize = size;
    }

    //****消息列表
    private ArrayList<MessageListModel> msgList = null;
    public ArrayList<MessageListModel> getMsgList(){
        if (msgList == null){
            msgList = new ArrayList<>();
            Gson gson = new Gson();
            String cacheString = CacheTool.getInstance().getCacheObject(ObjectCacheType.message_list);
            ArrayList<MessageListModel> cacheList = gson.fromJson(cacheString, new TypeToken<ArrayList<MessageListModel>>() {
            }.getType());
            if (cacheList != null && cacheList.size() > 0){
                msgList.addAll(cacheList);
            }
        }
        return msgList;
    }


    //***重新登录
    public void reLogInMethod(){
        String  accountStr = getCurrentAccount();
        String pwdStr = getCurrentPwd();
        JSONObject deviceInfo = new JSONObject();
        try {
            CryTool tool = new CryTool();
            String accountSha256 = tool.shaAccount(accountStr);
            if (accountSha256.length() > 40){
                accountSha256 = accountSha256.substring(0, 40);
            }

            deviceInfo.put("uniqueId", ServiceUtils.getUniqueID());
            deviceInfo.put("deviceToken", accountSha256);
            deviceInfo.put("osType", "Android");
            deviceInfo.put("osVersion", Build.VERSION.RELEASE);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String baseurl = RunningData.getInstance().server_url();

        OkGo.<CodeResponse>post(baseurl + "system/userLogin")
                .tag(this)
                .cacheMode(CacheMode.NO_CACHE)
                .params("account", accountStr)
                .params("password", pwdStr)
                .params("deviceInfo", deviceInfo.toString())
                .execute(new JsonCallback<CodeResponse>() {
                    @Override
                    public void onFinish() {
                    }
                    @Override
                    public void onSuccess(Response<CodeResponse> response) {
                        if (response.body().code == 1) {
                            try {
                                JSONObject data = Convert.formatToJson(response.body().data);
                                //更换token
                                String token = data.getString("token");
                                CacheTool.getInstance().cacheObject(ObjectCacheType.cur_token, token);
                                cur_token = token;

                                //***缓存user info
                                String userInfoString = data.getJSONObject("userInfo").toString();
                                CacheTool.getInstance().cacheObject(ObjectCacheType.user_info, userInfoString);
                            } catch (Exception ex) {

                            }
                        }else{

                        }
                    }

                    @Override
                    public void onError(Response<CodeResponse> response) {
                        response.getException().printStackTrace();
                    }
                });
    }



    public int getScreenWidth(){
        Context context = ChatAppApplication.getContext();
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return  dm.widthPixels;
    }

    public int getScreenHeight(){
        Context context = ChatAppApplication.getContext();
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return  dm.heightPixels;
    }

    public void setIsApkInDebug() {
        try {
            Context context = ChatAppApplication.getContext();
            ApplicationInfo info = context.getApplicationInfo();
            this.isDebug = (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            this.isDebug  = false;
        }
    }

    public boolean IsApkInDebug(){
        return this.isDebug;
    }


    public String getBgImageUrl(){
        String mFile = ChatAppApplication.getContext().getFilesDir()+ "/"+"bg_image/";
        return mFile;
    }

    public String getEncodeImageUrl(){
        String mFile = ChatAppApplication.getContext().getFilesDir()+ "/"+"encode_image/";
        return mFile;
    }

    public String getVoiceUrl(){
        String mFile = ChatAppApplication.getContext().getFilesDir()+ "/"+"voice/";
        return mFile;
    }

    public String getCollectImage(){
        String mFile = ChatAppApplication.getContext().getFilesDir()+ "/"+"collect_image/";
        return mFile;
    }

    public String getCurPushVoice(){
        if (this.curPushVoice == null){
            this.curPushVoice = "cat.mp3";
            Gson gson = new Gson();
            String configDicString = CacheTool.getInstance().getCacheObject(ObjectCacheType.config_dic);
            if (configDicString != "") {
                try {
                    ConfigDic config = gson.fromJson(configDicString, ConfigDic.class);
                    if (config.voiceSetting != null){
                        this.curPushVoice = config.voiceSetting;
                    }
                } catch (Exception e) {}
            }
        }
        return this.curPushVoice;
    }

    public void setCurPushVoice(String value){
        this.curPushVoice = value;
    }

}
