package com.android.crypt.chatapp;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.android.crypt.chatapp.guide.WelcomeActivity;
import com.android.crypt.chatapp.utility.Common.NotifyUtils;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.Common.WRKShareUtil;
import com.android.crypt.chatapp.utility.Websocket.Link.WsManager;
import com.android.crypt.chatapp.utility.Cache.CacheManager.ObjectCacheManager;
import com.android.crypt.chatapp.utility.Common.CrashHandler;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.HttpParams;
import com.mob.MobSDK;
import com.mob.pushsdk.MobPush;
import com.mob.pushsdk.MobPushCallback;
import com.mob.pushsdk.MobPushCustomMessage;
import com.mob.pushsdk.MobPushNotifyMessage;
import com.mob.pushsdk.MobPushReceiver;
import com.mob.tools.proguard.ProtectedMemberKeeper;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.utility.Cache.CacheManager.IMMsgDBManager;
import com.android.crypt.chatapp.utility.Websocket.Link.ForegroundCallbacks;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;

public class ChatAppApplication extends MultiDexApplication implements ProtectedMemberKeeper {
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
		initPush();
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
		WRKShareUtil.getInstance().regToWeiXin();
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


	protected String getAppkey() {
		return "2c68c5944f2d0";
	}

	protected String getAppSecret() {
		return "0d42f4293c910c69943551e6bf00765a";
	}

    private void initPush(){
    	islogIn = true;
		MobSDK.init(this, this.getAppkey(), this.getAppSecret());
		//获取注册id
		MobPush.getRegistrationId(new MobPushCallback<String>() {
			@Override
			public void onCallback(String data) {
				RunningData.getInstance().setMobPushRegistrationId(data);
			}
		});
	}

	public void setPushAlias(String alias){
		Logger.d("推送别名 alias = " + alias);
		RunningData.getInstance().setPushAlias(alias);
		MobPush.setAlias(alias);
		MobPush.setShowBadge(true);
		MobPush.setAppForegroundHiddenNotification(true);

		//		MobPush.addTags(new String[]{""});
		if (Build.VERSION.SDK_INT >= 21) {
			MobPush.setNotifyIcon(R.mipmap.default_head);
		} else {
			MobPush.setNotifyIcon(R.mipmap.default_head);
		}

		MobPush.addPushReceiver(new MobPushReceiver() {
			@Override
			public void onCustomMessageReceive(Context context, MobPushCustomMessage message) {
				//接收自定义消息(透传)
				System.out.println("onCustomMessageReceive:" + message.toString());
			}
			@Override
			public void onNotifyMessageReceive(Context context, MobPushNotifyMessage message) {
				showNoty();
			}

			@Override
			public void onNotifyMessageOpenedReceive(Context context, MobPushNotifyMessage message) {
				//接收通知消息被点击事件
			}

			@Override
			public void onTagsCallback(Context context, String[] tags, int operation, int errorCode) {
				//接收tags的增改删查操作
			}

			@Override
			public void onAliasCallback(Context context, String alias, int operation, int errorCode) {
				//接收alias的增改删查操作
			}
		});
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
//		boolean isDebug = RunningData.getInstance().IsApkInDebug();
//		if (isDebug == true){
//            ***打印日志
//			Logger.addLogAdapter(new AndroidLogAdapter());
//		}
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
                isForground = false;
				if (islogIn == true){
//					PushTargetManager.getInstance().setForeground(ChatAppApplication.this, false);
				}
			}
		}
		@Override
		public void onActivityStarted(Activity activity) {
			if (count == 0) { //后台切换到前台
                isForground = true;
                if (islogIn == true) {
//					PushTargetManager.getInstance().setForeground(ChatAppApplication.this,true);
				}
			}
			count++;
		}
	}

	public enum PushTargetEnum {
		XIAOMI("XIAOMI"), //小米
		HUAWEI("HUAWEI"),//华为
		MEIZU("MEIZU"); //魅族

		public String brand;
		PushTargetEnum(String brand) {
			this.brand = brand;
		}
	}


	private void showNoty(){
		if (isForground == false){
//			String mobile_brand = android.os.Build.MANUFACTURER;
//			mobile_brand = mobile_brand.toUpperCase();
//			//根据设备厂商选择推送平台
//			if (PushTargetEnum.XIAOMI.brand.equals(mobile_brand) ||
//					PushTargetEnum.HUAWEI.brand.equals(mobile_brand) ||
//					PushTargetEnum.MEIZU.brand.equals(mobile_brand)	) {
//			} else {
//				NotifyUtils.showNotification(getApplicationContext(),"你有一条新信息","点击查看", WelcomeActivity.class);
//			}
			NotifyUtils.showNotification(getApplicationContext(),"你有一条新信息","点击查看", WelcomeActivity.class);
		}
	}

}
