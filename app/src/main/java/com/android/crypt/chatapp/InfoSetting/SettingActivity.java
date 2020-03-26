package com.android.crypt.chatapp.InfoSetting;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.guide.LogOrRegActivity;
import com.android.crypt.chatapp.user.BgImageActivity;
import com.android.crypt.chatapp.utility.Cache.CacheClass.ObjectCacheType;
import com.android.crypt.chatapp.utility.Cache.CacheTool;
import com.android.crypt.chatapp.utility.Common.ClickUtils;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.Common.WRKShareUtil;
import com.android.crypt.chatapp.utility.Crypt.CryTool;
import com.android.crypt.chatapp.utility.okgo.callback.JsonCallback;
import com.android.crypt.chatapp.utility.okgo.model.CodeResponse;
import com.android.crypt.chatapp.utility.okgo.utils.Convert;
import com.android.crypt.chatapp.utility.upgrade.UpdateService;
import com.baoyz.actionsheet.ActionSheet;
import com.android.crypt.chatapp.ChatAppApplication;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.utility.Common.GlideCacheUtils;
import com.android.crypt.chatapp.utility.Common.ParameterUtil;
import com.android.crypt.chatapp.utility.Common.ServiceUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.Response;
import com.mob.pushsdk.MobPush;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;

import org.json.JSONObject;

