package com.android.crypt.chatapp.utility.Cache.CacheManager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.android.crypt.chatapp.utility.Cache.CacheClass.DaoMaster;
import com.android.crypt.chatapp.utility.Cache.CacheClass.DaoSession;
import com.android.crypt.chatapp.utility.Cache.CacheClass.ObjectCacheBody;
import com.android.crypt.chatapp.utility.Cache.CacheClass.ObjectCacheBodyDao;

import org.greenrobot.greendao.query.Query;


/**
 * Created by White on 2019/4/26.
 */

public class ObjectCacheManager {
    private final static String dbName = "chap_app_obj_message";
    private static ObjectCacheManager mInstance;
    private DaoMaster.DevOpenHelper openHelper;
    private Context context;

    public ObjectCacheManager(Context context) {
        this.context = context;
        openHelper = new DaoMaster.DevOpenHelper(context, dbName, null);
    }

    /**
     * 获取单例引用
     *
     * @param context
     * @return
     */
    public static ObjectCacheManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (ObjectCacheManager.class) {
                if (mInstance == null) {
                    mInstance = new ObjectCacheManager(context);
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
     *   插入一条记录  insertOrReplace
     *
     */
    public void insertData(ObjectCacheBody data) {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        ObjectCacheBodyDao cacheDao = daoSession.getObjectCacheBodyDao();
        cacheDao.insertOrReplace(data);
    }

    /***
     *
     *  根据ID寻找数据
     *
     * **/

    public ObjectCacheBody queryData(Long id) {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        ObjectCacheBodyDao cacheDao = daoSession.getObjectCacheBodyDao();

        Query<ObjectCacheBody> query = cacheDao.queryBuilder().where(ObjectCacheBodyDao.Properties.Id.eq(id))
                .orderDesc(ObjectCacheBodyDao.Properties.Id).build();
        return query.unique();

    }


}
