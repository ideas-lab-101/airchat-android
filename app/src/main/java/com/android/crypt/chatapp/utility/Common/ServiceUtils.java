package com.android.crypt.chatapp.utility.Common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.util.UUID;


/**
 * @author
 * 
 */
public class ServiceUtils {

	/**
	 * @param con
	 *            容器
	 * @return true有网络,false无网络
	 */
	public static boolean isNetworkAvailable(Context con) {
		ConnectivityManager cm = (ConnectivityManager) con
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null)
			return false;
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo == null) {
			return false;
		}
		if (netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	/**
	 * 获取当前网络连接类别
	 * 
	 * @param con
	 * @return state -1 无网络,0 WIFI网络,1 数据网络
	 */
	public static int getConnectivityManagerType(Context con) {
		int state = -1;
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) con
					.getSystemService(Context.CONNECTIVITY_SERVICE);// 获取系统的连接服务
			NetworkInfo activeNetInfo = connectivityManager
					.getActiveNetworkInfo();// 获取网络的连接情况
			if (null == activeNetInfo) {// 无网络
				state = -1;
			} else if (activeNetInfo.isConnected()) {// 有网络
				if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
					// 判断WIFI网
					state = 0;
				} else if (activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
					// 判断3G网
					state = 1;
					// 跳转到无线网络设置界面
					// startActivity(new
					// Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
					// 跳转到无限wifi网络设置界面
					// startActivity(new
					// Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
				}
			}

		} catch (Exception e) {
			state = -1;
		}
		return state;
	}

	/**
	 * 获取当前网络连接类别
	 * 
	 * @param con
	 * @return state -1无网络,0WIFI网络,1其他网络
	 */
	public static String getConnectionManagerType(Context con) {
		String str = null;
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) con
					.getSystemService(Context.CONNECTIVITY_SERVICE);// 获取系统的连接服务
			NetworkInfo activeNetInfo = connectivityManager
					.getActiveNetworkInfo();// 获取网络的连接情况
			if (null == activeNetInfo) {// 无网络
				str = "";
			} else if (activeNetInfo.isConnected()) {// 有网络
				if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
					// 判断WIFI网
					str = "WIFI";
				} else if (activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
					// 判断3G网
					str = "3G";
					// 跳转到无线网络设置界面
					// startActivity(new
					// Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
					// 跳转到无限wifi网络设置界面
					// startActivity(new
					// Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
				}
			}

		} catch (Exception e) {
			str = "";
		}
		return str;
	}

	/**
	 * 获得设备唯一设别码，无须权限
	 *
	 * @return
	 */
	public static String getUniqueID() {
		String serial = null;

		String m_szDevIDShort = "35" +
				Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +

				Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +

				Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +

				Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +

				Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +

				Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +

				Build.USER.length() % 10; //13 位

		try {
			serial = Build.class.getField("SERIAL").get(null).toString();
			//API>=9 使用serial号
			return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
		} catch (Exception exception) {
			//serial需要一个初始化
			serial = "serial"; // 随便一个初始化
		}
		//使用硬件信息拼凑出来的15位号码
		return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
	}

	/**
	 * 获取当前程序版本
	 *
	 * @param con
	 * @return
	 */
	public static String getCurVersionName(Context con) {
		String vn = "1.0.0";
		try {
			PackageInfo info = con.getPackageManager().getPackageInfo(
					con.getPackageName(), 0);
			vn = info.versionName;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return vn;
	}

    public static int getCurVersionCode(Context con) {
        int vc = 1;
        try {
            PackageInfo info = con.getPackageManager().getPackageInfo(
                    con.getPackageName(), 0);
            vc = info.versionCode;
        } catch (Exception e) {
            // TODO: handle exception
        }
        return vc;
    }

	/**
	 * 获取手机MAC地址
	 *
	 * @param context
	 * @return
	 */
	public static String getMacAddress(Context context) {
		String result = "";
		try {
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			result = wifiInfo.getMacAddress();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return result;
	}

	/**
	 * 获取手机型号
	 *
	 * @param con
	 * @return
	 */
	public static String getBuildMODEL(Context con) {
		String result = "";
		try {
			result = Build.MODEL;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return result;
	}

	/**
	 * 根据URL获取相册图片地址
	 */
	public static String getRealPathFromURI(Context context, Uri contentUri) {
		String filePath = "";
		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
		if(isKitKat && DocumentsContract.isDocumentUri(context, contentUri)){
			String wholeID = DocumentsContract.getDocumentId(contentUri);
			String id = wholeID.split(":")[1];
			String[] column = { MediaStore.Images.Media.DATA };
			String sel = MediaStore.Images.Media._ID + "=?";
			Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column,
					sel, new String[] { id }, null);
			int columnIndex = cursor.getColumnIndex(column[0]);
			if (cursor.moveToFirst()) {
				filePath = cursor.getString(columnIndex);
			}
			cursor.close();
		}else{
			String[] projection = { MediaStore.Images.Media.DATA };
			Cursor cursor = context.getContentResolver().query(contentUri, projection, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			filePath = cursor.getString(column_index);
		}
		return filePath;
	}

}
