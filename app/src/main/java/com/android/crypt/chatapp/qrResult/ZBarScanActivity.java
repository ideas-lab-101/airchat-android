package com.android.crypt.chatapp.qrResult;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.ChatAppApplication;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.msgDetail.IMTools.ChoosePhotoActivity;
import com.orhanobut.logger.Logger;

import org.devio.takephoto.model.TImage;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zbar.ZBarView;

public class ZBarScanActivity extends BaseActivity implements QRCodeView.Delegate, View.OnClickListener {

    @BindView(R.id.zbarview)
    ZBarView zbarview;
    @BindView(R.id.quit_photo_viewer)
    ImageButton quitPhotoViewer;
    @BindView(R.id.open_flash)
    ImageButton openFlash;
    @BindView(R.id.open_album)
    ImageButton openAlbum;

    private boolean isOpenFlash = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zbar_scan);
        ButterKnife.bind(this);
        initGlobalData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 处理ActionBar的菜单
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void initGlobalData() {
        zbarview.setDelegate(this);
        zbarview.getScanBoxView().setOnlyDecodeScanBoxArea(false);
        quitPhotoViewer.setOnClickListener(this);
        openFlash.setOnClickListener(this);
        openAlbum.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.quit_photo_viewer:
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                break;
            case R.id.open_flash:
                openFlash();
                break;
            case R.id.open_album:
                openAlbumMethod();
                break;
            default:
                break;
        }
    }

    private void openFlash(){
        if (!isOpenFlash){
            isOpenFlash = true;
            zbarview.openFlashlight();
        }else{
            isOpenFlash = false;
            zbarview.closeFlashlight();
        }
    }

    private void openAlbumMethod(){
        Intent intent = new Intent(this, ChoosePhotoActivity.class);
        intent.putExtra("image_kind", true);
        startActivityForResult(intent, 1);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == 200){
                ArrayList<TImage> imagesUrl = (ArrayList<TImage>)data.getSerializableExtra("images");
                if (imagesUrl.size() > 0){
                    String imgLocalUrl = imagesUrl.get(0).getOriginalPath();
                    zbarview.decodeQRCode(imgLocalUrl);
                }
            }
        }
    }

        /***
         *
         * 扫码结果
         *
         * **/
    @Override
    protected void onStart() {
        super.onStart();
        zbarview.startCamera(); // 打开后置摄像头开始预览，但是并未开始识别
//        mZBarView.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT); // 打开前置摄像头开始预览，但是并未开始识别
        zbarview.startSpotAndShowRect(); // 显示扫描框，并开始识别
    }

    @Override
    protected void onStop() {
        zbarview.stopCamera(); // 关闭摄像头预览，并且隐藏扫描框
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        zbarview.onDestroy(); // 销毁二维码扫描控件
        super.onDestroy();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
//        Logger.d("result:" + result);
//        setTitle("扫描结果为：" + result);
        vibrate();
        if (result == null){
            Toast.makeText(ChatAppApplication.getContext(), "未识别出内容", Toast.LENGTH_SHORT).show();
            zbarview.startSpot();
        }else{
            Intent resultMethod = new Intent();
            resultMethod.putExtra("qr_code_string", result);
            setResult(RESULT_OK, resultMethod);
            zbarview.stopSpot();
            finish();
        }
    }

    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {
        // 这里是通过修改提示文案来展示环境是否过暗的状态，接入方也可以根据 isDark 的值来实现其他交互效果
        String tipText = zbarview.getScanBoxView().getTipText();
        String ambientBrightnessTip = "\n环境过暗，请打开闪光灯";
        if (isDark) {
            if (!tipText.contains(ambientBrightnessTip)) {
                zbarview.getScanBoxView().setTipText(tipText + ambientBrightnessTip);
            }
        } else {
            if (tipText.contains(ambientBrightnessTip)) {
                tipText = tipText.substring(0, tipText.indexOf(ambientBrightnessTip));
                zbarview.getScanBoxView().setTipText(tipText);
            }
        }
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Logger.d("打开相机出错");
        Toast.makeText(ChatAppApplication.getContext(), "打开相机出错", Toast.LENGTH_SHORT).show();
    }
}
