package com.android.crypt.chatapp.InfoSetting;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.contact.Model.ContactModel;
import com.android.crypt.chatapp.qrResult.MyQRCodeActivity;
import com.android.crypt.chatapp.utility.Cache.CacheClass.ObjectCacheType;
import com.android.crypt.chatapp.utility.Cache.CacheTool;
import com.android.crypt.chatapp.utility.Common.ClickUtils;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.okgo.callback.JsonCallback;
import com.android.crypt.chatapp.utility.okgo.model.CodeResponse;
import com.baoyz.actionsheet.ActionSheet;
import com.android.crypt.chatapp.user.Model.UserInfo;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.Response;
import com.orhanobut.logger.Logger;
import com.qiniu.android.common.AutoZone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.android.crypt.chatapp.R;

import org.devio.takephoto.app.TakePhoto;
import org.devio.takephoto.app.TakePhotoImpl;
import org.devio.takephoto.compress.CompressConfig;
import org.devio.takephoto.model.CropOptions;
import org.devio.takephoto.model.InvokeParam;
import org.devio.takephoto.model.LubanOptions;
import org.devio.takephoto.model.TContextWrap;
import org.devio.takephoto.model.TResult;
import org.devio.takephoto.model.TakePhotoOptions;
import org.devio.takephoto.permission.InvokeListener;
import org.devio.takephoto.permission.PermissionManager;
import org.devio.takephoto.permission.TakePhotoInvocationHandler;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InfoDataActivity extends BaseActivity implements ActionSheet.ActionSheetListener, TakePhoto.TakeResultListener, InvokeListener {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.user_name)
    TextView userName;
    @BindView(R.id.introduction)
    TextView introduction;
    @BindView(R.id.ac_number)
    TextView acNumber;
    @BindView(R.id.head_icon_change_f)
    LinearLayout headIconChangeF;
    @BindView(R.id.user_name_change_f)
    LinearLayout userNameChangeF;
    @BindView(R.id.introduction_change_f)
    LinearLayout introductionChangeF;

    private String token;
    private UserInfo userInfo;
    private String qiniuToken = null;
    private String imageUrl = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_data);
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.title_bar_userInfo);
        setSupportActionBar(toolbar);

        token = RunningData.getInstance().getToken();
        initView();
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
                overridePendingTransition(R.anim.in_from_left,
                        R.anim.out_to_right);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        Gson gson = new Gson();
        String userInfoString = CacheTool.getInstance().getCacheObject(ObjectCacheType.user_info);
        userInfo = gson.fromJson(userInfoString, UserInfo.class);

        userName.setText(userInfo.username);
        introduction.setText(userInfo.introduction);
        acNumber.setText(userInfo.login_name);

    }


    @OnClick({R.id.head_icon_change_f, R.id.user_name_change_f, R.id.introduction_change_f, R.id.iv_qrCode_change})
    public void onViewClicked(View view) {
        if (!ClickUtils.isFastClick()) {
            return;
        }
        Intent intent = null;
        switch (view.getId()) {
            case R.id.head_icon_change_f:
                choosePhoto();
                break;
            case R.id.user_name_change_f:
                intent = new Intent(this, ChangeMyInfoActivity.class);
                intent.putExtra("change_kind", "username");

                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right,
                        R.anim.out_to_left);
                break;
            case R.id.introduction_change_f:
                intent = new Intent(this, ChangeMyInfoActivity.class);
                intent.putExtra("change_kind", "introduction");

                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right,
                        R.anim.out_to_left);
                break;
            case R.id.iv_qrCode_change:
                myQrCodeAc();
                break;
            default:
                    break;
        }
    }


    private void myQrCodeAc(){
        ContactModel mMap = new ContactModel(userInfo.avatar_url, userInfo.username, "", userInfo.login_name, userInfo.introduction, "", "");
        Intent intent  = new Intent(this, MyQRCodeActivity.class);
        intent.putExtra("friendInfo", mMap);
        intent.putExtra("isMine", true);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right,
                R.anim.out_to_left);

    }



    private void choosePhoto(){
        ActionSheet.createBuilder(this, getSupportFragmentManager())
                .setCancelButtonTitle("取消")
                .setOtherButtonTitles("从相册种选择")
                .setCancelableOnTouchOutside(true)
                .setListener(this).show();
    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if (index == 0) {
            openTakePhoto();
        }
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancle) {


    }


