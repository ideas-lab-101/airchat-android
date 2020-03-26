package com.android.crypt.chatapp.msgDetail.IMTools;

import android.content.Intent;
import android.os.Bundle;

import com.android.crypt.chatapp.R;

import org.devio.takephoto.app.TakePhoto;
import org.devio.takephoto.app.TakePhotoActivity;
import org.devio.takephoto.compress.CompressConfig;
import org.devio.takephoto.model.LubanOptions;
import org.devio.takephoto.model.TImage;
import org.devio.takephoto.model.TResult;
import org.devio.takephoto.model.TakePhotoOptions;

import java.util.ArrayList;

public class ChooseEmojiActivity extends TakePhotoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_emoji);
        initView();
    }

    private void initView(){
        Intent intent = getIntent();
        startPhoto(getTakePhoto());
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
        showImg(result.getImages());
    }

    private void showImg(ArrayList<TImage> images) {
        Intent resendIntent = new Intent();
        resendIntent.putExtra("images", images);
        setResult(100, resendIntent);
        finish();
    }


    //****相册
    public void startPhoto(TakePhoto takePhoto) {
        configCompress(takePhoto);
        configTakePhotoOption(takePhoto);
        takePhoto.onPickMultiple(1);
    }

    private void configTakePhotoOption(TakePhoto takePhoto) {
        TakePhotoOptions.Builder builder = new TakePhotoOptions.Builder();
        builder.setWithOwnGallery(true);
        builder.setCorrectImage(true);
        takePhoto.setTakePhotoOptions(builder.create());
    }

    private void configCompress(TakePhoto takePhoto) {
        int maxSize = 1024 * 512;  // 512 K
        int width = 300;
        int height = 300;
        boolean showProgressBar =  true;
        boolean enableRawFile =  false;
        CompressConfig config;
        LubanOptions option = new LubanOptions.Builder().setMaxHeight(height).setMaxWidth(width).setMaxSize(maxSize).create();
        config = CompressConfig.ofLuban(option);
        config.enableReserveRaw(enableRawFile);
        takePhoto.onEnableCompress(config, showProgressBar);
    }

}
