package com.android.crypt.chatapp.utility.Common;

import android.os.Environment;

import java.io.File;

public class ParameterUtil {
	/**
	 * 全局输出锁
	 */
	public static final boolean printLogFlag = true;


    public static String rootPath = "/crypt.chatapp";

	// 下载apk升级
	public static final String savePath = getSDPath()+ rootPath + "/updateAPK/";

	/**
	 * 本地临时附件压缩包存放路径
	 */
	public static String saveAttachTempPath = getSDPath()+ rootPath +"/data/attach/temp/";

	public static String saveAttachTempZipPath = getSDPath()+rootPath+"/data/attach/tempZip/";

	public static String cachePath = getSDPath() + rootPath + "/data/cache/";

	public static String imagePath = getSDPath() + rootPath + "/data/cache/airchat/";

	/**
	 * 当前登录帐号记录文件名
	 */
	public static String CUR_ACCOUNT_MANAGEMENT_XML = "CUR_ACCOUNT_MANAGEMENT_XML";
    public static String CUR_PROFILE_MANAGEMENT_XML = "CUR_PROFILE_MANAGEMENT_XML";

	public static String SYS_SETTING_XML = "SYS_SETTING_XML";
	
	/**
	 * 资讯频道记录文件名
	 */
	public static String CHANNEL_LIST_MANAGEMENT_XML = "CHANNEL_LIST_MANAGEMENT_XML";
	
	/**
	 * 资讯列表记录文件名
	 */
	public static String INFO_LIST_MANAGEMENT_XML = "INFO_LIST_MANAGEMENT_XML";

    public static String DOC_LIST_MANAGEMENT_XML = "DOC_LIST_MANAGEMENT_XML";
	
	/**
	 * 消息列表记录文件名
	 */
	public static String MSG_LIST_MANAGEMENT_XML = "MSG_LIST_MANAGEMENT_XML";
	
	/**
	 * 省数据列表
	 */
	public static String PROVINCE_LIST_MANAGEMENT_XML = "PROVINCE_LIST_MANAGEMENT_XML";
	
	/**
	 * 城市数据列表
	 */
	public static String CITY_LIST_MANAGEMENT_XML = "CITY_LIST_MANAGEMENT_XML";

	public static String ORG_LIST_MANAGEMENT_XML = "ORG_LIST_MANAGEMENT_XML";

	/**
	 * 通讯录列表
	 */
	public static String GROUP_LIST_MANAGEMENT_XML = "GROUP_LIST_MANAGEMENT_XML";

	public static String CONTACT_LIST_MANAGEMENT_XML = "CONTACT_LIST_MANAGEMENT_XML";


	public static String TOPIC_LIST_MANAGEMENT_XML = "TOPIC_LIST_MANAGEMENT_XML";

	public static String SIGN_LIST_MANAGEMENT_XML = "SIGN_LIST_MANAGEMENT_XML";

	public static String COMMENT_LIST_MANAGEMENT_XML = "COMMENT_LIST_MANAGEMENT_XML";

	public static String ORDER_LIST_MANAGEMENT_XML = "ORDER_LIST_MANAGEMENT_XML";

	/**
	 * 应用列表记录文件名
	 */
	public static String APP_LIST_MANAGEMENT_XML = "APP_LIST_MANAGEMENT_XML";

	public static String CATALOG_LIST_MANAGEMENT_XML = "CATALOG_LIST_MANAGEMENT_XML";

	public static String DEVICE_LIST_MANAGEMENT_XML = "DEVICE_LIST_MANAGEMENT_XML";

	/**
	 * 课程相关
	 */
	public static String SUBJECT_LIST_MANAGEMENT_XML = "SUBJECT_LIST_MANAGEMENT_XML";

	public static String COURSE_LIST_MANAGEMENT_XML = "COURSE_LIST_MANAGEMENT_XML";

	public static String LESSON_LIST_MANAGEMENT_XML = "LESSON_LIST_MANAGEMENT_XML";


    /**
     * 任务列表
     */
	public static String NOTICE_LIST_MANAGEMENT_XML = "NOTICE_LIST_MANAGEMENT_XML";

	public static String ACTIVITY_LIST_MANAGEMENT_XML = "ACTIVITY_LIST_MANAGEMENT_XML";

    public static String COOR_LIST_MANAGEMENT_XML = "COOR_LIST_MANAGEMENT_XML";

	public static String FORM_LIST_MANAGEMENT_XML = "FORM_LIST_MANAGEMENT_XML";

	public static String FORUM_LIST_MANAGEMENT_XML = "FORUM_LIST_MANAGEMENT_XML";

	public static String LETTER_LIST_MANAGEMENT_XML = "LETTER_LIST_MANAGEMENT_XML";

	public static String LIVE_LIST_MANAGEMENT_XML = "LIVE_LIST_MANAGEMENT_XML";

	/**
	 * 班级成员缓存
	 */
	public static String CLASSMATE_LIST_MANAGEMENT_XML = "CLASSMATE_LIST_MANAGEMENT_XML";

    /**
	 * 获取sdcard根目录  /mnt/sdcard
	 * @return
	 */
	public static String getSDPath(){
		String sdDirPath="/sdcard";
	       File sdDir = null;
	       boolean sdCardExist = Environment.getExternalStorageState()
	                           .equals(Environment.MEDIA_MOUNTED);   //判断sd卡是否存在
	       if(sdCardExist)   
	       {                               
	         sdDir = Environment.getExternalStorageDirectory();//获取根目录
	         sdDirPath=sdDir.toString(); 
	       }   
	       return sdDirPath;
	}
}