////////***** 相册
    private TakePhoto takePhoto = null;
    private InvokeParam invokeParam;

    private void openTakePhoto(){
        if (null == takePhoto){
//            takePhoto = new TakePhotoImpl(this,this);
            takePhoto = (TakePhoto) TakePhotoInvocationHandler.of(this).bind(new TakePhotoImpl(this, this));
        }
        configCompress(takePhoto);
        configTakePhotoOption(takePhoto);
        takePhoto.onPickMultipleWithCrop(1, getCropOptions());
    }



    @Override
    public void takeSuccess(TResult result) {
//        Logger.d("takeSuccess：" + result.getImage().getCompressPath());
        imageUrl = result.getImage().getCompressPath();
        getUploadToken();
    }

    @Override
    public void takeFail(TResult result, String msg) {
        Logger.d("takeFail:" + msg);
    }

    @Override
    public void takeCancel() {

    }



    @Override
    public PermissionManager.TPermissionType invoke(InvokeParam invokeParam) {
        PermissionManager.TPermissionType type = PermissionManager.checkPermission(TContextWrap.of(this), invokeParam.getMethod());
        if (PermissionManager.TPermissionType.WAIT.equals(type)) {
            this.invokeParam = invokeParam;
        }
        return type;
    }

    private void configCompress(TakePhoto takePhoto) {
        int maxSize = 800;
        int width = 400;
        int height = 400;
        boolean showProgressBar =  true;
        boolean enableRawFile =  false;
        CompressConfig config;
        LubanOptions option = new LubanOptions.Builder().setMaxHeight(height).setMaxWidth(width).setMaxSize(maxSize).create();
        config = CompressConfig.ofLuban(option);
        config.enableReserveRaw(enableRawFile);
        takePhoto.onEnableCompress(config, showProgressBar);
    }

    private void configTakePhotoOption(TakePhoto takePhoto) {
        TakePhotoOptions.Builder builder = new TakePhotoOptions.Builder();
        builder.setWithOwnGallery(true);
        builder.setCorrectImage(true);
        takePhoto.setTakePhotoOptions(builder.create());
    }

    private CropOptions getCropOptions() {
        int height = 600;
        int width = 600;

        CropOptions.Builder builder = new CropOptions.Builder();
        builder.setAspectX(width).setAspectY(height);
        builder.setWithOwnCrop(true);

        return builder.create();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        takePhoto.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.TPermissionType type = PermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.handlePermissionsResult(this, type, invokeParam, this);
    }


    /////////**** 上传
    private void getUploadToken(){
        OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "system/v2/getUploadToken")
                .tag(this)
                .cacheMode(CacheMode.NO_CACHE)
                .params("token", token)
                .params("type", 1)

                .execute(new JsonCallback<CodeResponse>() {
                    @Override
                    public void onSuccess(Response<CodeResponse> response) {
                        qiniuToken = response.body().data.toString();
                        startUpload();
                    }

                    @Override
                    public void onError(Response<CodeResponse> response) {
                        response.getException().printStackTrace();
                        makeSnake(headIconChangeF, response.body().msg, R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
                    }
                });
    }

    private String getQiniuKey(){
        String acNum = RunningData.getInstance().getCurrentAccount();

        int randNumber = (int) (Math.random() * 1000);
        long time = System.currentTimeMillis();
        String timeNumberList = String.valueOf(time);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(time);
        String dateString = simpleDateFormat.format(date);

        //图片后缀
        String surNmae = "png";
        String[] surNmaeArray = imageUrl.toString().split("\\.");
        if (surNmaeArray.length >= 1){
            surNmae = surNmaeArray[surNmaeArray.length - 1];
        }

        String key = "userHeadIcon/" + acNum + "/" + dateString + "/"+ timeNumberList + randNumber + "." + surNmae;
        return key;
    }

    private void startUpload(){
        if (qiniuToken == null || imageUrl == null){
            makeSnake(headIconChangeF, "数据为空", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
            return;
        }
        final String key = getQiniuKey();
        Logger.d("getQiniuKey = " + key);
        Configuration config = new Configuration.Builder()
                .zone(AutoZone.autoZone)
                .build();
        UploadManager uploadManager = new UploadManager(config);
        String token = qiniuToken;
        uploadManager.put(imageUrl, key, token,
                new UpCompletionHandler() {
                    @Override
                    public void complete(String key, ResponseInfo info, JSONObject res) {
                        //res包含hash、key等信息，具体字段取决于上传策略的设置
                        if(info.isOK()) {
                            Logger.d("Upload Success");
                            //***接口还没有
                            freshServer(key);
                        } else {
                            makeSnake(toolbar, "上传失败", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
                        }
                    }
                }, null);
    }

    private void freshServer(final String key){
        createDialog("修改中...");
        OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "user/v2/updateAvatar")
                .tag(this)
                .cacheMode(CacheMode.NO_CACHE)
                .params("token", token)
                .params("avatarUrl", key)
                .execute(new JsonCallback<CodeResponse>() {
                    @Override
                    public void onSuccess(Response<CodeResponse> response) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        changeUserInfo(key);
                        finish();
                        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                    }

                    @Override
                    public void onError(Response<CodeResponse> response) {
                        response.getException().printStackTrace();
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });
    }

    public void changeUserInfo(String key) {
        Gson gson = new Gson();
        String userInfoString = CacheTool.getInstance().getCacheObject(ObjectCacheType.user_info);
        if (userInfoString != "") {
            try {
                UserInfo userInfo = gson.fromJson(userInfoString, UserInfo.class);
                Logger.d("userInfo = " + gson.toJson(userInfo));
                if (userInfo != null){
                    userInfo.avatar_url = key;

                    String userInfoStringNew = gson.toJson(userInfo);
                    CacheTool.getInstance().cacheObject(ObjectCacheType.user_info, userInfoStringNew);

                }
            }catch (Exception e){
            }
        }
    }
}