import java.io.File;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends BaseActivity implements ActionSheet.ActionSheetListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_version)
    TextView tvVersion;
    @BindView(R.id.find_new_version)
    TextView findNewVersion;

    @BindView(R.id.ll_version_check)
    LinearLayout llVersionCheck;
    @BindView(R.id.tv_cacheSize)
    TextView tvCacheSize;
    @BindView(R.id.ll_clear_cache)
    LinearLayout llClearCache;
    @BindView(R.id.btn_exit)
    TextView btnExit;
    @BindView(R.id.push_voice)
    TextView pushVoice;

    private String token;
    private String versionName;
    private String versionCode;
    private String version_url = "";
    private int actionKind = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity);
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.title_bar_setting);
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
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        versionName = ServiceUtils.getCurVersionName(getApplication());
        versionCode = ServiceUtils.getCurVersionCode(getApplication()) + ".";
        TextView tv_version = (TextView) findViewById(R.id.tv_version);
        tv_version.setText("【" + versionCode + versionName + "】");
        tvCacheSize.setText(GlideCacheUtils.getInstance().getCacheSize(this));


        String voice_setting = RunningData.getInstance().getCurPushVoice();
        changeTips(voice_setting);
        startCheckVersion();
    }

    private void changeTips(String defaultVoice) {
        if (defaultVoice.equals("dog.mp3")) {
            pushVoice.setText("汪星人");
        } else if (defaultVoice.equals("dev.mp3")) {
            pushVoice.setText("系统音");
        } else if (defaultVoice.equals("blank.mp3")) {
            pushVoice.setText("静音");
        } else {
            pushVoice.setText("喵星人");
        }
    }

    @OnClick({R.id.ll_version_check, R.id.ll_clear_cache, R.id.btn_exit, R.id.ll_account_info, R.id.ll_chat_global_Image, R.id.ll_chat_text_size, R.id.ll_server_setting, R.id.log_out_and_delete_key, R.id.share_app, R.id.ll_black_list, R.id.ll_tab_bar_height_setting})
    public void onViewClick(View v) {
        if (!ClickUtils.isFastClick()) {
            return;
        }
        Intent intent = null;
        AlertDialog.Builder builder = null;
        switch (v.getId()) {
            case R.id.ll_tab_bar_height_setting:
                setPrivateXml(ParameterUtil.CUR_ACCOUNT_MANAGEMENT_XML, "initTabbar", "0");

                builder = new AlertDialog.Builder(this)
                        .setTitle("已开启调整窗口")
                        .setMessage("请重启此应用，然后就可以开始调整")
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            public void onDismiss(final DialogInterface dialog) {
                                finish();
                            }
                        })
                        .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //ToDo: 你想做的事情
                                dialogInterface.dismiss();
                                finish();
                                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                            }
                        });
                builder.create().show();

                break;
            case R.id.ll_black_list:
                makeSnake(btnExit, "此功能即将开放", R.mipmap.toast_alarm, Snackbar.LENGTH_SHORT);
                break;
            case R.id.ll_version_check:
                if (!version_url.equalsIgnoreCase("")){
                    upGradeMethod(version_url);
                }else{
                    makeSnake(btnExit, "当前是最新版本", R.mipmap.toast_alarm, Snackbar.LENGTH_SHORT);
                }
                break;
            case R.id.share_app:
                shareMethod();
                break;
            case R.id.ll_clear_cache:
                builder = new AlertDialog.Builder(this);
                builder.setMessage("确认清除应用缓存?");
                builder.setTitle(getResources().getString(
                        R.string.dialog_alert_tip));
                builder.setPositiveButton(
                        getResources().getString(R.string.dialog_alert_ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                tvCacheSize.setText("0.0byte");
                                //执行删除缓存目录的操作
                                GlideCacheUtils.getInstance().clearImageAllCache(SettingActivity.this);
                            }
                        });
                builder.setNegativeButton(
                        getResources().getString(R.string.dialog_alert_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
                break;

            case R.id.ll_account_info:
                intent = new Intent(this, InfoDataActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.btn_exit:
                logOut(false);
                break;
            case R.id.log_out_and_delete_key:
                logOut(true);
                break;
            case R.id.ll_chat_global_Image:
                choosePicture();
                break;
            case R.id.ll_chat_text_size:
                intent = new Intent(this, TextSizeSetActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_server_setting:
                intent = new Intent(this, ChangePushVoiceActivity.class);
                startActivityForResult(intent, 1);
                break;
            default:
                break;
        }
    }

    private int logoutkind = 0;

    private void logOut(boolean deleteKey) {
        if (deleteKey == true) {
            logoutkind = 2;  //删除本地私钥
        } else {
            logoutkind = 1;  //不删除本地私钥
        }
        actionKind = 1;
        ActionSheet.createBuilder(this, getSupportFragmentManager()).setCancelButtonTitle("取消").setOtherButtonTitles("退出登录").setCancelableOnTouchOutside(true).setListener(this).show();
    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if (actionKind == 1) {
            if (index == 0) {
                if (logoutkind == 1){
                    quitApp(false);
                }else if (logoutkind == 2){
                    quitApp(true);
                }
            }
        } else if (actionKind == 2) {
            if (index == 0) {
                openPhoto(false);
            } else if (index == 1) {
                openPhoto(true);
            }
        }else if (actionKind == 3) {
            String baseUrl = "http://airchat.ideas-lab.cn/download.html";
            String title = "分享你一个安全的通讯工具";
            String sub = "AirChat -- 基于密码学的安全通讯工具";

            if (index == 0){
                shareToWechat(baseUrl, title, sub, SendMessageToWX.Req.WXSceneSession);
            }else if (index == 1){
                shareToWechat(baseUrl, title, sub, SendMessageToWX.Req.WXSceneTimeline);
            }else if (index == 2){
                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType("text/plain");
                share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

                share.putExtra(Intent.EXTRA_SUBJECT, title);
                share.putExtra(Intent.EXTRA_TEXT, baseUrl);
                startActivity(Intent.createChooser(share, "AirChat"));
            }
        }
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancle) {}

    private void quitApp(boolean deleteKey){
        if (deleteKey == false){
            MobPush.setAlias("");
            setPrivateXml(ParameterUtil.CUR_ACCOUNT_MANAGEMENT_XML, "loginState", "0");
            RunningData.getInstance().clearDataWhenExit();
            RunningData.getInstance().clearmListContact();
            RunningData.getInstance().clearmsgList();
            Intent intent = new Intent(this, LogOrRegActivity.class);
            startActivity(intent);
            ChatAppApplication.getDBcApplication().exit();
        }else{
            AlertDialog.Builder builder= new AlertDialog.Builder(this);
            View view= LayoutInflater.from(this).inflate(R.layout.dialog_choosepage, null);
            TextView cancel =view.findViewById(R.id.choosepage_cancel);
            TextView sure =view.findViewById(R.id.choosepage_sure);
            final EditText edittext =view.findViewById(R.id.choosepage_edittext);
            final Dialog dialog= builder.create();
            dialog.show();
            dialog.getWindow().setContentView(view);

            //使editext可以唤起软键盘
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            sure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String inputText =  edittext.getText().toString();
                    final String pwd = RunningData.getInstance().getCurrentPwd();

                    if (inputText.equals(pwd)){
                        MobPush.setAlias("");
                        RunningData.getInstance().clearDataWhenExit();
                        RunningData.getInstance().clearmListContact();
                        RunningData.getInstance().clearmsgList();

                        CryTool tool = new CryTool();
                        String pwd_en = tool.aesEnWith("", RunningData.getInstance().getInnerAESKey());
                        CacheTool.getInstance().cacheObject(ObjectCacheType.cur_pwd, pwd_en);
                        CacheTool.getInstance().clearPri_key(RunningData.getInstance().getCurrentAccount());

                        makeSnake(btnExit, "正确", R.mipmap.toast_alarm, Snackbar.LENGTH_SHORT);
                        setPrivateXml(ParameterUtil.CUR_ACCOUNT_MANAGEMENT_XML, "loginState", "0");
                        Intent intent = new Intent(SettingActivity.this, LogOrRegActivity.class);
                        startActivity(intent);
                        ChatAppApplication.getDBcApplication().exit();
                    }else{
                        makeSnake(btnExit, "密码错误", R.mipmap.toast_alarm, Snackbar.LENGTH_SHORT);
                    }

                    dialog.dismiss();
                }
            });
        }
    }



    private void choosePicture() {
        actionKind = 2;
        ActionSheet.createBuilder(this, getSupportFragmentManager())
                .setCancelButtonTitle("取消")
                .setOtherButtonTitles("选择照片", "删除当前图片")
                .setCancelableOnTouchOutside(true)
                .setListener(this).show();
    }


    private void openPhoto(boolean isdelete) {
        if (isdelete == false) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("设置基础聊天背景")
                    .setMessage("如果对联系人设置了个人聊天背景\n优先显示个人聊天背景")
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    })
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //ToDo: 你想做的事情
                            Intent intent = new Intent(SettingActivity.this, BgImageActivity.class);
                            intent.putExtra("is_global", true);
                            startActivity(intent);
                            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        }
                    });
            builder.create().show();

        } else {
            deleteBgImage("global_bg_image");
        }
    }

    private void deleteBgImage(String account) {
        try {
            String speBgImage = RunningData.getInstance().getBgImageUrl();
            //查找特定的图
            File fileRoot = new File(speBgImage);
            if (fileRoot.exists()) {
                File[] files = fileRoot.listFiles();
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    String imageName = file.getName().split("\\.")[0];
                    if (imageName.equals(account)) {
                        file.delete();
                        makeSnake(btnExit, "操作成功", R.mipmap.toast_alarm, Snackbar.LENGTH_SHORT);
                        break;
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == 1){
                String voice = (String)data.getSerializableExtra("voice");
                changeTips(voice);
            }
        }
    }

    private void shareMethod(){
        actionKind = 3;
        ActionSheet.createBuilder(this, getSupportFragmentManager())
                .setCancelButtonTitle("取消")
                .setOtherButtonTitles("分享给微信联系人", "分享到朋友圈", "更多")
                .setCancelableOnTouchOutside(true)
                .setListener(this).show();
    }

    private void shareToWechat(String url, String title, String desc, final int wxSceneSession){
        WRKShareUtil sharetool = WRKShareUtil.getInstance();
        sharetool.shareUrlToWx(url, title, desc, wxSceneSession);
    }


    private void startCheckVersion(){
        findNewVersion.setVisibility(View.GONE);
        String version  = versionCode + versionName;
        OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "system/v2/checkAppVersion")
                .tag(this)
                .params("token", token)
                .params("os", "Android")
                .params("version", version)
                .cacheMode(CacheMode.NO_CACHE)
                .execute(new JsonCallback<CodeResponse>() {
                    @Override
                    public void onSuccess(Response<CodeResponse> response) {
                        if (response.body().code == 1){
                            try {
                                JSONObject data = Convert.formatToJson(response.body().data);
                                version_url = data.getString("version_url");
                                findNewVersion.setVisibility(View.VISIBLE);
                            } catch (Exception ex) {}
                        }
                    }
                    @Override
                    public void onError(Response<CodeResponse> response) {

                    }
                });
    }

    private void upGradeMethod(String version_url){
        Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(version_url));
        startActivity(it);
        return;
