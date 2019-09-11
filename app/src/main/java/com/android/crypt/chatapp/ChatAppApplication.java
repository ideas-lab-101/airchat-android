package com.android.crypt.chatapp;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.android.crypt.chatapp.utility.Common.NotifyUtils;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.Websocket.Link.WsManager;
import com.android.crypt.chatapp.utility.Cache.CacheManager.ObjectCacheManager;
import com.android.crypt.chatapp.utility.Common.CrashHandler;
import com.chatapp.push.PushTargetManager;
import com.chatapp.push.handle.impl.BaseHandleListener;
import com.chatapp.push.model.ReceiverInfo;
import com.chatapp.push.util.PushCallback;
import com.chatapp.push.util.PushRunningData;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.HttpParams;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.utility.Cache.CacheManager.IMMsgDBManager;
import com.android.crypt.chatapp.utility.Websocket.Link.ForegroundCallbacks;


import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;

public class ChatAppApplication extends MultiDexApplication implements PushCallback {
	public static ChatAppApplication instances;
	private static Application context;
	private static ChatAppApplication mContextApplication = null;
	private List<Activity> mList = new LinkedList<Activity>();
	private OkHttpClient httpClient = null;
	private int count = 0;
	private boolean  islogIn = false;
    private boolean  isForground = true;

    @Override
	public void onCreate() {
		super.onCreate();
		mContextApplication = this;
		instances = this;
		context = this;

        //初始化网络
		initNetwork();
        initSpeech();
        initShareSDK();
		initLog();
		initAppStatusListener();
		initLifecycle();
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this); // 初始化
	}

	public static ChatAppApplication getInstances(){
		return instances;
	}


    /**
     * 初始化shareSDK
     */
    public void initShareSDK(){

    }

    /**
     * 讯飞引擎
     */
    private void initSpeech() {
		SpeechUtility.createUtility(context, SpeechConstant.APPID +"=5d0756fc");
    }

    /**
     * 初始化推送
     */
    public void initPush(String numberSha){
    	islogIn = true;
		PushRunningData.getInstance().setCallbacks(this).setAlias(numberSha);
		PushTargetManager.getInstance().init(this);
	}

	private void initNetwork(){
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //全局的读取超时时间
        builder.readTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        //全局的写入超时时间
        builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        //全局的连接超时时间
        builder.connectTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        HttpHeaders headers = new HttpHeaders();
//        headers.put("commonHeaderKey1", "commonHeaderValue1");    //header不支持中文，不允许有特殊字符
        HttpParams params = new HttpParams();
//        params.put("commonParamsKey1", "commonParamsValue1");     //param支持中文,直接传,不要自己编码
        OkGo.getInstance().init(this)                       //必须调用初始化
                .setOkHttpClient(builder.build())               //建议设置OkHttpClient，不设置将使用默认的
                .setCacheMode(CacheMode.DEFAULT)               //全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   //全局统一缓存时间，默认永不过期，可以不传
                .setRetryCount(3)                               //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
                .addCommonHeaders(headers)                      //全局公共头
                .addCommonParams(params);                       //全局公共参数
    }

	public OkHttpClient getHttpClient() {
		return httpClient;
	}

	//将activity加入队列
	public void addActivity(Activity activity) {
		mList.add(activity);
	}

	//退出程序，将注册的Activity都finish
	public void exit() {
		try {
			for (Activity activity : mList) {
				if (activity != null)
					activity.finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}

	public static ChatAppApplication getDBcApplication() {
		return mContextApplication;
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		exit();
		super.onTerminate();
	}

	/**
	 * 初始化日志配置
	 */
	private void initLog() {
		ApplicationInfo info = context.getApplicationInfo();
		boolean isDebug = (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
		if (isDebug == true){
			//***打印日志
			Logger.addLogAdapter(new AndroidLogAdapter());
		}
		//***崩溃日志
		CrashHandler.getInstance().init(this);
	}

	/***
	 *
	 * 初始化数据库管理类 GreenDAO
	 *
	 * ****/

	public IMMsgDBManager getIMCacheManager(){
		IMMsgDBManager dbManager = IMMsgDBManager.getInstance(this);
		return dbManager;
	}

	public ObjectCacheManager getObjectCacheManager(){
		ObjectCacheManager dbManager = ObjectCacheManager.getInstance(this);
		return dbManager;
	}


	/**
	 * 初始化应用前后台状态监听
	 */
	private void initAppStatusListener() {
		ForegroundCallbacks.init(this).addListener(new ForegroundCallbacks.Listener() {
			@Override
			public void onBecameForeground() {
				Logger.d("应用回到前台调用重连方法");
				WsManager.getInstance().reconnect();
			}
			@Override
			public void onBecameBackground() {}
		});
	}

	public static Context getContext(){
		return context;
	}


	private void initLifecycle(){
		registerActivityLifecycleCallbacks(new SwitchBackgroundCallbacks());
	}
	private class SwitchBackgroundCallbacks implements ActivityLifecycleCallbacks {
		@Override
		public void onActivityCreated(Activity activity, Bundle bundle) {}
		@Override
		public void onActivityResumed(Activity activity) {}
		@Override
		public void onActivityPaused(Activity activity) {}

		@Override
		public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {}
		@Override
		public void onActivityDestroyed(Activity activity) {}

		@Override
		public void onActivityStopped(Activity activity) {
			count--;
			if (count == 0) { //前台切换到后台
				Logger.d("App切到后台>>>>>>>>>>>>>>>>>>>");
                isForground = false;
				if (islogIn == true){
					PushTargetManager.getInstance().setForeground(ChatAppApplication.this, false);
				}
			}
		}
		@Override
		public void onActivityStarted(Activity activity) {
			if (count == 0) { //后台切换到前台
				Logger.d(">>>>>>>>>>>>>>>>>>>App切到前台");
                isForground = true;
                if (islogIn == true) {
					PushTargetManager.getInstance().setForeground(ChatAppApplication.this,true);
				}
			}
			count++;
		}
	}

	@Override
	public void comeSomePush(int count) {
		Logger.d("来了一个通知");
		if (isForground == false){
            NotifyUtils.showNotification(getApplicationContext(),"你有一条新信息","点击查看", MainActivity.class);
        }
//		try{
//			ShortcutBadger.applyCountOrThrow(WelcomeActivity.getLauncher(), 10); //for 1.1.4+
//		}catch (ShortcutBadgeException e){Logger.d("comeSomePush Badge错误" + e);}
	}

	@Override
	public void pushIsOpened() {
		Logger.d("通知被点击了");
//		ShortcutBadger.removeCount(WelcomeActivity.getLauncher());
	}


	@Override
	public void loggerString(String value) {
		Logger.d("push --- \n " + value);
	}
}
