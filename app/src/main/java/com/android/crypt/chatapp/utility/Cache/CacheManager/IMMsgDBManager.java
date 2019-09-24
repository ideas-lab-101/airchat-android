package com.android.crypt.chatapp.utility.Cache.CacheManager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.android.crypt.chatapp.utility.Cache.CacheClass.CacheIMEnBody;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import com.android.crypt.chatapp.utility.Cache.CacheClass.CacheIMEnBodyDao;
import com.android.crypt.chatapp.utility.Cache.CacheClass.DaoMaster;
import com.android.crypt.chatapp.utility.Cache.CacheClass.DaoSession;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by White on 2019/3/24.
 *
 *  聊天的私信缓存
 *
 *  采用Green数据库
 *
 *  不直接调用，封装在CacheTool统一调用
 *
 */

public class IMMsgDBManager {

    private final static String dbName = "chap_app_im_message";
    private static IMMsgDBManager mInstance;
    private DaoMaster.DevOpenHelper openHelper;
    private Context context;

    public IMMsgDBManager(Context context) {
        this.context = context;
//        openHelper = new DaoMaster.DevOpenHelper(context, dbName, null);
    }

    /**
     * 获取单例引用
     *
     * @param context
     * @return
     */
    public static IMMsgDBManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (IMMsgDBManager.class) {
                if (mInstance == null) {
                    mInstance = new IMMsgDBManager(context);
                }
            }
        }
        return mInstance;
    }

    /**
     * 获取可读数据库
     */
    private SQLiteDatabase getReadableDatabase() {
        if (openHelper == null) {
            openHelper = new DaoMaster.DevOpenHelper(context, dbName, null);
        }
        SQLiteDatabase db = openHelper.getReadableDatabase();
        return db;
    }

    /**
     * 获取可写数据库
     */
    private SQLiteDatabase getWritableDatabase() {
        if (openHelper == null) {
            openHelper = new DaoMaster.DevOpenHelper(context, dbName, null);
        }
        SQLiteDatabase db = openHelper.getWritableDatabase();
        return db;
    }

    /**
     * 插入一条记录
     *
     */
    public void insertData(CacheIMEnBody data) {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();

        CacheIMEnBodyDao cacheDao = daoSession.getCacheIMEnBodyDao();
        cacheDao.insert(data);
    }

    /**
     * 更新一条记录
     *
     * 更新发送状态IsSendSuccess，每次重发需要更新resend_count，
     *
     * ATTENTION 缓存的时候，对方一律是MessageCreator，我是MessageReceiver
     *
     */

    //no such column: 1 (code 1 SQLITE_ERROR):
// UPDATE CACHE_IMEN_BODY
// SET IsSendSuccess = `1`,
// has_send_error = `0`,
// MessageSendTime = `1556782571800` WHERE
// MessageReceiver = `18382319503` AND
// MessageIdClient = `c18d1d0950ad1909ef5c5cadbf464c2fdbcabcc4304aa74e8c3f3c16587dfd1151ae10dbea276484a1c232b2ec8cc1721d8a00d2c6e809e656c8d1719c063705`


