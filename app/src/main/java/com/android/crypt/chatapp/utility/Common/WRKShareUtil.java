package com.android.crypt.chatapp.utility.Common;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.android.crypt.chatapp.ChatAppApplication;
import com.android.crypt.chatapp.R;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import rx.functions.Action1;

/**
 * Created by White on 2019/8/23.
 */

public class WRKShareUtil {
    private IWXAPI wxapi;
    private static WRKShareUtil instance;

    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
    private static final int THUMB_SIZE = 100; //缩略图大小
    private static String appID;

    private WRKShareUtil(String appID) {
        this.appID = appID;
    }

    public static WRKShareUtil getInstance() {
        if (instance == null) {
            synchronized (WRKShareUtil.class) {
                if (instance == null) {
                    if (appID == null) {
                        //获取清单文件中的APP_ID
                        appID = "wx08b50440146f6a41";
                    }
                    instance = new WRKShareUtil(appID);
                }
            }
        }
        return instance;
    }

    public void regToWeiXin() {
        wxapi = WXAPIFactory.createWXAPI(ChatAppApplication.getContext(), appID, true);
        wxapi.registerApp(appID);
    }

    /**
     * 判断是否安装微信
     */
    public boolean isWeiXinAppInstall() {
        if (wxapi == null)
            wxapi = WXAPIFactory.createWXAPI(ChatAppApplication.getContext(), appID);
        if (wxapi.isWXAppInstalled()) {
            return true;
        } else {
            Toast.makeText(ChatAppApplication.getContext(), "没有安装微信", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * 是否支持分享到朋友圈
     */
    public boolean isWXAppSupportAPI() {
        if (isWeiXinAppInstall()) {
            int wxSdkVersion = wxapi.getWXAppSupportAPI();
            if (wxSdkVersion >= TIMELINE_SUPPORTED_VERSION) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String buildTransaction(String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    public IWXAPI getWxApi() {
        if (wxapi == null)
            wxapi = WXAPIFactory.createWXAPI(ChatAppApplication.getContext(), appID);
        return wxapi;
    }

    /**
     * 分享图片到微信
     */

    public void shareImageToWx(final Bitmap shareImage, String title, String desc, final int wxSceneSession) {
        Bitmap bmp = shareImage; //BitmapFactory.decodeResource(ChatAppApplication.getContext().getResources(), R.mipmap.ic_launcher);
        WXImageObject imgObj = new WXImageObject(bmp);

        final WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;
        msg.title = title;
        msg.description = desc;
        final Bitmap[] thumbBmp = {Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true)};
        bmp.recycle();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    InputStream imageStream = getImageStream(imgUrl);
                    Bitmap bitmap = shareImage;//BitmapFactory.decodeStream(imageStream);
                    thumbBmp[0] = Bitmap.createScaledBitmap(bitmap, THUMB_SIZE, THUMB_SIZE, true);
                    bitmap.recycle();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                msg.thumbData = bmpToByteArray(thumbBmp[0], true);

                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = buildTransaction("img");
                req.message = msg;
                req.scene = wxSceneSession;
                getWxApi().sendReq(req);
            }
        }).start();
    }


    /**
     * 分享url地址
     *
     * @param url            地址
     * @param title          标题
     * @param desc           描述
     * @param wxSceneSession 类型
     */
    public void shareUrlToWx(String url, String title, String desc, final int wxSceneSession) {

        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
        final WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;

        msg.description = desc;
        Bitmap bmp = BitmapFactory.decodeResource(ChatAppApplication.getContext().getResources(), R.mipmap.default_head);
        final Bitmap[] thumbBmp = {Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true)};
        bmp.recycle();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    InputStream imageStream = getImageStream(iconUrl);
                    Bitmap bitmap = BitmapFactory.decodeResource(ChatAppApplication.getContext().getResources(), R.mipmap.default_head);//BitmapFactory.decodeStream(imageStream);
                    thumbBmp[0] = Bitmap.createScaledBitmap(bitmap, THUMB_SIZE, THUMB_SIZE, true);
                    bitmap.recycle();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                msg.thumbData = bmpToByteArray(thumbBmp[0], true);
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = buildTransaction("webpage");
                req.message = msg;
                req.scene = wxSceneSession;
                getWxApi().sendReq(req);
            }
        }).start();
    }



    public InputStream getImageStream(String path) throws Exception {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5 * 1000);
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return conn.getInputStream();
        }
        return null;
    }

    public  byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