//        if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) && !checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
//            ActivityCompat.requestPermissions(SettingActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
//            Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(version_url));
//            startActivity(it);
//            return;
//        }else{
//            UpdateService.Builder.create(version_url).setStoreDir("sdcard/Android/package/update").setDownloadSuccessNotificationFlag(Notification.DEFAULT_ALL).setDownloadErrorNotificationFlag(Notification.DEFAULT_ALL).build(this);
//            makeSnake(btnExit, "正在后台下载", R.mipmap.toast_alarm, Snackbar.LENGTH_SHORT);
//
//        }
    }

//    private void requestAppVersion() {
//        RequestParams params = new RequestParams();
//        addRequestHeader(params);
//        ChatAppApplication.getContext().httpRequest.xPostjson(mContext, params, Constant.BASE_HTTP + ContantUrl.getVersionUpDate, new RequestResultJsonCallBack() {
//            @Override
//            public void onSucess(String result) {
//                Logger.e(TAG, "requestAppVersion-------" + result);
//                NewBaseBean info = Constant.getPerson(result, NewBaseBean.class);
//                if (info.getCode() == Constant.RETURN_SUCCESS__STATE_CODE) {
//                    UpdataAppBean bean = Constant.getPerson(result, UpdataAppBean.class);
//                    List<UpdataAppBean.DataBean.ListBean> list = bean.getData().getList();
//                    if (list != null && list.size() > 0) {
//                        listBean = list.get(0);
//                        String name = listBean.getName();
//                        if (!name.equals(ContantUrl.SERVER_VERSION_NAME)) {
//                            final String url = listBean.getUrl();
//                            String text = mContext.getResources().getString(R.string.check_upgrade);
//                            UpgradeDialog upgrade = new UpgradeDialog(MainActivity.this, text, new UpgradeDialog.OnClickconfirmListener() {
//                                @Override
//                                public void confirm() {
//                                    if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) && !checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_MAIN);
//                                        return;
//                                    }
//
//                                    UpdateService.Builder.create(url).setStoreDir(ContantUrl.AppFile).setDownloadSuccessNotificationFlag(Notification.DEFAULT_ALL).setDownloadErrorNotificationFlag(Notification.DEFAULT_ALL).build(mContext);
//                                    Toast.makeText(mContext, "正在后台下载", Toast.LENGTH_LONG).show();
//                                    //通过浏览器去下载APK
//                                    //                                    InstallUtils.installAPKWithBrower(mContext, url);
//
//                                }
//                            });
//                            upgrade.show();
//                            upgrade.setDetail(listBean.getDescription());
//
//                        }
//                    }
//                } else {
//                    Logger.e(TAG, info.getMessage());
//                }
//
//            }
//
//            @Override
//            public void onFailure(int errorCode, String errorMsg) {
//                showErrorLogger(TAG, errorCode, errorMsg);
//            }
//        });
//    }
//
//    public boolean checkPermission(@NonNull String permission) {
//        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 0) {//权限走的是这里
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                UpdateService.Builder.create(version_url).setStoreDir("sdcard/Android/package/update").setDownloadSuccessNotificationFlag(Notification.DEFAULT_ALL).setDownloadErrorNotificationFlag(Notification.DEFAULT_ALL).build(this);
//                Toast.makeText(this, "正在后台下载", Toast.LENGTH_LONG).show();
//                删除apk文件(获取权限之后)
//                FileUtils.deleteFile(new File(ContantUrl.absolutePath + File.separator + ContantUrl.AppFile + "/GangGang_release-1.0.apk"));
//            } else {
//                AlertDialog.Builder builder = new AlertDialog.Builder(this)
//                        .setTitle("你没有开启安装权限")
//                        .setMessage("是否去下载页面下载？")
//                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(version_url));
//                                startActivity(it);
//                            }
//                        })
//                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//
//                            }
//                        });
//                builder.create().show();
//            }
//        }
//    }


}