////
/// {
/// "Body_en":"",
/// "IsSendSuccess":false,
/// "Key":"",
/// "MessageCreator":"18382319502",
/// "MessageIdClient":"c18d1d0950ad1909ef5c5cadbf464c2fdbcabcc4304aa74e8c3f3c16587dfd1151ae10dbea276484a1c232b2ec8cc1721d8a00d2c6e809e656c8d1719c063705",
/// "MessageReceiver":"18382319503",
/// "MessageSendTime":"1556782586712",
/// "MessageTag":false,
/// "has_send_error":0,
/// "id":18,
/// "message_other_process_info":"",
/// "resend_count":0}


    public void msgSendSuccess(String creator, String msgId, String time) {

        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        CacheIMEnBodyDao cacheDao = daoSession.getCacheIMEnBodyDao();
        List<CacheIMEnBody> list  = cacheDao.queryBuilder()
                .where(CacheIMEnBodyDao.Properties.MessageReceiver.eq(creator), CacheIMEnBodyDao.Properties.MessageIdClient.eq(msgId))
                .orderDesc(CacheIMEnBodyDao.Properties.Id)
                .list();

        CacheIMEnBody bodyNew = list.get(0);
        bodyNew.setMessageSendTime(time);
        bodyNew.setIsSendSuccess(true);
        bodyNew.setHas_send_error(false);

        cacheDao.update(bodyNew);
    }

    public void uploadTranslateMethod(String creator, String msgId, String result) {

        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        CacheIMEnBodyDao cacheDao = daoSession.getCacheIMEnBodyDao();
        List<CacheIMEnBody> list  = cacheDao.queryBuilder()
                .where(CacheIMEnBodyDao.Properties.MessageReceiver.eq(creator), CacheIMEnBodyDao.Properties.MessageIdClient.eq(msgId))
                .orderDesc(CacheIMEnBodyDao.Properties.Id)
                .list();

        CacheIMEnBody bodyNew = list.get(0);
        bodyNew.setMessage_other_process_info(result);
        Gson gson = new Gson();
        cacheDao.update(bodyNew);
    }

    public void msgSendFailed(String creator, String msgId, String time) {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        CacheIMEnBodyDao cacheDao = daoSession.getCacheIMEnBodyDao();
        List<CacheIMEnBody> list  = cacheDao.queryBuilder()
                .where(CacheIMEnBodyDao.Properties.MessageReceiver.eq(creator), CacheIMEnBodyDao.Properties.MessageIdClient.eq(msgId))
                .orderDesc(CacheIMEnBodyDao.Properties.Id)
                .list();

        CacheIMEnBody bodyNew = list.get(0);
        bodyNew.setMessageSendTime(time);
        bodyNew.setIsSendSuccess(false);
        bodyNew.setHas_send_error(true);

        cacheDao.update(bodyNew);
    }

    /**
     *
     * 分页查询某个用户的聊天数据数据
     * 测试方法
     *
     * ***/

    public List<CacheIMEnBody> queryOneFriendMsg(String creator, String receive,int curPage) {
        int limit = 30;
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        CacheIMEnBodyDao cacheDao = daoSession.getCacheIMEnBodyDao();
        QueryBuilder qb = cacheDao.queryBuilder()
                .where(CacheIMEnBodyDao.Properties.MessageCreator.eq(creator), CacheIMEnBodyDao.Properties.MessageReceiver.eq(receive))
                .orderDesc(CacheIMEnBodyDao.Properties.Id)
                .offset(curPage * limit)
                .limit(limit);

        List<CacheIMEnBody> list = qb.list();

        return list;
    }

    /**
     *
     * 删除一个好友的聊天记录
     *
     */

    public void deleteAllData(String creator){
        Logger.d("删除的账号 = " +  creator);
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        CacheIMEnBodyDao cacheDao = daoSession.getCacheIMEnBodyDao();

        List<CacheIMEnBody> list  = cacheDao.queryBuilder()
                .where(CacheIMEnBodyDao.Properties.MessageCreator.eq(creator))
                .orderDesc(CacheIMEnBodyDao.Properties.Id)
                .list();

        if (list != null){
            for (int i = 0; i < list.size(); i++){
                CacheIMEnBody bodyNew = list.get(i);
                cacheDao.delete(bodyNew);
            }
        }
    }

    /***
     *
     * 删除一个好友的私密聊天
     **
     * **/
    public void deleteSecretData(String creator, ArrayList<String> msgId){
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        CacheIMEnBodyDao cacheDao = daoSession.getCacheIMEnBodyDao();

        List<CacheIMEnBody> list  = cacheDao.queryBuilder()
                .where(CacheIMEnBodyDao.Properties.MessageCreator.eq(creator), CacheIMEnBodyDao.Properties.MessageIdClient.in(msgId))
                .orderDesc(CacheIMEnBodyDao.Properties.Id)
                .list();


        if (list != null){
            for (int i = 0; i < list.size(); i++){
                CacheIMEnBody bodyNew = list.get(i);
                cacheDao.delete(bodyNew);
            }
        }

    }

    /***
     *
     * 删除一条特定的消息
     **
     * **/

    public void deleteSpecData(String creator, String messageId){
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        CacheIMEnBodyDao cacheDao = daoSession.getCacheIMEnBodyDao();

        List<CacheIMEnBody> list  = cacheDao.queryBuilder()
                .where(CacheIMEnBodyDao.Properties.MessageCreator.eq(creator), CacheIMEnBodyDao.Properties.MessageIdClient.eq(messageId))
                .orderDesc(CacheIMEnBodyDao.Properties.Id)
                .list();

        if (list != null){
            for (int i = 0; i < list.size(); i++){
                CacheIMEnBody bodyNew = list.get(i);
                cacheDao.delete(bodyNew);
            }
        }
    }


}
