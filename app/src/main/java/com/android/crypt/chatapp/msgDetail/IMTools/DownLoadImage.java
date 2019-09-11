package com.android.crypt.chatapp.msgDetail.IMTools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.Crypt.CryTool;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by White on 2019/6/19.
 */

public class DownLoadImage {

    public ImageCallback callbacks;

    public interface ImageCallback{
        void showImageResult(Bitmap bitmap);
    }
    private  Context context;
    public DownLoadImage(Context context, ImageCallback callback){
        this.context = context;
        this.callbacks = callback;

    }
    public Bitmap bitmap;



    public void loadFile(final PhotoView photo, final String allurl, final String deKey, int placeHolder){
        //下载路径

        //本地路径 斜线替换成下划线  / to -
        String[] fileNames = allurl.replace("/", "_").split("imModelImages_");
        final String fileName = fileNames[fileNames.length - 1];
        final String imagePath =  RunningData.getInstance().getEncodeImageUrl();
        final String fileLocal = imagePath + fileName;


        photo.setImageDrawable(context.getResources().getDrawable(placeHolder));
        Logger.d("fileLocal = " + fileLocal);
        Logger.d("allurl = " + allurl);
        Logger.d("fileName = " + fileName);

        if (fileIsExists(fileLocal)){
            showImage(photo, fileLocal, deKey);
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkGo.<File>get(allurl)
                        .cacheKey(fileLocal)
                        .cacheMode(CacheMode.IF_NONE_CACHE_REQUEST)
                        .execute(new FileCallback(imagePath, fileName) {//在下载中，可以设置下载下来的文件的名字
                            @Override
                            public void downloadProgress(Progress progress) {
                                super.downloadProgress(progress);
                                double percent = progress.currentSize / progress.totalSize;
                                if (percent < 0){
                                    percent = 0;
                                }
                                if (percent > 1){
                                    percent = 1;
                                }
                            }

                            @Override
                            public void onError(Response<File> response) {
                                super.onError(response);
                            }

                            @Override
                            public void onCacheSuccess(Response<File> response) {
                                super.onCacheSuccess(response);
                            }

                            @Override
                            public void onSuccess(Response<File> response) {
                                Logger.d("onSuccess onSuccess onSuccess");
                            }

                            @Override
                            public void onFinish() {
                                super.onFinish();
                                Logger.d("onFinish onFinish onFinish");
                                showImage(photo, fileLocal, deKey);
                            }
                        });
            }
        }).start();

    }

    private void showImage(PhotoView photo, String imagePath, String deKey){
        if (fileIsExists(imagePath)){
            try{
                byte[] data = getContent(imagePath);
                if (data != null){
                    CryTool tool = new CryTool();
                    byte[] date_de = tool.aesImageDeWith(data, deKey);
                    if (date_de != null){
                        bitmap = BitmapFactory.decodeByteArray(date_de, 0, date_de.length);
                        photo.setImageBitmap(bitmap);
                        photo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        date_de = null;
                        data = null;
                        if (callbacks != null){
                            callbacks.showImageResult(bitmap);
                        }
                    }
                }
            }catch (Exception e){

            }
        }
    }


    private byte[] getContent(String filePath) throws IOException {
        File file = new File(filePath);
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            System.out.println("file too big...");
            return null;
        }
        FileInputStream fi = new FileInputStream(file);
        byte[] buffer = new byte[(int) fileSize];
        int offset = 0;
        int numRead = 0;
        while (offset < buffer.length
                && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
            offset += numRead;
        }
        // 确保所有数据均被读取
        if (offset != buffer.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        fi.close();
        return buffer;
    }



    public boolean fileIsExists(String strFile) {
        try {
            File f=new File(strFile);
            if(!f.exists()) {
                return false;
            }
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }
}
