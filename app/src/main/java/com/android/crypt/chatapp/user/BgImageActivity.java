package com.android.crypt.chatapp.user;

import android.content.Intent;
import android.os.Bundle;

import com.android.crypt.chatapp.utility.Common.RunningData;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.R;

import org.devio.takephoto.app.TakePhoto;
import org.devio.takephoto.app.TakePhotoActivity;
import org.devio.takephoto.compress.CompressConfig;
import org.devio.takephoto.model.CropOptions;
import org.devio.takephoto.model.LubanOptions;
import org.devio.takephoto.model.TResult;
import org.devio.takephoto.model.TakePhotoOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class BgImageActivity extends TakePhotoActivity {

    private int limit = 1;
    private boolean isGlobal = true;
    private String bgAccount = null;
    private String mFile = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bg_image);
        initView();
    }

    private void initView(){
        Intent intent = getIntent();
        isGlobal = (boolean) intent.getSerializableExtra("is_global");
        bgAccount = (String) intent.getSerializableExtra("account");
        
        int width = RunningData.getInstance().getScreenWidth();
        int height = RunningData.getInstance().getScreenHeight();
        Logger.d("width = " + width + " height = " + height);
        startPhoto(getTakePhoto());

        mFile = RunningData.getInstance().getBgImageUrl();
    }

    @Override
    public void takeCancel() {
        super.takeCancel();
        finish();
    }

    @Override
    public void takeFail(TResult result, String msg) {
        super.takeFail(result, msg);
        finish();
    }


    @Override
    public void takeSuccess(TResult result) {
        super.takeSuccess(result);
        String imageUrl = result.getImage().getOriginalPath();
        saveBgImage(imageUrl);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //****相册
    public void startPhoto(TakePhoto takePhoto) {
        configTakePhotoOption(takePhoto);
        takePhoto.onPickMultipleWithCrop(limit, getCropOptions());
    }

    private void configTakePhotoOption(TakePhoto takePhoto) {
        TakePhotoOptions.Builder builder = new TakePhotoOptions.Builder();
        builder.setWithOwnGallery(true);
        builder.setCorrectImage(true);

        takePhoto.setTakePhotoOptions(builder.create());
    }


    private CropOptions getCropOptions() {
        int height = RunningData.getInstance().getScreenHeight() - 50;
        int width = RunningData.getInstance().getScreenWidth();

        CropOptions.Builder builder = new CropOptions.Builder();
        builder.setAspectX(width).setAspectY(height);
        builder.setWithOwnCrop(true);
        return builder.create();
    }


    public void saveBgImage(String imageUrl) {
        if (mFile != null){
            String[] surNmaeArray = imageUrl.toString().split("\\.");
            String surNmae = "png";
            if (surNmaeArray.length >= 1){
                surNmae = surNmaeArray[surNmaeArray.length - 1];
                String savefile = mFile + bgAccount + "." + surNmae;
                if (isGlobal == true){
                    savefile = mFile + "global_bg_image" + "." + surNmae;
                }
                copyFile(imageUrl, savefile);
            }
        }
        finish();
    }

    public boolean copyFile(String oldPath$Name, String newPath$Name) {
        try {
            File oldFile = new File(oldPath$Name);
            if (!oldFile.exists()) {
                Logger.d("--Method--", "copyFile:  oldFile not exist.");
                return false;
            } else if (!oldFile.isFile()) {
                Logger.d("--Method--", "copyFile:  oldFile not file.");
                return false;
            } else if (!oldFile.canRead()) {
                Logger.d("--Method--", "copyFile:  oldFile cannot read.");
                return false;
            }

            /* 如果不需要打log，可以使用下面的语句
            if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
                return false;
            }
            */

            File file = new File(mFile);
            long fileSize = file.length();
            if (fileSize <= 0){
                return false;
            }
            if (!file.exists()){
                return false;
            }
            //删除旧的图
            File newFile = new File(newPath$Name);
            if (newFile.exists()){
                newFile.delete();
            }

            FileInputStream fileInputStream = new FileInputStream(oldPath$Name);
            FileOutputStream fileOutputStream = new FileOutputStream(newPath$Name);
            byte[] buffer = new byte[(int)fileSize];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
